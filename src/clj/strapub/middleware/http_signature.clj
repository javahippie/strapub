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

(defn hash-headers [request signature-headers]
  (let [header-list (str/split signature-headers #"\s")]
    (signature/create-hash (str/join "\n" (map (fn[header]
                           (if (= "(request-target)" header)
                             (format "%s: %s" header (str (name (:request-method request)) " " (:uri request)))
                             (format "%s: %s" header (get (:headers request) header))))
                         header-list)))))

(defn http-signature-middleware
  "Teeest"
  [handler]
  (fn [request]
    (println (:headers request))
    (if-let [request-headers (get (:headers request) "signature")]
      (let [{:strs [keyId headers signature]} (extract-signature-fields request-headers)
            public-key (retrieve-public-key keyId)
            header-hash (hash-headers request headers)]
        (signature/verify-hash signature header-hash public-key))
      (println "No signature header provided"))


    ;; Header zerlegen
    ;; Link zu Key Extrahieren
    ;; Actor abrufen, Public Key abziehen
    ;; Entschlüsseln
    ;; Hash über Header Felder bilden
    ;; Vergleichen
    (handler request)))

(comment
  (:uri req)
  (retrieve-public-key "https://freiburg.social/actor#main-key")



  )
