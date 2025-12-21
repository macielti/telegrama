(ns telegrama.component.adapters.update
  (:require [clojure.string :as str]
            [schema.core :as s]
            [telegrama.component.models.update :as models.update]))

(s/defn wire->identity :- models.update/Identity
  [{{:keys [from]} :message}]
  {:id (:id from)})

(s/defn wire-update->type :- models.update/UpdateType
  [update]
  (cond (some-> update :message :entities first :type (= "bot_command")) :bot-command))

(defmulti wire->model
  (fn [update] (wire-update->type update)))

(s/defmethod wire->model :bot-command :- models.update/BotCommand
  [{{:keys [text]} :message
    update-id      :update_id :as update}]
  {:id       update-id
   :type     :bot-command
   :raw      text
   :identity (wire->identity update)
   :command  (-> text (str/split #" ") first (str/replace #"/" ""))})