(ns clj-http.test.fake
  (:require [clj-http.client :as http])
  (:use [clj-http.fake]
        [clojure.test]))

(deftest test-executes-fake-when-matches-route-exactly
  (is (= (with-fake-routes
           {"http://floatboth.com/"
            (fn [request]
              {:status 200 :headers {} :body "response"})}
           (:body (http/get "http://floatboth.com/"))) "response")))

(deftest test-executes-fake-when-route-contains-default-port-but-request-doesnt
  (is (= (with-fake-routes
           {"http://floatboth.com:80/"
            (fn [request]
              {:status 200 :headers {} :body "response"})}
           (:body (http/get "http://floatboth.com/"))))))