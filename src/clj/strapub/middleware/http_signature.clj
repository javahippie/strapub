(ns strapub.middleware.http-signature
   (:require [clj-http.client :as client]
             [clojure.string :as str]
             [cheshire.core :refer [parse-string]]
             [strapub.activitypub.data :as activitypub]
             [ring.util.response :as r]
             [strapub.activitypub.signature :as signature]
             [strapub.config :refer [env]]))

(defn- extract-signature-fields [signature]
    (->> (str/split signature #"," )
         (mapcat #(str/split %1 #"=" 2))
         (map #(str/replace % #"\"" ""))
         (partition 2)
         (map vec)
         (vec)
         (into {})))

(defn extract-hostname [url]
  (let [pattern #"^https?:\/\/([^\/?#]+)"
        matcher (re-find pattern url)]
    (if matcher
      (second matcher)
      "No match found.")))

(defn create-signature-hash [request-target key-id]
  (let [host (extract-hostname key-id)]
    (format "(request-target): %s\nhost: %s\ndate: <Date>" request-target host)))

(defn create-signature-header [request-target key-id {:keys [schema host]}]
  (let [actor-key-id (format "%s://%s/user/%s#main-key" schema host "tim")
        signature (signature/hash-and-sign (create-signature-hash request-target key-id) (:privatekey env))]
    {"Signature"
     (format "keyId=\"%s\",headers=\"(request-target) host date\",signature=\"%s\"" actor-key-id signature)}))



(comment
  (create-signature-header "/targi!" "http://localhost/user/tim#main-key" env)
  )

(defn- retrieve-public-key [key-id]
  (-> key-id
      (client/get {:headers (create-signature-header "targi" key-id env)})
      :body
      parse-string
      (get-in ["publicKey" "publicKeyPem"])))

(defn hash-headers [request signature-headers]
  (let [header-list (str/split signature-headers #"\s")]
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
