(defproject clj-http-fake "1.0.2-SNAPSHOT"
  :description "Helper for faking clj-http requests. For testing. You monster."
  :url "https://github.com/myfreeweb/clj-http-fake"
  :license {:name "MIT License"
            :url  "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/math.combinatorics "0.1.1"]
                 [robert/hooke "1.3.0"]
                 [clj-http "2.0.0"]]    ;; Needed here or lein might not compile clj-http first, resulting in broken builds
  :aliases {"test-newest" ["with-profile" "newest,1.5:newest,1.6:newest,1.7" "test"]
            "test-oldest" ["with-profile" "oldest,1.5:oldest,1.6:oldest,1.7" "test"]}
  :profiles {:1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             ;;the newest supported version of clj-http
             :newest {:dependencies [[clj-http "2.0.0"]]}
             :oldest {:dependencies [[clj-http "0.7.8"]]}})
