(ns strapub.middleware.http-signature)

(defn http-signature-middleware
  "Teeest"
  [handler]
  (fn [{:keys [headers] :as request}]
    (println (get headers "signature"))
    (handler request)))
