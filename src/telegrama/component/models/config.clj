(ns telegrama.component.models.config
  (:require [common-clj.schema.core :as common-schema]
            [schema.core :as s]))

(def telegram
  {:token                                  s/Str
   (s/optional-key :poll-interval-seconds) s/Int
   (s/optional-key :poll-timeout-seconds)  s/Int})
(s/defschema Telegram
  (common-schema/loose-schema telegram))

(def config
  {:telegram Telegram})
(s/defschema Config
  (common-schema/loose-schema config))
