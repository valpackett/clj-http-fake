(ns clj-http.fake
  (:import [java.util.regex Pattern])
  (:require [clj-http.client :as client]
            [clj-http.util :as util])
  (:use [robert.hooke]
        [clojure.math.combinatorics]
        [clojure.string :only [join]]))

(def ^:dynamic *fake-routes* {})
(def ^:dynamic *in-isolation* false)

(defmacro with-fake-routes-in-isolation
  "Makes all wrapped clj-http requests first match against given routes.
  If no route matches, an exception is thrown."
  [routes & body]
  `(binding [*in-isolation* true]
    (with-fake-routes ~routes ~@body)))

(defmacro with-fake-routes
  "Makes all wrapped clj-http requests first match against given routes.
  The actual HTTP request will be sent only if no matches are found."
  [routes & body]
  `(do
     (let [s# ~routes]
       (assert (vector? s#))
       (assert (every? map? s#))
       (binding [*fake-routes* s#]
         ~@body))))

(defn- defaults-or-value [defaults value]
  (if (contains? defaults value) (reverse (vec defaults)) (vector value)))

(defn- potential-server-ports-for [request-map]
  (defaults-or-value #{80 nil} (:server-port request-map)))

(defn- potential-uris-for [request-map]
  (defaults-or-value #{"/" "" nil} (:uri request-map)))

(defn- potential-schemes-for [request-map]
  (defaults-or-value #{:http nil} (keyword (:scheme request-map))))

(defn- potential-alternatives-to [request]
  (let [schemes      (potential-schemes-for      request)
        server-ports (potential-server-ports-for request)
        uris         (potential-uris-for         request)
        combinations (cartesian-product schemes server-ports uris)]
    (map #(merge request (zipmap [:scheme :server-port :uri] %)) combinations)))

(defn- request-string-for [request-map]
  (let [{:keys [scheme server-name server-port uri query-string]} request-map]
    (join [(if (nil? scheme) "" (format "%s://" (name scheme)))
           server-name
           (if (nil? server-port) "" (format ":%s" server-port))
           (if (nil? uri) "" uri)
           (if (nil? query-string) "" (format "?%s" query-string))])))

(defprotocol RouteMatcher
  (matches [route request]))

(extend-protocol RouteMatcher
  String
  (matches [route request]
    (matches (re-pattern (Pattern/quote route)) request))

  Pattern
  (matches [route request]
    (let [request-strings (map request-string-for (potential-alternatives-to request))]
      (some #(re-matches route %) request-strings))))

(defn try-intercept [origfn request]
  (if-let [matching-route (first (filter #(matches (:address %) request) *fake-routes*))]
    (let [route-handler (:handler matching-route)
          response (route-handler request)]
      (assoc response :body (util/utf8-bytes (:body response))))
    (if *in-isolation*
      (throw (Exception. "No matching fake route found to handle request."))
      (origfn request))))

(defn initialise-request-hook []
  (add-hook
   #'clj-http.core/request
   #'try-intercept))

(initialise-request-hook)
