(ns clj-http.test.fake
  (:require [clj-http.client :as http]
            [clj-http.core :as core]
            [clj-http.util :as util])
  (:use [clj-http.fake]
        [clojure.test]
        :reload-all)
  (:import (java.net ConnectException)))


(deftest many-qparams-performance-test
  (is (= "29RQPV"
         (let [num-qparams 10]
           (with-fake-routes
             {#"http://test/\?.*"
              (fn [request]
                {:status 200 :headers {} :body "29RQPV"})}
             (:body (http/request
                      {:url "http://test/"
                       :query-params (zipmap (map str (range 0 num-qparams))
                                             (range 0 num-qparams))
                       :method :get})))))))

(deftest matches-route-exactly
  (is (= "29RQPV"
         (with-fake-routes
           {"http://floatboth.com:2020/path/resource.ext?key=value"
            (fn [request]
              {:status 200 :headers {} :body "29RQPV"})}
           (:body (http/get "http://floatboth.com:2020/path/resource.ext?key=value"))))))

(deftest route-contains-default-port-but-request-doesnt
  (is (= "3bxkA4"
         (with-fake-routes
           {"http://floatboth.com:80/"
            (fn [request]
              {:status 200 :headers {} :body "3bxkA4"})}
           (:body (http/get "http://floatboth.com/"))))))

(deftest request-contains-default-port-but-route-doesnt
  (is (= "z3mwf9"
         (with-fake-routes
           {"http://google.com/"
            (fn [request]
              {:status 200 :headers {} :body "z3mwf9"})}
           (:body (http/get "http://google.com:80/"))))))

(deftest route-contains-trailing-slash-but-request-doesnt
  (is (= "uAjFYT"
         (with-fake-routes
           {"http://google.com/"
            (fn [request]
              {:status 200 :headers {} :body "uAjFYT"})}
           (:body (http/get "http://google.com"))))))

(deftest request-contains-trailing-slash-but-route-doesnt
  (is (= "R1BWm0"
         (with-fake-routes
           {"http://google.com"
            (fn [request]
              {:status 200 :headers {} :body "R1BWm0"})}
           (:body (http/get "http://google.com/"))))))

(deftest request-contains-default-scheme-but-route-doesnt
  (is (= "EDWWO3"
         (with-fake-routes
           {"google.com"
            (fn [request]
              {:status 200 :headers {} :body "EDWWO3"})}
           (:body (http/get "http://google.com/"))))))

(deftest matching-route-regular-expression
  (is (= "UrIrHi"
         (with-fake-routes
           {#"http://google.com/.*?\.html"
            (fn [request]
              {:status 200 :headers {} :body "UrIrHi"})}
           (:body (http/get "http://google.com/index.html"))))))

(deftest matches-correct-route-when-many-specified
  (is (= "5ttguy"
         (with-fake-routes
           {"http://amazon.com"
            (fn [request]
              {:status 200 :headers {} :body "8jLUY7"})
            "http://google.com"
            (fn [reqeust]
              {:status 200 :headers {} :body "5ttguy"})}
           (:body (http/get "http://google.com"))))))

(deftest matches-on-method-if-specified
  (is (= "y4Swg8"
         (with-fake-routes
           {"http://localhost"
            {:get    (fn [request] {:body "DCiTTN" :status 200 :headers {}})
             :delete (fn [request] {:body "y4Swg8" :status 200 :headers {}})}}
           (:body (http/delete "http://localhost"))))))

(deftest matches-any-method-when-specified
  (with-fake-routes
    {"http://example.com"
     {:any (fn [request] {:body "wp8gJf" :status 200 :headers {}})}}
    (is (= "wp8gJf" (:body (http/get "http://example.com"))))
    (is (= "wp8gJf" (:body (http/delete "http://example.com"))))))

(deftest matches-any-method-when-no-method-specified
  (with-fake-routes
    {"http://example.com"
     (fn [request] {:body "FyLNcb" :status 200 :headers {}})}
    (is (= "FyLNcb" (:body (http/get "http://example.com"))))
    (is (= "FyLNcb" (:body (http/delete "http://example.com"))))))

(deftest uses-first-matching-route-if-many-possible-matches
  (is (= "mKmfyH"
         (with-fake-routes
           {"http://localhost"
            (fn [request] {:body "mKmfyH" :status 200 :headers {}})
            "http://localhost/"
            (fn [request] {:body "rFGWGr" :status 200 :headers {}})}
           (:body (http/get "http://localhost/"))))))

(deftest falls-through-to-real-request-method-if-no-matching-route
  (with-redefs [clj-http.core/request
                (fn [req]
                  {:status 200 :headers {} :body (util/utf8-bytes "zgBOaC")})]
    (initialize-request-hook)
    (with-fake-routes
      {"http://idontmatch.com" (fn [req] {:status 200 :headers {} :body "wp8gJf"})}
      (is (= "zgBOaC" (:body (http/get "http://somerandomhost.org")))))))

(deftest throws-exception-if-in-isolation-mode-and-no-matching-route
  (with-redefs [clj-http.core/request
                (fn [req]
                  {:status 200 :headers {} :body (util/utf8-bytes "1Z6xAB")})]
    (initialize-request-hook)
    (with-fake-routes-in-isolation
      {"http://idontmatch.com"
       (fn [req]
         {:status 200 :headers {} :body "lL4QSc"})}
      (is (thrown? Exception (http/get "http://somerandomhost.org"))))))

(defmacro other-thread
  "Mostly like future but fails to preserve thread-local bindings."
  [& body]
  `(let [p# (promise)
         t# (new Thread (fn [] (deliver p# (do ~@body))))]
     (.start t#)
     p#))

(deftest requesting-on-different-thread-test
  (is (= "29RQPV"
         (with-global-fake-routes
           {"http://floatboth.com:2020/path/resource.ext?key=value"
            (fn [request]
              {:status 200 :headers {} :body "29RQPV"})}
           @(other-thread (:body (http/get "http://floatboth.com:2020/path/resource.ext?key=value")))))))

(deftest get-request-contains-empty-query-params
  (is (= "that's my foo bar"
         (with-fake-routes-in-isolation
           {#".*/foo/bar" (constantly {:status 200 :headers {} :body "that's my foo bar"})}
           (:body (http/get "http://floatboth.com/achey/breaky/foo/bar" {:query-params {}}))))))

(deftest request-contains-query-params
  (is (= "4XbAfG"
         (with-fake-routes
           {"http://google.com/?test=test"
            (fn [request]
              {:status 200 :headers {} :body "4XbAfG"})}
           (:body (http/get "http://google.com/" {:query-params {:test "test"}}))))))

(deftest request-contains-form-params
  (is (= "test=4XbAfG"
         (with-fake-routes
           {"http://google.com/"
            (fn [request]
              {:status 200 :headers {} :body (slurp (:body request))})}
           (:body (http/post "http://google.com/" {:form-params {:test "4XbAfG"}}))))))

(deftest request-query-param-order-does-not-matter
  (is (= "ASd0gf"
         (with-fake-routes
           {"http://google.com/?fst=test1&sec=test2"
            (fn [request]
              {:status 200 :headers {} :body "ASd0gf"})}
           (:body (http/get "http://google.com/" {:query-params {:fst "test1",
                                                                 :sec "test2"}})))))
  (is (= "oDKL13"
         (with-fake-routes
           {"http://google.com/?sec=test2&fst=test1"
            (fn [request]
              {:status 200 :headers {} :body "oDKL13"})}
           (:body (http/get "http://google.com/" {:query-params {:fst "test1",
                                                                 :sec "test2"}})))))
  (is (= "BXC9ai"
         (with-fake-routes
           {"http://google.com/?fst=test1&sec=test2"
            (fn [request]
              {:status 200 :headers {} :body "BXC9ai"})}
           (:body (http/get "http://google.com/" {:query-params {:sec "test2",
                                                                 :fst "test1"}})))))
  (is (= "91nOjA"
         (with-fake-routes
           {"http://google.com/?sec=test2&fst=test1"
            (fn [request]
              {:status 200 :headers {} :body "91nOjA"})}
           (:body (http/get "http://google.com/" {:query-params {:sec "test2",
                                                                 :fst "test1"}}))))))

(deftest query-params-specified-as-map
  (is (= "anteater"
         (with-fake-routes-in-isolation
           {{:address "http://google.com/search"
             :query-params {:q "aardvark"}}
            (fn [request]
              {:status 200 :headers {} :body "anteater"})}
           (:body (http/get "http://google.com/search" {:query-params {:q "aardvark"}})))))

  (is (= "anteater"
         (with-fake-routes-in-isolation
           {{:address #"http://google.com/[abc]{3}"
             :query-params {:q "aardvark"}}
            (fn [request]
              {:status 200 :headers {} :body "anteater"})}
           (:body (http/get "http://google.com/aab" {:query-params {:q "aardvark"}})))))

  (testing "with spaces in the query params"
    (is (= "anteater"
           (with-fake-routes-in-isolation
             {{:address "http://google.com/search"
               :query-params {:q "this has spaces"}}
              (fn [request]
                {:status 200 :headers {} :body "anteater"})}
             (:body (http/get "http://google.com/search"
                              {:query-params {:q "this has spaces"}})))))))

(deftest get-as-byte-array
  (let [body (.getBytes "anteater")]
    (is (= (seq body)
           (seq (with-fake-routes-in-isolation
                  {{:address #"http://google.com/[abc]{3}"
                    :query-params {:q "aardvark"}}
                   (fn [request]
                     {:status 200 :headers {} :body body})}
                  (:body (http/get "http://google.com/aab"
                                   {:as :byte-array
                                    :query-params {:q "aardvark"}}))))))))

(deftest response-map-default-fields
  (testing "if no :body is given, the body is empty"
    (is (= ""
           (with-fake-routes {"http://google.com/" (constantly {:status 200})}
             (:body (http/get "http://google.com/"))))))

  (testing "if no :status is given, the it is assumed to be 200"
    (is (= 200
           (with-fake-routes {"http://google.com/" (constantly {:body "OK"})}
             (:status (http/get "http://google.com/"))))))

  (testing "defaults when both :status and :body are missing"
    (let [response (with-fake-routes {"http://google.com/" (constantly {})}
                     (http/get "http://google.com/"))]
      (is (= 200 (:status response)))
      (is (= "" (:body response))))))

(defn- supports-async?
  []
  (> (count (:arglists (meta #'clj-http.core/request))) 1))

(deftest respond-and-raise
  (when (supports-async?)
    (let [body (.getBytes "OK")]
     (with-fake-routes-in-isolation
       {"http://google.com/"  (constantly {:body body})
        "http://google2.com/" (fn [_]
                                (throw (ConnectException.)))}

       (testing "if there is no exception, respond is called"
         (let [val (atom nil)]
           (http/get "http://google.com/"
                     {:as :byte-array :async? true}
                     (partial reset! val)
                     (partial reset! val))
           (is (= (seq body) (seq (:body @val))))))

       (testing "if there is an exception, raise is called"
         (let [val (atom nil)]
           (http/get "http://google2.com/"
                     {:as :byte-array :async? true}
                     (partial reset! val)
                     (partial reset! val))
           (is (instance? ConnectException @val))))

       (testing "if route is unavailable, exception is thrown"
         (let [val (atom nil)]
           (is (thrown? Exception
                        (http/get "http://somerandomhost.com/"
                                  {:as :byte-array :async? true}
                                  (partial reset! val)
                                  (partial reset! val))))
           (is (nil? @val))))))))
