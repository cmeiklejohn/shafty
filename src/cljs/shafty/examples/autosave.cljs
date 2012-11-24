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
;; Autosave example.
;;
;; Example similar to the example in Section 2.3 of the Flapjax paper.
;;
(ns shafty.examples.autosave
  (:use [shafty.observable :only [bind! bind-timer!]]
        [shafty.event-stream :only [merge! map! snapshot!]])
  (:require [goog.dom :as dom]))

(defn- perform-save []
  "Perform the save."

  (let [element (dom/getElement "save-status")
        curtime (js/Date)]
    (set! (.-innerHTML element) (str "Last save at " curtime))))

(defn- make-request [value]
  "Generate a request object."
  { :url "/save" :fields { :value value } :request "post" })

(defn main []
  "Run the autosave example."

  (-> (bind! (dom/getElement "save-button") "click")
      (merge! (bind-timer! 10000))
      (snapshot! (bind! (dom/getElement "live-content")))
      (map! make-request)))
