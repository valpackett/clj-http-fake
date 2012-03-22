(ns clj-http.test.fake
  (:require [clj-http.client :as http])
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

(deftest matches-correct-route-when-many-specified
  (is (= (with-fake-routes
           {"http://amazon.com"
            (fn [request]
              {:status 200 :headers {} :body "8jLUY7"})
            "http://google.com"
            (fn [reqeust]
              {:status 200 :headers {} :body "5ttguy"})}
           (:body (http/get "http://google.com"))) "5ttguy")))

(deftest matches-on-method-if-specified
  (is (= (with-fake-routes
           {"http://localhost"
            {:get    (fn [request] {:body "DCiTTN" :status 200 :headers {}})
             :delete (fn [request] {:body "y4Swg8" :status 200 :headers {}})}}
           (:body (http/delete "http://localhost"))) "y4Swg8")))

(deftest matches-any-method-when-specified
  (with-fake-routes
    {"http://example.com"
     {:any (fn [request] {:body "wp8gJf" :status 200 :headers {}})}}
    (is (= (:body (http/get "http://example.com")) "wp8gJf"))
    (is (= (:body (http/delete "http://example.com")) "wp8gJf"))))

(deftest matches-any-method-when-no-method-specified
  (with-fake-routes
    {"http://example.com"
     (fn [request] {:body "FyLNcb" :status 200 :headers {}})}
    (is (= (:body (http/get "http://example.com")) "FyLNcb"))
    (is (= (:body (http/delete "http://example.com")) "FyLNcb"))))

(deftest uses-first-matching-route-if-many-possible-matches
  (is (= (with-fake-routes
           {"http://localhost"
            (fn [request] {:body "mKmfyH" :status 200 :headers {}})
            "http://localhost/"
            (fn [request] {:body "rFGWGr" :status 200 :headers {}})}
           (:body (http/get "http://localhost/"))) "mKmfyH")))