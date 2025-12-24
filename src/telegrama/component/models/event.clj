(ns telegrama.component.models.event
  (:require [schema.core :as s]))

(def types #{:bot-command})
(def EventType (apply s/enum types))

(def identification
  {:id s/Int})
(s/defschema Identification
  identification)

(def base
  {:id             s/Int
   :identification Identification
   :raw            {s/Keyword s/Any}})

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

(s/defschema Event
  (s/conditional #(= (:type %) :bot-command) BotCommand
                 :else Other))
