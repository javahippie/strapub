(ns strapub.middleware.formats
(:require [muuntaja.core :as m]
          [muuntaja.format.json :as json-format]
          [strapub.middleware.ld-json :as json-ld-format]))

(def instance
  (m/create (-> m/default-options
                (assoc :formats {"application/json" json-format/format
                                 "application/jrf+json" json-ld-format/format})
                (assoc  :default-format "application/jrd+json"))))
