;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.observable)

(defprotocol Observable
  "Generate events or behaviours from browser elements or events."
  (behaviour! [this initial]
              "Generate a behaviour from a browser element.")
  (event! [this event-type] [this event-type value-fn]
          "Generate an event from a DOM event.")
  (events! [this event-types] [this event-type value-fn]
          "Generate an event from DOM events."))
