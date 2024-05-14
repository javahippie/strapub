(ns strapub.activitypub.signature
  "Provides functions to hash texts with SHA-265 and sign them with an RSA key"
  (:require [strapub.config :refer [env]]))

(defn- clean-pem-key
  "Clears the key, if it is passed as a PEM format with BEGIN... END... blocks and line breaks"
  [key]
  (.decode (java.util.Base64/getDecoder )
           (-> key
               (.replaceAll "-----(BEGIN|END) .* KEY-----(\n)?" "")
               (.replaceAll "\n" ""))))

(defn- private-key-from-string
  "Creates an object of the class PrivateKey from the given PK String"
  [input]
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

(defn create-hash
  "Hashes a given String with SHA-256 and retuns it as a string"
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")]
    (->> (.getBytes value java.nio.charset.StandardCharsets/UTF_8)
         (.digest digest)
         (.formatHex (java.util.HexFormat/of)))))

(defn- sign-hash
  "Signs the hash in byte array representation with a private key"
  [value privatekey]
  (let [signature (java.security.Signature/getInstance "SHA256withRSA")]
    (.initSign signature privatekey)
    (.update signature (.getBytes value java.nio.charset.StandardCharsets/UTF_8))
    (.sign signature)))

(defn verify-hash [signed-hash expected-value public-key]
  (println "Signatur: " (String. signed-hash) " has type " (type signed-hash))
  (println "Hash: " expected-value " has type " (type expected-value))

  (let [signature (java.security.Signature/getInstance "SHA256withRSA")]
    (.initVerify signature (public-key-from-string (clean-pem-key public-key)))
    (.update signature (.getBytes expected-value java.nio.charset.StandardCharsets/UTF_8))
    (.verify signature (.decode (java.util.Base64/getDecoder) signed-hash))))


(defn hash-and-sign
  "Combines the hashing and signing from above and returns the result as a hex string"
  [text private-key-as-pem]
  (.formatHex (java.util.HexFormat/of)
              (sign-hash text (private-key-from-string (clean-pem-key private-key-as-pem)))))

(comment
  (hash-and-sign "(request-target): get /user/tim\nhost: localhost\ndate: 18 Dec 2019 10:08:46 GMT"
                 (:privatekey env))

  (verify-hash (.encode (java.util.Base64/getEncoder) (.getBytes "9641b09360e81b8f0c423dde63387023541bf249fd8ab018e5386ac029fd364d30183365fe065159dcdf2adc2045fcba73cc616ee5701fd09392d3290f2e4e0ef6073424decbc14ae9bb60975a0a707cacae406195addc0322d46a9d9a763b86533c6ce4e7691333b6c001ff481c5703aad9d3c4f316a9573d0e98ddd61f8f5e4a4b5955c5ff5635c2172ced7f218fec3b477bcd20caeb5bb5c1072a32245363b8ab44e152805af0524e2a49171dbaefcc7bc6dce7893112935f3e045098a5e89f9256ea13843dab52e41024d53d1bf4f3dd41dff4ad28bfefd350505241fa9b0de53361486e379c0343b77c2027426431a10708befce32b804682e65803f69b"))
               "(request-target): get /user/tim\nhost: localhost\ndate: 18 Dec 2019 10:08:46 GMT"
               (:publickey env))

  ;; mastodon.social
  (let [my-hash "(request-target): post /user/tim/inbox\nhost: strapub.javahippie.net\ndate: Tue, 14 May 2024 18:32:50 GMT\ndigest: SHA-256=woX1MKwLTnkaVjaxjJHntCOIPwsvZOyzP1AfJ6sEG9c=\ncontent-type: application/activity+json"]

    (verify-hash  "M5O9G5Kawkcr16LT31CuY8AV9STSmZXkg4LgoLfst7pXBXre9AbV/DVZlc62pq2B7sPIdtpNAMfrhhGncedY3WLhD+NijdCylbKwbUKMUQAxVp6WPQpm87/znz43iWmAr9mu8BaR596JyjWNiwJ8V5h5/s3BOJi0k+/Gu5fCWcNrTUqNN1wukjbCDeLTM71IIN7IBUfgeNcxots4+cLka0TSbESCNKc75sgAKbFj+4Y0/y0pP+gkHz5KQf2I4kHGLcqYYCSFUyZpe5DOuciMrS2xv4StqaknYeoHK4LObXSfD8HIiWpZtXHp5dKkg/nz2q2E2dgs0e9sSdxXNxVuXQ=="
                  my-hash
                  "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArS+f/mzIg532Q29GwNPM\nVGsv9cxsMR3dkZGTTN7UcJGO+LVCymkDzLW7Zd1897oyESVh1yYnwLmIQmrJCsgN\ngqYrhiyDwdeCgg/9g5Xnpd9IhJpvq4ZnQI/mgc0SkUhfl+i9eWp5zMfaBeyH47iL\nbImUhAhlLH1TBxj0RrmZ45TFdg/tAF2m/QDw3uYIMTaFmf6y+snIqwekPeA/ky+j\nhUgDTzSGXX5piwKnYPZeAzsmH3sBJvYs9Qr+CJLk4Eq+Nn8QfhM/lklGQw3EM/RF\nRot/xKxB9p896w6GqY+bZyLZ+EOjeNQJllDL1fmtGRTpXEIxTU+eHcSr0gi5DuR8\nSQIDAQAB\n-----END PUBLIC KEY-----\n"
                  ))

  )
