(ns strapub.routes.services
  (:require
    [reitit.ring.coercion :as coercion]
    [reitit.coercion.spec :as spec-coercion]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [strapub.middleware.formats :as formats]
    [strapub.config :refer [env]]
    [strapub.activitypub.data :as activitypub]))



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
                          :body (activitypub/user-account-json-ld username env)}
                         {:status 404}))}}]
    ["/:username/inbox"
     {:post {:parameters {:path {:username string?}}
             :handler (fn [request]
                        (activitypub/process-inbox-message request)
                        {:status 200})}}]

    ["/:username/outbox"
     {:post {:parameters {:path {:username string?}}
             :handler (fn [{{{:keys [username]} :path} :parameters}]
                        {:status 200})}}]

    ["/:username/following"
     {:post {:parameters {:path {:username string?}}
             :handler (fn [{{{:keys [username]} :path} :parameters}]
                        {:status 200})}}]

    ["/:username/followers"
     {:post {:parameters {:path {:username string?}}
             :handler (fn [{{{:keys [username]} :path} :parameters}]
                        {:status 200})}}]]


   ["/.well-known"
    ["/webfinger"
     {:get {:parameters {:query {:resource string?}}
            :handler (fn [{{{:keys [resource]} :query} :parameters}]
                       (let [{:keys [host]} env]
                         (if (= resource (format "acct:tim@%s" host))
                           {:status 200
                            :body (activitypub/user-json resource env)}
                           {:status 404})))}}]]])
