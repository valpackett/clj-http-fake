(ns clj-http.test.fake
  (:require [clj-http.client :as http])
  (:use [clj-http.fake]
        [clojure.test]))

(deftest test-executes-fake-with-single-route
  (is (= (with-fake-routes
           {"http://floatboth.com/"
            (fn [req]
              {:status 200 :headers {} :body "hi"})}
           (:body (http/get "http://floatboth.com/"))) "hi")))