(ns clj-http.fake
  (:use (clj-http core util),
        robert.hooke))

(def ^:dynamic *fake-routes* {})

(defmacro with-fake-routes
  "Makes all wrapped clj-http requests first match against given routes.
  The actual HTTP request will be sent only if no matches are found."
  [routes & body]
  `(do
     (let [s# ~routes]
       (assert (map? s#))
       (binding [*fake-routes* s#]
         ~@body))))

(defn matches [route req]
  (let [route (if (= (class route) java.util.regex.Pattern)
                  route
                  (if (string? route)
                    (re-pattern
                      (if (.startsWith route "http")
                          route
                          (str "http://" route)))))
        uri (format "%s://%s%s%s%s"
                    (name (:scheme req))
                    (:server-name req)
                    (if (= 80 (:server-port req)) "" (str ":" (:server-port req)))
                    (:uri req)
                    (or (:query-string req) ""))]
    (boolean (re-matches route uri))))

(add-hook #'clj-http.core/request
  (fn [origfn req]
    (if-let [route (val (first (filter #(matches (key %) req) *fake-routes*)))]
      (let [resp (route (assoc req :scheme (symbol (:scheme req))))]
        (assoc resp :body (utf8-bytes (:body resp))))
      (origfn req))))