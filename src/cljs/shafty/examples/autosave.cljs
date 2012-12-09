;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
;; Autosave example.
;;
;; Example similar to the example in Section 2.3 of the Flapjax paper.
;;
(ns shafty.examples.autosave
  (:use [shafty.event-stream :only [merge! map! snapshot!]]
        [shafty.observable :only [event! behaviour!]]
        [shafty.requestable :only [requests!]]
        [shafty.renderable :only [insert!]]
        [shafty.timer :only [timer!]]
        [clojure.browser.dom :only [get-element]]))

(defn- build-request [value]
  "Generate a request object."
  { :url "/save" :data { :value value } :method "post" })

(defn- live-content []
  "Generate a behaviour for the live content area."
  (behaviour! (get-element "live-content") nil))

(defn- timer []
  "Generate a timer."
  (-> (timer! 10000 (fn [] (js/Date.)))
      (map! (fn [x] (.log js/console "Timer ticked.") x))))

(defn main []
  "Run the autosave example."

  (-> (event! (get-element "save-button") "click")
      (map! (fn [x] (.log js/console "Button clicked.") x))
      (merge! (timer))
      (snapshot! (live-content))
      (map! build-request)
      (requests!))

  (.log js/console "Starting the autosave example."))
