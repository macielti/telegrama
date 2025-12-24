(ns telegrama.component.models.update
  (:require [schema.core :as s]))

(def types #{:bot-command})
(def UpdateType (apply s/enum types))

(def identification
  {:id s/Int})
(s/defschema Identification
  identification)

(def base
  {:id             s/Int
   :identification Identification
   :raw            s/Str})

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
