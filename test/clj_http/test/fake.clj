(ns clj-http.test.fake
  (:require [clj-http.client :as http]
            [clj-http.core :as core]
            [clj-http.util :as util])
  (:use [clj-http.fake]
        [clojure.test]
        :reload-all))

(deftest matches-route-exactly
  (is (= (with-fake-routes
           {"http://floatboth.com:2020/path/resource.ext?key=value"
            (fn [request]
              {:status 200 :headers {} :body "29RQPV"})}
           (:body (http/get "http://floatboth.com:2020/path/resource.ext?key=value")))
         "29RQPV")))

(deftest route-contains-default-port-but-request-doesnt
  (is (= (with-fake-routes
           {"http://floatboth.com:80/"
            (fn [request]
              {:status 200 :headers {} :body "3bxkA4"})}
           (:body (http/get "http://floatboth.com/"))) "3bxkA4")))

(deftest request-contains-default-port-but-route-doesnt
  (is (= (with-fake-routes
           {"http://google.com/"
            (fn [request]
              {:status 200 :headers {} :body "z3mwf9"})}
           (:body (http/get "http://google.com:80/"))) "z3mwf9")))

(deftest route-contains-trailing-slash-but-request-doesnt
  (is (= (with-fake-routes
           {"http://google.com/"
            (fn [request]
              {:status 200 :headers {} :body "uAjFYT"})}
           (:body (http/get "http://google.com"))) "uAjFYT")))

(deftest request-contains-trailing-slash-but-route-doesnt
  (is (= (with-fake-routes
           {"http://google.com"
            (fn [request]
              {:status 200 :headers {} :body "R1BWm0"})}
           (:body (http/get "http://google.com/"))) "R1BWm0")))

(deftest request-contains-default-scheme-but-route-doesnt
  (is (= (with-fake-routes
           {"google.com"
            (fn [request]
              {:status 200 :headers {} :body "EDWWO3"})}
           (:body (http/get "http://google.com/"))) "EDWWO3")))

(deftest matching-route-regular-expression
  (is (= (with-fake-routes
           {#"http://google.com/.*?\.html"
            (fn [request]
              {:status 200 :headers {} :body "UrIrHi"})}
           (:body (http/get "http://google.com/index.html"))) "UrIrHi")))
