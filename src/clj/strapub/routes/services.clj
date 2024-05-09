(ns strapub.routes.services
  (:require
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [strapub.middleware.formats :as formats]
    [strapub.config :refer [env]]))

(defn user-json-ld [username {:keys [schema host]}]
  {:subject username
   :links [
           {:rel "self"
            :type "application/activity+json"
            :href (format "%s://%s/user/tim" schema host)}]})

(defn user-account-json-ld [username {:keys [schema host publickey]}]
  {"@context" ["https://www.w3.org/ns/activitystreams"
               "https://w3id.org/security/v1"]
   "id" (format "%s://%s/user/%s" schema host username)
   "type" "Person"
   "preferredUsername" "tim"
   "name" "Tim Zöller"
   "inbox" (format "%s://%s/user/%s/inbox" schema host username)
   "outbox" (format "%s://%s/user/%s/outbox" schema host username)
   "publicKey" {"id" (format "%s://%s/user/%s#main-key" schema host username)
                "owner" (format "%s://%s/user/%s" schema host username)
                "publicKeyPem" publickey}})

(defn service-routes []
  [""
   {:coercion spec-coercion/coercion
    :muuntaja formats/instance
    :middleware [ ;; query-params & form-params
                 parameters/parameters-middleware
                 ;; content-negotiation
                 muuntaja/format-negotiate-middleware
                 ;; encoding response body
                 muuntaja/format-response-middleware
                 ;; exception handling
                 coercion/coerce-exceptions-middleware
                 ;; decoding request body
                 muuntaja/format-request-middleware
                 ;; coercing response bodys
                 coercion/coerce-response-middleware
                 ;; coercing request parameters
                 coercion/coerce-request-middleware
                 ;; multipart
                 multipart/multipart-middleware]}

   ["/user"
    ["/:username"
     {:get {:parameters {:path {:username string?}}
            :handler (fn [{{{:keys [username]} :path} :parameters}]
                       (println (format "Queries actor %s" username))
                       (if (= username "tim")
                         {:status 200
                          :body (user-account-json-ld username env)}
                         {:status 404}))}}]
    ["/:username/inbox"
     {:post {:parameters {:path {:username string?}}
             :handler (fn [{{{:keys [username]} :path} :parameters}]
                        (println (format "Posted to inbox of %s" username))
                        {:status 200})}}]

    ["/:username/outbox"
     {:get {:parameters {:path {:username string?}}
             :handler (fn [{{{:keys [username]} :path} :parameters}]
                        (println (format "Reading from outbox of %s" username))
                        {:status 200})}}]]

   ["/.well-known"
    ["/webfinger"
     {:get {:parameters {:query {:resource string?}}
            :handler (fn [{{{:keys [resource]} :query} :parameters}]
                       (let [{:keys [host]} env]
                         (if (= resource (format "acct:tim@%s" host))
                           {:status 200
                            :body (user-json-ld resource env)}
                           {:status 404})))}}]]])
