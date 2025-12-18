(ns telegrama.component
  (:require [chime.core]
            [clojure.tools.logging :as log]
            [http-client-component.core :as component.http-client]
            [integrant.core :as ig])
  (:import (java.time Duration Instant)))

(defn get-updates!
  [offset http-client {:keys [telegram]}]
  (let [url (str "https://api.telegram.org/bot" (:token telegram) "/getUpdates?offset=" offset)]
    @(component.http-client/request! {:url         url
                                      :method      :get
                                      :endpoint-id :telegram-consumer-get-updates}
                                     http-client)))

(defn ^:private consume-updates!
  [offset settings http-client config as-of]
  (doseq [update (get-updates! @offset http-client config)]
    ))

(defmethod ig/init-key ::consumer
  [_ {:keys [settings components]}]
  (log/info :starting ::consumer)

  (let [offset (atom 0)]
    (chime.core/chime-at (chime.core/periodic-seq (Instant/now) (Duration/ofSeconds 1))
                         (partial consume-updates! offset settings (:http-client components) (:config components)))))

(defmethod ig/halt-key! ::consumer
  [_ consumer]
  (log/info :stopping ::consumer))