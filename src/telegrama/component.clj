(ns telegrama.component
  (:require [cheshire.core :as json]
            [chime.core]
            [clojure.tools.logging :as log]
            [http-client-component.core :as component.http-client]
            [integrant.core :as ig]
            [io.pedestal.interceptor]
            [io.pedestal.interceptor.chain :as interceptor.chain]
            [schema.core :as s]
            [telegrama.component.adapters.update :as adapters.update]
            [telegrama.component.models.config :as models.config]
            [telegrama.component.models.settings :as models.settings]
            [telegrama.component.models.update :as models.update])
  (:import (clojure.lang IFn)
           (io.pedestal.interceptor Interceptor)
           (java.time Duration Instant)))

(s/defn get-updates! :- [models.update/Update]
  [offset :- s/Int
   http-client
   {:keys [telegram]} :- models.config/Config]
  (let [url (str "https://api.telegram.org/bot" (:token telegram) "/getUpdates?offset=" @offset)
        request {:url         url
                 :method      :get
                 :endpoint-id :telegram-consumer-get-updates}]
    (-> @(component.http-client/request! request http-client)
        :body (json/decode true) :result
        (->> (map adapters.update/wire->model)))))

(s/defn settings-by-update :- models.settings/HandlerSettings
  [{:keys [type command]} :- models.update/Update
   settings :- models.settings/Settings]
  (get-in settings [type command]))

(s/defn handler->interceptor :- Interceptor
  [handler :- IFn]
  (io.pedestal.interceptor/interceptor
   {:name  :handler-function
    :enter handler}))

(s/defn ^:private consume-updates!
  [offset :- s/Int
   settings :- models.settings/Settings
   {:keys [http-client config] :as components}
   _as-of]
  (let [updates (get-updates! offset http-client config)]
    (doseq [update updates
            :let [{:keys [handler interceptors]} (settings-by-update update settings)
                  context {:components components
                           :update     update}
                  interceptors' (delay (-> (concat [] interceptors [(handler->interceptor handler)])
                                           flatten))]]
      (if handler
        (interceptor.chain/execute context @interceptors')
        (log/warn :update-type-not-supported :update update))
      (reset! offset (-> update :id inc)))))

(defmethod ig/init-key ::consumer
  [_ {:keys [settings components]}]
  (log/info :starting ::consumer)

  (s/validate models.config/Config (:config components))
  (assert (not (nil? settings)) "You must provide settings for the handlers - Telegram Consumer component")
  (assert (not (nil? (:http-client components))) "You must provide a HTTP Cliente for the Telegram Consumer component")

  (let [offset (atom 0)]
    (chime.core/chime-at (chime.core/periodic-seq (Instant/now) (Duration/ofSeconds 1))
                         (partial consume-updates! offset settings components))))

(defmethod ig/halt-key! ::consumer
  [_ _consumer]
  (log/info :stopping ::consumer))