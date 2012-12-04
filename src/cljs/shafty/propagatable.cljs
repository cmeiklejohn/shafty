;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.propagatable)

(defprotocol Propagatable
  "Provides a mechanism for propogating events and values."
  (propagate! [this value]
              "Propagate the value to all of the sinks.")
  (send! [this value]
         "Notify object of a new value.")
  (add-sink! [this that]
             "Add a sink for propagation."))
