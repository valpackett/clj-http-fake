# clj-http-fake

[![Build Status](https://travis-ci.org/timgluz/clj-http-fake.svg?branch=update_deps)](https://travis-ci.org/timgluz/clj-http-fake)

Basically, [fakeweb][] in Clojure, for [clj-http][].

## Usage

In your `project.clj` file:

    [clj-http-fake "0.7.8"]

In your namespace declaration:

    (ns myapp.test.core
      (:require [clj-http.client :as c])
      (:use clj-http.fake))

### Basic operations:

    (with-fake-routes {
      ;; Exact string match:
      "http://google.com/apps" (fn [request] {:status 200 :headers {} :body "Hey, do I look like Google.com?"})
      ;; matches (c/get "http://google.com/apps")

      ;; Exact string match with query params:
      "http://google.com/?query=param" (fn [request] {:status 200 :headers {} :body "Nah, that can't be Google!"})
      ;; matches (c/get "http://google.com/" {:query-params {:query "param"}})

      ;; Regexp match:
      #"http://([a-z]+).floatboth.com" (fn [req] {:status 200 :headers {} :body "trololo"})
      ;; matches (c/get "http://labs.floatboth.com"), (c/get "http://ringfinger.floatboth.com") and so on, based on regexp.

      ;; Match based an HTTP method:
      "http://shmoogle.com/" {:get (fn [req] {:status 200 :headers {} :body "What is Scmoogle anyways?"})}
      ;; will match only (c/get "http://google.com/")

      ;; Match multiple HTTP methods:
      "http://doogle.com/" {:get    (fn [req] {:status 200 :headers {} :body "Nah, that can't be Google!"})
                            :delete (fn [req] {:status 401 :headers {} :body "Do you think you can delete me?!"})}

      ;; Match using query params as a map
       {:address "http://google.com/search"
        :query-params {:q "aardark"}} (fn [req] {:status 200 :headers {} :body "Searches have results"}
     }
     ;; Your tests with requests here
     )

## License

Released under [the MIT License][].

## [Contributors]

  [![Build Status]: #
  [fakeweb]: https://github.com/chrisk/fakeweb
  [clj-http]: https://github.com/dakrone/clj-http
  [the MIT License]: http://www.opensource.org/licenses/mit-license.php
  [Contributors]: https://github.com/myfreeweb/clj-http-fake/contributors
