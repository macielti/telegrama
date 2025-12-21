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
   :identity Identity})

(def bot-command
  {:type    (s/eq :bot-command)
   :raw     s/Str
   :command s/Keyword})
(s/defschema BotCommand
  (merge base
         bot-command))

(s/defschema Update
  (s/conditional #(= (:type %) :bot-command) BotCommand))