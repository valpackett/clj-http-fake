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
  `(let [s# ~routes]
    (assert (map? s#))
    (binding [*fake-routes* s#]
      ~@body)))

(defmacro with-global-fake-routes-in-isolation
  [routes & body]
  `(with-redefs [*in-isolation* true]
     (with-global-fake-routes ~routes ~@body)))

(defmacro with-global-fake-routes
  [routes & body]
  `(let [s# ~routes]
     (assert (map? s#))
     (with-redefs [*fake-routes* s#]
       ~@body)))

(defn- defaults-or-value [defaults value]
  (if (contains? defaults value) (reverse (vec defaults)) (vector value)))

(defn- potential-server-ports-for [request-map]
  (defaults-or-value #{80 nil} (:server-port request-map)))

(defn- potential-uris-for [request-map]
  (defaults-or-value #{"/" "" nil} (:uri request-map)))

(defn- potential-schemes-for [request-map]
  (defaults-or-value #{:http nil} (keyword (:scheme request-map))))

(defn- potential-query-strings-for [request-map]
  (defaults-or-value #{"" nil} (:query-string request-map)))

(defn- potential-alternatives-to [request]
  (let [schemes       (potential-schemes-for       request)
        server-ports  (potential-server-ports-for  request)
        uris          (potential-uris-for          request)
        query-strings (potential-query-strings-for request)
        combinations  (cartesian-product schemes server-ports uris query-strings)]
    (map #(merge request (zipmap [:scheme :server-port :uri :query-string] %)) combinations)))

(defn- address-string-for [request-map]
  (let [{:keys [scheme server-name server-port uri query-string]} request-map]
    (join [(if (nil? scheme)       "" (format "%s://" (name scheme)))
           server-name
           (if (nil? server-port)  "" (format ":%s"   server-port))
           (if (nil? uri)          "" uri)
           (if (nil? query-string) "" (format "?%s"   query-string))])))

(defprotocol RouteMatcher
  (matches [address method request]))

(extend-protocol RouteMatcher
  String
  (matches [address method request]
    (matches (re-pattern (Pattern/quote address)) method request))

  Pattern
  (matches [address method request]
    (let [request-method (:request-method request)
          address-strings (map address-string-for (potential-alternatives-to request))]
      (and (contains? (set (distinct [:any request-method])) method)
           (some #(re-matches address %) address-strings)))))

(defn- flatten-routes [routes]
  (let [normalised-routes
        (reduce
         (fn [accumulator [address handlers]]
           (if (map? handlers)
             (into accumulator (map (fn [[method handler]] [method address handler]) handlers))
             (into accumulator [[:any address handlers]])))
         []
         routes)]
    (map #(zipmap [:method :address :handler] %) normalised-routes)))

(defn try-intercept [origfn request]
  (if-let [matching-route
           (first
            (filter
             #(matches (:address %) (:method %) request)
             (flatten-routes *fake-routes*)))]
    (let [route-handler (:handler matching-route)
          response (route-handler request)]
      (assoc response :body (util/utf8-bytes (:body response))))
    (if *in-isolation*
      (throw (Exception.
              (apply format
               "No matching fake route found to handle request. Reuqest details: \n\t%s \n\t%s \n\t%s \n\t%s \n\t%s "
               (select-keys request [:scheme :request-method :server-name :uri :query-string]))))
      (origfn request))))

(defn initialize-request-hook []
  (add-hook
   #'clj-http.core/request
   #'try-intercept))

(initialize-request-hook)