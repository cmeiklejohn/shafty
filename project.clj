;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(defproject shafty "0.0.2-SNAPSHOT"
  :description "ClojureScript functional reactive programming library."
  :url "https://github.com/cmeiklejohn/shafty"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.5.0"]]
  :plugins [[lein-cljsbuild "0.3.0"]]
  :source-paths ["src/clj" "src/cljs"]
  :hooks [leiningen.cljsbuild]
  :cljsbuild
    {:builds
     [{:source-paths ["src/cljs"],
       :id "core",
       :compiler
       {:pretty-print true,
        :output-to "resources/private/core.js",
        :optimizations :whitespace}}
      ]})
