(ns telegrama.component.wire.in.update
  (:require [schema.core :as s]
            [common-clj.schema.core :as common-schema]))

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

(def update
  {:message   Message
   :update_id s/Int})
(s/defschema Update
  (common-schema/loose-schema update))