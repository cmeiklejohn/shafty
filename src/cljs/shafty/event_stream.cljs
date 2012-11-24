;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0
;; (http://opensource.org/licenses/eclipse-1.0.php) which can be found
;; in the file LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.event-stream)

(defprotocol EventStream
  "The composable stream interface provides a series of filtering and
  selection methods for working with objects as they enter and leave the
  event stream."
  (propagate! [this value]
              "Propagate the value to all of the sinks.")
  (filter! [this filter-fn]
           "Filter the objects of the event stream using the provided
           filter-fn.")
  (map! [this map-fn]
           "Map the objects of the event stream using the provided
           map-fn.")
  (merge! [this that]
          "Merge supplied event stream and other event stream into one
          event stream")
  (delay! [this interval]
          "Delay propagation for interval.")
  (snapshot! [this that]
             "Generate a new event stream, which will contain the result
             of snapshotting a beahviour for every event occurrence."))
