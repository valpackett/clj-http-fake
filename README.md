# clj-http-fake [![Maintained Status](http://stillmaintained.com/myfreeweb/clj-http-fake.png)](http://stillmaintained.com/myfreeweb/clj-http-fake)

Basically, [fakeweb](https://github.com/chrisk/fakeweb) in Clojure, for [clj-http](https://github.com/dakrone/clj-http).

## Usage

```clojure
(ns myapp.test.core
  (:require [clj-http.client :as c])
  (:use clj-http.fake))

(with-fake-routes
  ; also supports regexps
  {"http://google.com/" (fn [req] {:status 200 :headers {} :body "HACKED LOL HAHA"})}
  (c/get "http://google.com/"))
```

## License

Released under the MIT License: http://www.opensource.org/licenses/mit-license.php
