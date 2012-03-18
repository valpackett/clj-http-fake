(ns clj-http.fake
  (:require [clj-http.client :as client]
            [clj-http.util :as util])
  (:use [robert.hooke]))

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
  (let [mapped-route (client/parse-url route)
        requested-route (select-keys req [:scheme
                                          :server-name
                                          :server-port
                                          :uri
                                          :query-string
                                          :user-info])]
    (= mapped-route requested-route)))

(add-hook #'clj-http.core/request
  (fn [origfn req]
    (if-let [route (val (first (filter #(matches (key %) req) *fake-routes*)))]
      (let [resp (route (assoc req :scheme (symbol (:scheme req))))]
        (assoc resp :body (util/utf8-bytes (:body resp))))
      (origfn req))))