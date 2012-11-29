;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(defproject shafty "0.0.1-SNAPSHOT"
  :description "ClojureScript functional reactive programming library."
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
                                         :pretty-print true}}
                       :examples {:source-path "src/cljs"
                                  :compiler {:output-to "resources/public/examples.js"
                                             :optimizations :whitespace
                                             :pretty-print true}}}
            :test-commands {"integration" ["phantomjs"
                                           "test/integration/runner.coffee"]}})
