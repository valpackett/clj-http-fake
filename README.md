# clj-http-fake

Basically, [fakeweb](https://github.com/chrisk/fakeweb) in Clojure, for [clj-http](https://github.com/dakrone/clj-http).

## Usage

In your ```project.clj``` file:

```clojure
[clj-http-fake "0.4.1"]
```

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
  #"http://([a-z]+).floatboth.com" (fn [req] {:status 200 :headers {} :body "trololo"})
  ;; matches (c/get "http://labs.floatboth.com"), (c/get "http://ringfinger.floatboth.com") and so on, based on regexp.

  ;; Match based an HTTP method:
  "http://shmoogle.com/" {:get (fn [req] {:status 200 :headers {} :body "What is Scmoogle anyways?"})}
  ;; will match only (c/get "http://google.com/")

  ;; Match multiple HTTP methods:
  "http://doogle.com/" {:get    (fn [req] {:status 200 :headers {} :body "Nah, that can't be Google!"})
                        :delete (fn [req] {:status 401 :headers {} :body "Do you think you can delete me?!"})}
 }
 ;; Your tests with requests here
 )
```
## License

Released under [the MIT License](http://www.opensource.org/licenses/mit-license.php).

## [Contributors](https://github.com/myfreeweb/clj-http-fake/contributors)
