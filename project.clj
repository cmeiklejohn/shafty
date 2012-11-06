(defproject shafty "0.0.1-SNAPSHOT"
  :description "Prototype ClojureScript functional reactive programming library."
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[lein-cljsbuild "0.2.7"]]
  :source-paths ["src/clj" "src/cljs"]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds {:test {:source-path "test/integration"
                              :compiler {:output-to "resources/private/integration.js"
                                         :optimizations :whitespace
                                         :pretty-print true}}
                       :core {:source-path "src/cljs"
                              :compiler {:output-to "resources/private/core.js"
                                         :optimizations :whitespace
                                         :pretty-print true}}}
            :test-commands {"integration" ["phantomjs"
                                           "test/integration/runner.coffee"]}})
