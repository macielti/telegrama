(ns telegrama.component.wire.in.event
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
   (s/optional-key :entities) [Entity]})
(s/defschema Message
  (common-schema/loose-schema message))

(def event
  {:message   Message
   :update_id s/Int})
(s/defschema Event
  (common-schema/loose-schema event))
