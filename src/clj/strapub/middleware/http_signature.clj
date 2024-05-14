(ns strapub.middleware.http-signature
   (:require [clj-http.client :as client]
             [clojure.string :as str]
             [cheshire.core :refer [parse-string]]
             [strapub.activitypub.data :as activitypub]
             [ring.util.response :as r]
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
    #_(signature/create-hash)
    (str/join "\n" (map (fn[header]
                          (if (= "(request-target)" header)
                            (format "%s: %s" header (str (name (:request-method request)) " " (:uri request)))
                            (format "%s: %s" header (get (:headers request) header))))
                        header-list))))

(defn http-signature-middleware
  "Teeest"
  [handler]
  (fn
    [request]
    (try
      (println "Middleware is looking for signature")
      (if-let [request-headers (get (:headers request) "signature")]
        (let [{:strs [keyId headers signature]} (extract-signature-fields request-headers)
              public-key (retrieve-public-key keyId)
              header-hash (hash-headers request headers)]
          (println "Has signature header!")
          (if (signature/verify-hash signature header-hash public-key)
            (do
              (println "Signature matches, proceeding!")
              (handler request))
            (do
              (println "No signature provided!")
              (r/bad-request "No signature provided!"))))
        (do
          (println "No signature, oh noes!")
          (r/bad-request "You need to sign your request!")))
      (catch Throwable e
        (println "Shit has hit the fan")
        (.printStackTrace e)
        {:status 500
         :body "We have screwed up!"}))))

(comment
  (:uri req)
  (retrieve-public-key "https://freiburg.social/actor#main-key")



  )
