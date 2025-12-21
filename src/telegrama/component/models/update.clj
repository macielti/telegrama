(ns telegrama.component.models.update
  (:require [schema.core :as s]))

(def types #{:bot-command})
(def UpdateType (apply s/enum types))

(def identity
  {:id s/Int})
(s/defschema Identity
  identity)

(def base
  {:id       s/Int
   :identity Identity
   :raw      s/Str})

(def bot-command
  {:type    (s/eq :bot-command)
   :text    s/Str
   :command s/Keyword})
(s/defschema BotCommand
  (merge base
         bot-command))

(def other
  {:type (s/eq :other)})
(s/defschema Other
  (merge base
         other))

(s/defschema Update
  (s/conditional #(= (:type %) :bot-command) BotCommand
                 :else Other))