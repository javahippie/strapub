(ns strapub.activitypub.signature
  "Provides functions to hash texts with SHA-265 and sign them with an RSA key"
  (:require [strapub.config :refer [env]]))

(defn- clean-pem-key
  "Clears the key, if it is passed as a PEM format with BEGIN... END... blocks and line breaks"
  [key]
  (.decode (java.util.Base64/getDecoder )
           (-> key
               (.replaceAll "-----(BEGIN|END) PRIVATE KEY-----(\n)?" "")
               (.replaceAll "\n" ""))))

(defn- private-key-from-string
  "Creates an object of the class PrivateKey from the given PK String"
  [input]
  (let [key-factory (java.security.KeyFactory/getInstance "RSA")]
    (->> input
         (java.security.spec.PKCS8EncodedKeySpec.)
         (.generatePrivate key-factory))))

(defn- create-hash
  "Hashes a given String with SHA-256 and retuns it as a byte array"
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (->> (.getBytes value java.nio.charset.StandardCharsets/UTF_8)
         (.digest digest))))

(defn- sign-hash
  "Signs the hash in byte array representation with a private key"
  [hash privatekey]
  (let [signature (java.security.Signature/getInstance "SHA256withRSA")]
    (.initSign signature privatekey)
    (.update signature hash)
    (.sign signature)))



(defn hash-and-sign
  "Combines the hashing and signing from above and returns the result as a hex string"
  [text private-key-as-pem]
  (.formatHex (java.util.HexFormat/of)
              (-> text
                  (create-hash)
                  (sign-hash (private-key-from-string (clean-pem-key private-key-as-pem))))))

(defn verify-hash [hash expected-value public-key]
  (println (format "Called with hash: %s, expected value %s, public key %s" hash expected-value public-key))
  )

(comment
  (hash-and-sign "(request-target): get /users/username/outbox\nhost: mastodon.example\ndate: 18 Dec 2019 10:08:46 GMT"
                 (:privatekey env))
  )
