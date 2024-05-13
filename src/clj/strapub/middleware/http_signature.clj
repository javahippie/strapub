(ns strapub.middleware.http-signature
   (:require [clj-http.client :as client]
             [clojure.string :as str]
             [cheshire.core :refer [parse-string]]
             [strapub.activitypub.data :as activitypub]
             [strapub.activitypub.signature :as signature]))

(defn- extract-signature-fields [signature]
    (->> (str/split signature #"," )
         (mapcat #(str/split %1 #"=" 2))
         (map #(str/replace % #"\"" ""))
         (partition 2)
         (map vec)
         (vec)
         (into {})))

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
    (try
      (if-let [request-headers (get headers "signature")]
        (let [{:strs [keyId headers signature]} (extract-signature-fields request-headers)
              public-key (retrieve-public-key keyId)]
          (signature/verify-hash signature "" public-key))
        (println "No signature header provided"))
      (catch Exception e
          (.printStackTrace e)))


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
