(ns telegrama.component.adapters.event
  (:require [clojure.string :as str]
            [schema.core :as s]
            [telegrama.component.models.event :as models.event]
            [telegrama.component.wire.in.event :as wire.in.event]))

(s/defn wire->identification :- models.event/Identification
  [{{:keys [from]} :message} :- wire.in.event/Event]
  {:id (:id from)})

(s/defn wire-event->type :- models.event/EventType
  [event :- wire.in.event/Event]
  (cond (some-> event :message :entities first :type (= "bot_command")) :bot-command
        :else :other))

(defmulti wire->model
  (fn [event] (wire-event->type event)))

(s/defmethod wire->model :bot-command :- models.event/BotCommand
  [{{:keys [text]} :message
    update-id      :update_id :as event} :- wire.in.event/Event]
  {:id             update-id
   :type           :bot-command
   :raw            event
   :text           text
   :identification (wire->identification event)
   :command        (-> text (str/split #" ") first (str/replace #"/" "") keyword)})

(s/defmethod wire->model :other :- models.event/Other
  [{update-id :update_id :as event} :- wire.in.event/Event]
  {:id             update-id
   :type           :other
   :raw            event
   :identification (wire->identification event)})
