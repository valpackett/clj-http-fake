(defproject clj-http-fake "0.2.3-SNAPSHOT"
  :description "Helper for faking clj-http requests. For testing. You monster."
  :url "https://github.com/myfreeweb/clj-http-fake"
  :license {:name "MIT License"
            :url  "http://www.opensource.org/licenses/mit-license.php"
            :distribution :repo}
  :autodoc {:name "clj-http-fake"
            :page-title "clj-http-fake API docs"
            :web-src-dir "https://github.com/myfreeweb/clj-http-fake/blob/"
            :web-home "http://myfreeweb.github.com/clj-http-fake/"
            :output-path "autodoc"
            :root "."
            :source-path ""
            :load-except-list [#"test/" #"project\.clj"]}
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [clj-http "0.2.3"]
                 [robert/hooke "1.1.2"]]
  :dev-dependencies [[org.clojars.weavejester/autodoc "0.9.0"]])