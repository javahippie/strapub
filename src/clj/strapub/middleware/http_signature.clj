(ns strapub.middleware.http-signature
   (:require [clj-http.client :as client]
             [clojure.string :as str]
             [cheshire.core :refer [parse-string]]))

(defn- extract-signature-fields [signature]
  (into {}
        (vec
         (map #(str/split %1 #"=" 2)
              (str/split signature  #"," )))))

(defn- retrieve-public-key [key-id]
  (-> key-id
      client/get
      :body
      parse-string
      (get-in ["publicKey" "publicKeyPem"])))

(defn http-signature-middleware
  "Teeest"
  [handler]
  (fn [{:keys [headers] :as request}]

    (let [{:strs [keyId headers signature]}
          (extract-signature-fields (get headers "signature"))]
      (println (retrieve-public-key keyId)))


    ;; Header zerlegen
    ;; Link zu Key Extrahieren
    ;; Actor abrufen, Public Key abziehen
    ;; Entschlüsseln
    ;; Hash über Header Felder bilden
    ;; Vergleichen
    (handler request)))

(comment
  (retrieve-public-key "https://freiburg.social/actor#main-key")

  )
