(ns telegrama.component.models.config
  (:require [common-clj.schema.core :as common-schema]
            [schema.core :as s]))

(def telegram
  {:token s/Str})
(s/defschema Telegram
  (common-schema/loose-schema telegram))

(def config
  {:telegram Telegram})
(def Config
  (common-schema/loose-schema config))
