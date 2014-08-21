(defproject little-couch "0.1.0"
  :description "Simple Clojure interface to CouchDB"
  :url "https://github.com/obi-a/little-couch"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"][cheshire "5.3.1"][clj-http "0.9.2"]]
  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.4"]]}}
  :signing {:gpg-key "obi.akubue@gmail.com"}
  :main ^:skip-aot little-couch.core)
