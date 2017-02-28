# clj-http-fake [![Build Status](https://img.shields.io/travis/myfreeweb/clj-http-fake.svg?style=flat)](https://travis-ci.org/myfreeweb/clj-http-fake) [![MIT License](https://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](https://www.tldrlegal.com/l/mit) [![Clojars Project](https://img.shields.io/clojars/v/clj-http-fake.svg)](https://clojars.org/clj-http-fake)

Basically, [fakeweb](https://github.com/chrisk/fakeweb) in Clojure, for [clj-http](https://github.com/dakrone/clj-http).

## Usage

```clojure
(ns myapp.test.core
  (:require [clj-http.client :as c])
  (:use clj-http.fake))
```

The public interface consists of macros:

* ``with-fake-routes`` - lets you override clj-http requests that match keys in the provided map
* ``with-fake-routes-in-isolation`` - does the same but throws if a request does not match any key
* ``with-global-fake-routes``
* ``with-global-fake-routes-in-isolation``

'Global' counterparts use ``with-redefs`` instead of ``binding`` internally so they can be used in
a multi-threaded environment.


### Examples

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

   ;; If not given, the fake response status will be 200 and the body will be "".
   "https://duckduckgo.com/?q=ponies" (constantly {})}

 ;; Your tests with requests here
 )
```

## Development

Use [Leiningen](https://leiningen.org) **with profiles**. E.g.:

```
$ lein with-profile +latest-2.x,+1.8 repl
```

There are aliases to run the tests with the oldest and newest supported versions of clj-http:

```
$ lein test-3.x  # Testing under clj-http 3.x
$ lein test-2.x  # Testing under clj-http 2.x
$ lein test-oldest  # Testing under clj-http 0.7.8... Anyone still using that?
```

## License

Released under [the MIT License](http://www.opensource.org/licenses/mit-license.php).

## Contributing

Please feel free to submit pull requests!

By participating in this project you agree to follow the [Contributor Code of Conduct](http://contributor-covenant.org/version/1/4/).

[The list of contributors is available on GitHub](https://github.com/myfreeweb/clj-http-fake/graphs/contributors).
