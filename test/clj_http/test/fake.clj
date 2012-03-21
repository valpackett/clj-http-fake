(ns clj-http.test.fake
  (:require [clj-http.client :as http])
  (:use [clj-http.fake]
        [clojure.test]))

(deftest matches-route-exactly
  (is (= (with-fake-routes
           [{:address "http://floatboth.com:2020/path/resource.ext?key=value"
              :handler (fn [request] {:status 200 :headers {} :body "29RQPV"})}]
           (:body (http/get "http://floatboth.com:2020/path/resource.ext?key=value")))
         "29RQPV")))

(deftest route-contains-default-port-but-request-doesnt
  (is (= (with-fake-routes
           [{:address "http://floatboth.com:80/"
             :handler (fn [request] {:status 200 :headers {} :body "3bxkA4"})}]
           (:body (http/get "http://floatboth.com/"))) "3bxkA4")))

(deftest request-contains-default-port-but-route-doesnt
  (is (= (with-fake-routes
           [{:address "http://google.com/"
             :handler (fn [request] {:status 200 :headers {} :body "z3mwf9"})}]
           (:body (http/get "http://google.com:80/"))) "z3mwf9")))

(deftest route-contains-trailing-slash-but-request-doesnt
  (is (= (with-fake-routes
           [{:address "http://google.com/"
             :handler (fn [request] {:status 200 :headers {} :body "uAjFYT"})}]
           (:body (http/get "http://google.com"))) "uAjFYT")))

(deftest request-contains-trailing-slash-but-route-doesnt
  (is (= (with-fake-routes
           [{:address "http://google.com"
             :handler (fn [request] {:status 200 :headers {} :body "R1BWm0"})}]
           (:body (http/get "http://google.com/"))) "R1BWm0")))

(deftest request-contains-default-scheme-but-route-doesnt
  (is (= (with-fake-routes
           [{:address "google.com"
             :handler (fn [request] {:status 200 :headers {} :body "EDWWO3"})}]
           (:body (http/get "http://google.com/"))) "EDWWO3")))

(deftest matching-route-regular-expression
  (is (= (with-fake-routes
           [{:address #"http://google.com/.*?\.html"
             :handler (fn [request] {:status 200 :headers {} :body "UrIrHi"})}]
           (:body (http/get "http://google.com/index.html"))) "UrIrHi")))

(deftest matches-correct-route-when-many-specified
  (is (= (with-fake-routes
           [{:address "http://amazon.com"
             :handler (fn [request] {:status 200 :headers {} :body "8jLUY7"})}
            {:address "http://google.com"
             :handler (fn [reqeust] {:status 200 :headers {} :body "5ttguy"})}]
           (:body (http/get "http://google.com"))) "5ttguy")))

(deftest matches-on-method-if-specified
  (is (= (with-fake-routes
           [{:method :get
             :address "http://localhost"
             :handler (fn [request] {:body "DCiTTN" :status 200 :headers {}})}
            {:method :delete
             :address "http://localhost"
             :handler (fn [request] {:body "y4Swg8" :status 200 :headers {}})}]
           (:body (http/delete "http://localhost"))) "y4Swg8")))

(deftest matches-any-method-when-specified
  (with-fake-routes
    [{:method :any
      :address "http://example.com"
      :handler (fn [request] {:body "wp8gJf" :status 200 :headers {}})}]
    (is (= (:body (http/get "http://example.com")) "wp8gJf"))
    (is (= (:body (http/delete "http://example.com")) "wp8gJf"))))

(deftest matches-any-method-when-no-method-specified
  (with-fake-routes
    [{:address "http://example.com"
      :handler (fn [request] {:body "FyLNcb" :status 200 :headers {}})}]
    (is (= (:body (http/get "http://example.com")) "FyLNcb"))
    (is (= (:body (http/delete "http://example.com")) "FyLNcb"))))

(deftest uses-first-matching-route-if-many-possible-matches
  (is (= (with-fake-routes
           [{:method :get
             :address "http://localhost"
             :handler (fn [request] {:body "mKmfyH" :status 200 :headers {}})}
            {:method :get
             :address "http://localhost"
             :handler (fn [request] {:body "rFGWGr" :status 200 :headers {}})}]
           (:body (http/get "http://localhost"))) "mKmfyH")))