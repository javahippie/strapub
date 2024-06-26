(ns strapub.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[strapub started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[strapub has shut down successfully]=-"))
   :middleware identity})
