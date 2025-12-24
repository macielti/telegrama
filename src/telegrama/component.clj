(ns telegrama.component
  (:require [cheshire.core :as json]
            [chime.core]
            [clojure.tools.logging :as log]
            [http-client-component.core :as component.http-client]
            [integrant.core :as ig]
            [io.pedestal.interceptor]
            [io.pedestal.interceptor.chain :as interceptor.chain]
            [schema.core :as s]
            [telegrama.component.adapters.event :as adapters.event]
            [telegrama.component.models.config :as models.config]
            [telegrama.component.models.event :as models.event]
            [telegrama.component.models.settings :as models.settings])
  (:import (clojure.lang IFn)
           (io.pedestal.interceptor Interceptor)
           (java.time Duration Instant)))

(s/defn get-events! :- [models.event/Event]
  [offset :- s/Int
   http-client
   {{:keys [token poll-timeout-seconds]} :telegram} :- models.config/Config]
  (let [url (str "https://api.telegram.org/bot" token "/getUpdates?offset=" @offset "&timeout=" (or poll-timeout-seconds 60))
        request {:url         url
                 :method      :get
                 :endpoint-id :telegram-consumer-get-updates}]
    (-> @(component.http-client/request! request http-client)
        :body (json/decode true) :result
        (->> (map adapters.event/wire->model)))))

(s/defn settings-by-event :- (s/maybe models.settings/HandlerSettings)
  [{:keys [type command]} :- models.event/Event
   settings :- models.settings/Settings]
  (case type
    :bot-command (get-in settings [type command])))

(s/defn handler->interceptor :- Interceptor
  [handler :- IFn]
  (io.pedestal.interceptor/interceptor
   {:name  :handler-function
    :enter handler}))

(s/defn ^:private consume-events!
  [offset :- s/Int
   settings :- models.settings/Settings
   {:keys [http-client config] :as components}
   _as-of]
  (let [events (get-events! offset http-client config)]
    (doseq [event events
            :let [{:keys [handler interceptors]} (settings-by-event event settings)
                  context {:components components
                           :event      event}
                  interceptors' (delay (-> (concat interceptors [(handler->interceptor handler)]) flatten))]]
      (if handler
        (interceptor.chain/execute context @interceptors')
        (log/warn :event-type-not-supported :event (dissoc event :raw)))
      (reset! offset (-> event :id inc)))))

(defmethod ig/init-key ::consumer
  [_ {:keys [settings components]}]
  (log/info :starting ::consumer)

  (s/validate models.config/Config (:config components))
  (assert (not (nil? settings)) "You must provide settings for the handlers - Telegram Consumer component")
  (assert (not (nil? (:http-client components))) "You must provide a HTTP Client for the Telegram Consumer component")

  (let [poll-interval-seconds (get-in components [:config :telegram :poll-interval-seconds] 1)
        offset (atom 0)]
    (chime.core/chime-at (chime.core/periodic-seq (Instant/now) (Duration/ofSeconds poll-interval-seconds))
                         (partial consume-events! offset settings components))))

(defmethod ig/halt-key! ::consumer
  [_ consumer]
  (log/info :stopping ::consumer)
  (.close consumer))
