(ns shafty.observable
  (:require [shafty.event :as event]
            [goog.dom :as dom]
            [goog.events :as events]))

(defprotocol Observable
  "Generate observables from browser elements or events."
  (bind! [this x]
         "Bind a given browser event or element."))

(extend-type js/Node
  Observable
  (bind! [this event]
    (let [e (event/event)]
      (events/listen this event (fn [ev]
                                  (-notify-watches e nil ev))) e)))
(defn bind-timer! [interval]
  "Generate a new timer at a given interval, and bind to a new event
  stream."
  (let [e (event/event)]
    (js/setInterval (fn [] (-notify-watches e nil (js/Date))) interval) e))
