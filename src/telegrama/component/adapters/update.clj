(ns telegrama.component.adapters.update
  (:require [clojure.string :as str]
            [schema.core :as s]
            [telegrama.component.models.update :as models.update]
            [telegrama.component.wire.in.update :as wire.in.update]))

(s/defn wire->identity :- models.update/Identity
  [{{:keys [from]} :message} :- wire.in.update/Update]
  {:id (:id from)})

(s/defn wire-update->type :- models.update/UpdateType
  [update :- wire.in.update/Update]
  (cond (some-> update :message :entities first :type (= "bot_command")) :bot-command
        :else :other))

(defmulti wire->model
  (fn [update] (wire-update->type update)))

(s/defmethod wire->model :bot-command :- models.update/BotCommand
  [{{:keys [text]} :message
    update-id      :update_id :as update} :- wire.in.update/Update]
  {:id       update-id
   :type     :bot-command
   :raw      update
   :text     text
   :identity (wire->identity update)
   :command  (-> text (str/split #" ") first (str/replace #"/" ""))})

(s/defmethod wire->model :other :- models.update/Other
  [{update-id :update_id :as update} :- wire.in.update/Update]
  {:id       update-id
   :type     :other
   :raw      update
   :identity (wire->identity update)})
