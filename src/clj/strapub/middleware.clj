(ns strapub.middleware
  (:require
    [strapub.env :refer [defaults]]
    [strapub.config :refer [env]]
    [strapub.middleware.http-signature :refer [http-signature-middleware]]
    [ring.middleware.flash :refer [wrap-flash]]
    [ring.adapter.undertow.middleware.session :refer [wrap-session]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]])
  )

(defn debug-middleware [handler]
  (fn [request]
    (handler request)))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      wrap-flash
      (wrap-session {:cookie-attrs {:http-only true}})
      (wrap-defaults
       (-> site-defaults
           (assoc-in [:security :anti-forgery] false)
           (dissoc :session)))
      http-signature-middleware))
