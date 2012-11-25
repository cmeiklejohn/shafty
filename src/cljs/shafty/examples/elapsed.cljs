;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
;; Elapsed timer example.
;;
;; Example similar to the example in Section 2.2 of the Flapjax paper.
;;
;; var nowB = timerB(1000);
;; var startTm = nowB.valueNow();
;; var clickTmsB = $E("reset", "click").snapshotE(nowB)
;;                 .startsWith(startTm);
;; var elapsedB = nowB - clickTmsB;
;; insertValueB(elapsedB, "curTime", "innerHTML");
;;
(ns shafty.examples.elapsed
  (:use [shafty.observable :only [event!]]
        [shafty.event-stream :only [merge! map! snapshot!]]
        [shafty.behaviour-conversion :only [hold!]]
        [shafty.liftable :only [lift!]]
        [shafty.renderable :only [insert!]]
        [shafty.timer :only [timer!]])
  (:require [goog.dom :as dom]))

(defn- timer []
  "Generate a timer, and convert the timer into a behaviour."
  (-> (timer! 1000 (fn [] (js/Date.)))
      (map! (fn [x] (.log js/console "Timer ticked.") x))
      (hold! (js/Date.))))

(defn- reset [timer]
  "Generate a behaviour originating from click events on the reset
  button.  When clicked, snapshot the current state of the timer."
  (-> (event! (dom/getElement "reset-button") "click")
      (snapshot! timer)
      (map! (fn [x] (.log js/console "Reset button clicked.") x))
      (hold! (js/Date.))))

(defn main []
  "Run the elapsed time example."

  (let [the-timer (timer)
        reset-button (reset the-timer)]
    (-> (lift! the-timer (fn [x] (- x (deref reset-button))))
        (insert! (dom/getElement "elapsed"))))

  (.log js/console "Starting the elapsed time example."))
