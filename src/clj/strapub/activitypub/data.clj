(ns strapub.activitypub.data)

(defn user-json
  "Creates the document for the user which can then be queries by webfinger.
  Returns to a single hardcoded user right now"
  [username {:keys [schema host]}]
  {:subject username
   :links [
           {:rel "self"
            :type "application/activity+json"
            :href (format "%s://%s/user/tim" schema host)}]})

(defn user-account-json-ld
  "Provides metadata about the actor / user, as well as links to the inbox and outbox
   and the public key"
  [username {:keys [schema host publickey]}]
  {"@context" ["https://www.w3.org/ns/activitystreams"
               "https://w3id.org/security/v1"]
   "id" (format "%s://%s/user/%s" schema host username)
   "type" "Person"
   "preferredUsername" "tim"
   "name" "Tim Zöller"
   "following" (format "%s://%s/user/%s/following" schema host username)
   "followers" (format "%s://%s/user/%s/followers" schema host username)
   "inbox" (format "%s://%s/user/%s/inbox" schema host username)
   "outbox" (format "%s://%s/user/%s/outbox" schema host username)
   "publicKey" {"id" (format "%s://%s/user/%s#main-key" schema host username)
                "owner" (format "%s://%s/user/%s" schema host username)
                "publicKeyPem" publickey}})

(defn process-inbox-message
  "Receives and delegates an inbox message"
  [message]
  (println message))
