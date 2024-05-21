(ns strapub.activitypub.signature
  "Provides functions to hash texts with SHA-265 and sign them with an RSA key"
  (:require [strapub.config :refer [env]]))

(defn- base64-decode [input]
  (.decode (java.util.Base64/getDecoder) input))

(defn- base64-encode [input]
  (.encode (java.util.Base64/getEncoder) input))

(defn- base64-encode-to-string [input]
  (.encodeToString (java.util.Base64/getEncoder) input))

(defn- clean-pem-key
  "Clears the key, if it is passed as a PEM format with BEGIN... END... blocks and line breaks"
  [key]
  (.decode (java.util.Base64/getDecoder )
           (-> key
               (.replaceAll "-----(BEGIN|END) .* KEY-----(\n)?" "")
               (.replaceAll "\n" ""))))

(defn- private-key-from-string
  "Creates an object of the class PrivateKey from the given PK String"
  [^java.lang.String input]
  (let [key-factory (java.security.KeyFactory/getInstance "RSA")]
    (->> input
         (java.security.spec.PKCS8EncodedKeySpec.)
         (.generatePrivate key-factory))))

(defn- public-key-from-string
  "Creates an object of the class PublicKey from the given PK String"
  [input]
  (let [key-factory (java.security.KeyFactory/getInstance "RSA")]
    (->> input
         (java.security.spec.X509EncodedKeySpec.)
         (.generatePublic key-factory))))

(defn- sign-hash
  "Signs the hash in byte array representation with a private key"
  [^"[B" value
   ^java.security.PrivateKey privatekey]
  (let [signature (java.security.Signature/getInstance "SHA256withRSA")]
    (.initSign signature privatekey)
    (.update signature value)
    (.sign signature)))

(defn verify-hash [signed-hash expected-value public-key]
    (let [signature (java.security.Signature/getInstance "SHA256withRSA")]
      (.initVerify signature (public-key-from-string (clean-pem-key public-key)))
      (.update signature (.getBytes expected-value java.nio.charset.StandardCharsets/UTF_8))
      (.verify signature (base64-decode signed-hash))))

(defn hash-and-sign
  "Combines the hashing and signing from above and returns the result as a hex string"
  [text private-key-as-pem]

  (->> private-key-as-pem
       (clean-pem-key)
       (private-key-from-string)
       (sign-hash (.getBytes text java.nio.charset.StandardCharsets/UTF_8))
       (base64-encode-to-string)))

(comment

  (let [hash-base "(request-target): get /user/tim\nhost: localhost\ndate: 18 Dec 2019 10:08:46 GMT"
        signed-hash (hash-and-sign hash-base (:privatekey env))]
    (verify-hash signed-hash hash-base (:publickey env)))


  (hash-and-sign "(request-target): get /user/tim\nhost: localhost\ndate: 18 Dec 2019 10:08:46 GMT"
                 (:privatekey env))



  ;; mastodon.social
  (let [my-hash "(request-target): post /user/tim/inbox\nhost: strapub.javahippie.net\ndate: Tue, 14 May 2024 18:32:50 GMT\ndigest: SHA-256=woX1MKwLTnkaVjaxjJHntCOIPwsvZOyzP1AfJ6sEG9c=\ncontent-type: application/activity+json"]

    (verify-hash  "M5O9G5Kawkcr16LT31CuY8AV9STSmZXkg4LgoLfst7pXBXre9AbV/DVZlc62pq2B7sPIdtpNAMfrhhGncedY3WLhD+NijdCylbKwbUKMUQAxVp6WPQpm87/znz43iWmAr9mu8BaR596JyjWNiwJ8V5h5/s3BOJi0k+/Gu5fCWcNrTUqNN1wukjbCDeLTM71IIN7IBUfgeNcxots4+cLka0TSbESCNKc75sgAKbFj+4Y0/y0pP+gkHz5KQf2I4kHGLcqYYCSFUyZpe5DOuciMrS2xv4StqaknYeoHK4LObXSfD8HIiWpZtXHp5dKkg/nz2q2E2dgs0e9sSdxXNxVuXQ=="
                  my-hash
                  "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArS+f/mzIg532Q29GwNPM\nVGsv9cxsMR3dkZGTTN7UcJGO+LVCymkDzLW7Zd1897oyESVh1yYnwLmIQmrJCsgN\ngqYrhiyDwdeCgg/9g5Xnpd9IhJpvq4ZnQI/mgc0SkUhfl+i9eWp5zMfaBeyH47iL\nbImUhAhlLH1TBxj0RrmZ45TFdg/tAF2m/QDw3uYIMTaFmf6y+snIqwekPeA/ky+j\nhUgDTzSGXX5piwKnYPZeAzsmH3sBJvYs9Qr+CJLk4Eq+Nn8QfhM/lklGQw3EM/RF\nRot/xKxB9p896w6GqY+bZyLZ+EOjeNQJllDL1fmtGRTpXEIxTU+eHcSr0gi5DuR8\nSQIDAQAB\n-----END PUBLIC KEY-----\n"
                  ))

  )
