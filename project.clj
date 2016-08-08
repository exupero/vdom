(defproject vdom "0.2.0-SNAPSHOT"
  :description "Virtual-dom library"
  :url "https://github.com/exupero/vdom"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89" :exclusions [org.apache.ant/ant]]
                 [org.clojure/core.async "0.2.385"]]
  :source-paths ["src"])
