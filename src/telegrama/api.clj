(ns telegrama.api
  (:require [cheshire.core :as json]
            [http-client-component.core :as component.http-client]))

(def BASE_URL "https://api.telegram.org/bot")

(defn send-file!
  "Helper function to send various kinds of files as multipart-encoded"
  [token chat-id options file method field filename http-client]
  (let [url (str BASE_URL token method)
        base-form [{:name "chat_id" :content (str chat-id)}
                   {:name field :content file :filename filename}]
        options-form (for [[key value] options]
                       {:name (name key) :content value})
        form (into base-form options-form)]
    (-> @(component.http-client/request! {:url         url
                                          :method      :post
                                          :endpoint-id :telegram-send-document
                                          :payload     {:multipart form}}
                                         http-client)
        :body
        (json/decode true))))

(defn send-text!
  "Sends message to the chat"
  ([token chat-id text http-client] (send-text! token chat-id {} text http-client))
  ([token chat-id options text http-client]
   (let [url (str BASE_URL token "/sendMessage")
         body (into {:chat_id chat-id :text text} options)
         resp @(component.http-client/request! {:url         url
                                                :method      :post
                                                :endpoint-id :telegram-send-text
                                                :payload     {:content-type :json
                                                              :form-params  body}}
                                               http-client)]
     (-> resp :body (json/decode true)))))
