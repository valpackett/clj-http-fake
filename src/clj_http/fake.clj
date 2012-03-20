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

(defn matches [route request]
  (let [mapped-route (client/parse-url route)]
    (and
     (= (:scheme mapped-route)        (:scheme request))
     (= (:server-name mapped-route)   (:server-name request))
     (if (contains? #{80 nil} (:server-port mapped-route))
       (contains? #{80 nil} (:server-port request))
       (= (:server-port mapped-route) (:server-port request)))
     (if (contains? #{"/" ""} (:uri mapped-route))
       (contains? #{"/" ""} (:uri request))
       (= (:uri mapped-route)         (:uri request)))
     (= (:user-info mapped-route)     (:user-info request))
     (= (:query-string mapped-route)  (:query-string request)))))

(defn try-intercept [origfn request]
  (if-let [matching-route (first (filter #(matches (key %) request) *fake-routes*))]
    (let [route-handler (val matching-route)
          response (route-handler request)]
      (assoc response :body (util/utf8-bytes (:body response))))
    (origfn request)))

(add-hook
 #'clj-http.core/request
 #'try-intercept)