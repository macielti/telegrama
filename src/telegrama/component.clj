(ns telegrama.component
  (:require [cheshire.core :as json]
            [chime.core]
            [clojure.tools.logging :as log]
            [http-client-component.core :as component.http-client]
            [integrant.core :as ig]
            [schema.core :as s]
            [telegrama.component.adapters.update :as adapters.update]
            [telegrama.component.models.update :as models.update])
  (:import (java.time Duration Instant)))

(s/defn get-updates! :- [models.update/Update]
  [offset http-client {:keys [telegram]}]
  (let [url (str "https://api.telegram.org/bot" (:token telegram) "/getUpdates?offset=" @offset)
        request {:url         url
                 :method      :get
                 :endpoint-id :telegram-consumer-get-updates}]
    (-> @(component.http-client/request! request http-client)
        :body (json/decode true) :result
        (->> (map adapters.update/wire->model)))))

(defn ^:private consume-updates!
  [offset _settings http-client config _as-of]
  (let [updates (get-updates! offset http-client config)
        {latest-update-id :id} (-> updates last)]
    (when latest-update-id
      (reset! offset (inc latest-update-id))
      (doseq [_update updates]))))

(defmethod ig/init-key ::consumer
  [_ {:keys [settings components]}]
  (log/info :starting ::consumer)

  (let [offset (atom 0)]
    (chime.core/chime-at (chime.core/periodic-seq (Instant/now) (Duration/ofSeconds 1))
                         (partial consume-updates! offset settings (:http-client components) (:config components)))))

(defmethod ig/halt-key! ::consumer
  [_ _consumer]
  (log/info :stopping ::consumer))