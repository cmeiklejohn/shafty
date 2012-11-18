(ns shafty.core
  (:require [shafty.event :as event]
            [goog.events :as events]
            [goog.dom :as dom]))

(defn bind-timer! [interval]
  "Generate a new timer at a given interval, and bind to a new event
  stream."
  (let [e (event/event)]
    (js/setInterval (fn [] (-notify-watches e nil (js/Date))) 1000) e))

(defn bind-event! [selector event-type]
  "Given a selector and an event-type, bind an listener to a new event
  stream."
  (let [e (event/event)
        element (dom/getElement selector)]
    (events/listen element event-type (fn [ev]
                                        (-notify-watches e nil ev))) e))
