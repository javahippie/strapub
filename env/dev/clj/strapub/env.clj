(ns strapub.env
  (:require
    [clojure.tools.logging :as log]
    [strapub.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[strapub started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[strapub has shut down successfully]=-"))
   :middleware wrap-dev})
