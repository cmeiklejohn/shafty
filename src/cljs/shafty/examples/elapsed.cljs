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
  (:use [shafty.event-stream          :only [merge! map! snapshot!]]
        [shafty.behaviour-conversion  :only [hold!]]
        [shafty.observable            :only [event!]]
        [shafty.requestable           :only [requests!]]
        [shafty.renderable            :only [insert!]]
        [shafty.liftable              :only [lift! lift2!]]
        [shafty.timer                 :only [timer!]]
        [clojure.browser.dom          :only [get-element]]))

(defn- timer []
  "Generate a timer, and convert the timer into a behaviour."
  (-> (timer! 1000 (fn [] (js/Date.)))
      (map! (fn [x] (.log js/console (str "Timer ticked at: " x)) x))
      (hold! (js/Date.))))

(defn- reset [timer]
  "Generate a behaviour originating from click events on the reset
  button.  When clicked, snapshot the current state of the timer."
  (-> (event! (get-element "reset-button") "click")
      (snapshot! timer)
      (map! (fn [x] (.log js/console (str "Reset button clicked at: " x)) x))
      (hold! (js/Date.))))

(defn main []
  "Run the elapsed time example."
  (let [the-timer (timer)
        reset-button (reset the-timer)]
    (-> (lift2! the-timer reset-button (fn [now click] (- now click)) 0)
        (insert! (get-element "elapsed"))))
  (.log js/console "Starting the elapsed time example."))
