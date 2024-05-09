(ns strapub.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [strapub.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[strapub started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[strapub has shut down successfully]=-"))
   :middleware wrap-dev})
