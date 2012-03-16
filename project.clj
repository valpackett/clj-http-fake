(defproject clj-http-fake "0.3.0-SNAPSHOT"
  :description "Helper for faking clj-http requests. For testing. You monster."
  :url "https://github.com/tobyclemson/clj-http-fake"
  :license {:name "MIT License"
            :url  "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [clj-http "0.2.3"]
                 [robert/hooke "1.1.2"]]
  :profiles {:dev {:dependencies [[clj-http "0.3.4-SNAPSHOT"]]}
             :clj-http-0.2.3 {:dependencies [[clj-http "0.2.3"]]}
             :clj-http-0.3.3 {:dependencies [[clj-http "0.3.3"]]}
             :clj-http-latest {:dependencies [[clj-http "0.3.4-SNAPSHOT"]]}})