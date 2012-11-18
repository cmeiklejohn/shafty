(ns shafty.observable
  (:use [shafty.event_stream :only [merge!]]
        [shafty.behaviour_conversion :only [hold!]])
  (:require [shafty.event :as event]
            [shafty.behaviour :as behaviour]
            [goog.dom :as dom]
            [goog.events :as events]))

(defn bind-timer!
  "Generate a new timer at a given interval, and bind to a new event
  stream."
  ([interval]
   (bind-timer! interval js/Date))
  ([interval value-fn]
   (let [e (event/event)]
     (js/setInterval (fn []
                       (-notify-watches e nil (value-fn))) interval) e)))

(defn bind-event! [element event value-fn]
  "Given a selector and an event, bind an event listener to a new event
  stream."
    (let [e (event/event)]
      (events/listen element event (fn [ev]
                                     (-notify-watches e nil (apply value-fn [ev])))) e))

(defn bind-behaviour! [element events get-fn set-fn]
  "Given a selector, generate an onchange event stream for the
  particular element, and return a new behaviour."
  (let [e (reduce (fn [acc x] (merge! acc x))
                  (map (fn [x] (bind-event! element x get-fn)) events))
        value (get-fn)
        b (hold! e value)]
    (-add-watch b (gensym "watch") (fn [x y a b] (set-fn b))) b))

(defprotocol Observable
  "Generate observables from browser elements or events."
  (bind! [this] [this event]
         "Given a particular element type, bind a listener for a
         particular type of event and return an event stream which will
         be populated with events."))

(extend-type js/HTMLElement
  Observable
  (bind! [this event]
    (bind-event! this event (fn [x] (identity x)))))

(extend-type js/HTMLTextAreaElement
  Observable
  (bind! [this]
    (bind-behaviour! this ["change" "keyup"]
                     (fn [] (.-value this))
                     (fn [x] (set! (.-value this) x)))))
