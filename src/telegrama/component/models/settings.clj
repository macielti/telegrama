(ns telegrama.component.models.settings
  (:require [schema.core :as s])
  (:import (clojure.lang IFn)
           (io.pedestal.interceptor Interceptor)))

(def handler-settings
  {:handler      IFn
   :interceptors [Interceptor]})

(s/defschema HandlerSettings
  handler-settings)

(def settings
  {:bot-command {s/Keyword HandlerSettings}})

(s/defschema Settings
  settings)
