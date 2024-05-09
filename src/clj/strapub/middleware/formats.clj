(ns strapub.middleware.formats
(:require [muuntaja.core :as m]
          [muuntaja.format.json :as json-format]
          [strapub.middleware.ld-json :as json-ld-format]))

(def instance
  (m/create (-> m/default-options
                (assoc :formats {"application/json" json-format/format
                                 "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\"" json-ld-format/format}
                                 "application/activity+json" json-ld-format/format-alt)
                (assoc  :default-format "application/ld+json; profile=\"https://www.w3.org/ns/activitystreams\""))))
