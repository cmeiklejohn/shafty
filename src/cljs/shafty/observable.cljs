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
  "Generate observables from browser elements or events."
  (bind! [this]
         "Generate a single binding for one event with the default event
         type.")
  (bind-one! [this] [this event-type] [this event-type value-fn]
             "Generate a single binding for one event.")
  (bind-many! [this] [this event-types] [this event-types value-fn]
         "Generate bindings for many events."))
