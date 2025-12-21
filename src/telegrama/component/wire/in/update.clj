(ns telegrama.component.wire.in.update
  (:require [common-clj.schema.core :as common-schema]
            [schema.core :as s]))

(def from
  {:id s/Int})
(s/defschema From
  (common-schema/loose-schema from))

(def entity
  {:type s/Str})
(s/defschema Entity
  (common-schema/loose-schema entity))

(def message
  {:from     From
   :text     s/Str
   :entities [Entity]})
(s/defschema Message
  (common-schema/loose-schema message))

(def event
  {:message   Message
   :update_id s/Int})
(s/defschema Update
  (common-schema/loose-schema event))
