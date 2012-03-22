# clj-http-fake [![Maintained Status](http://stillmaintained.com/myfreeweb/clj-http-fake.png)](http://stillmaintained.com/myfreeweb/clj-http-fake)

Basically, [fakeweb](https://github.com/chrisk/fakeweb) in Clojure, for [clj-http](https://github.com/dakrone/clj-http).

## Usage

```clojure
(ns myapp.test.core
  (:require [clj-http.client :as c])
  (:use clj-http.fake))

; also with-fake-routes-in-isolation if you don't want
; to hit the network if no routes match
(with-fake-routes
  {"http://google.com/" (fn [req] {:status 200 :headers {} :body "HACKED LOL HAHA"})
   #"http://([a-z]+).floatboth.com" {:get (fn [req] {:status 200 :headers {} :body "trololo"})}}
  (c/get "http://google.com/"))
```

## License

Released under the MIT License: http://www.opensource.org/licenses/mit-license.php

## [Contributors](https://github.com/myfreeweb/clj-http-fake/contributors)
