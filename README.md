# clj-http-fake [![Build Status](https://img.shields.io/travis/myfreeweb/clj-http-fake.svg?style=flat)](https://travis-ci.org/myfreeweb/clj-http-fake) [![MIT License](https://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](https://www.tldrlegal.com/l/mit)

Basically, [fakeweb](https://github.com/chrisk/fakeweb) in Clojure, for [clj-http](https://github.com/dakrone/clj-http).

## Usage

In your ```project.clj``` file:

[![Clojars Project](http://clojars.org/clj-http-fake/latest-version.svg)](http://clojars.org/clj-http-fake)

In your namespace declaration:

```clojure
(ns myapp.test.core
  (:require [clj-http.client :as c])
  (:use clj-http.fake))
```

### Basic operations:

```clojure
(with-fake-routes {
  ;; Exact string match:
  "http://google.com/apps" (fn [request] {:status 200 :headers {} :body "Hey, do I look like Google.com?"})
  ;; matches (c/get "http://google.com/apps")

  ;; Exact string match with query params:
  "http://google.com/?query=param" (fn [request] {:status 200 :headers {} :body "Nah, that can't be Google!"})
  ;; matches (c/get "http://google.com/" {:query-params {:query "param"}})

  ;; Regexp match:
  #"https://([a-z]+).unrelenting.technology" (fn [req] {:status 200 :headers {} :body "Hello world"})
  ;; matches (c/get "https://labs.unrelenting.technology"), (c/get "https://server.unrelenting.technology") and so on, based on regexp.

  ;; Match based an HTTP method:
  "http://shmoogle.com/" {:get (fn [req] {:status 200 :headers {} :body "What is Scmoogle anyways?"})}
  ;; will match only (c/get "http://google.com/")

  ;; Match multiple HTTP methods:
  "http://doogle.com/" {:get    (fn [req] {:status 200 :headers {} :body "Nah, that can't be Google!"})
                        :delete (fn [req] {:status 401 :headers {} :body "Do you think you can delete me?!"})}

  ;; Match using query params as a map
   {:address "http://google.com/search"
    :query-params {:q "aardark"}} (fn [req] {:status 200 :headers {} :body "Searches have results"})
 }
 ;; Your tests with requests here
 )
```

## Development

Use [Leiningen](http://leiningen.org) with profiles.

There are aliases to run the tests with the oldest and newest supported versions of clj-http:

```
$ lein test-newest
$ lein test-oldest
```

## License

Released under [the MIT License](http://www.opensource.org/licenses/mit-license.php).

## [Contributors](https://github.com/myfreeweb/clj-http-fake/contributors)
