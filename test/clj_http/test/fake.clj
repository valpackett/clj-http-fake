(ns clj-http.test.fake
  (:require [clj-http.client :as c])
  (:use clj-http.fake, clojure.test :reload-all))

(deftest t-fake
  (is (= (with-fake-routes
            {"http://floatboth.com/" (fn [req] {:status 200 :headers {} :body "hi"})}
            (:body (c/get "http://floatboth.com/"))) "hi")))