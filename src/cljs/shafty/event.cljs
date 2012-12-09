;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.event
  (:use [shafty.behaviour-conversion :only [BehaviourConversion]]
        [shafty.event-stream :only [EventStream merge!]]
        [shafty.propagatable :only [Propagatable propagate! send! add-sink!]]
        [shafty.observable :only [Observable event! events!]]
        [shafty.requestable :only [Requestable]]
        [shafty.behaviour :only [behaviour]]
        [clojure.browser.event :only [listen]])
  (:require [goog.net.XhrIo :as xhrio]))

(deftype Event [sources sinks watches]
  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

(defn event
  "Define an event, which is a time-varying value with finite
  occurences."
  ([sources update-fn]
   (let [e (Event. sources nil nil)]
     (-add-watch e (gensym "watch") (partial update-fn e)) e)))

(extend-type Event
  BehaviourConversion
  (hold! [this initial]
    (let [b (behaviour initial this)]
      (set! (.-sinks this) (conj (.-sinks this) b)) b))

  Propagatable
  (propagate! [this value]
    (let [sinks (.-sinks this)]
      (doall (map (fn [x] (send! x value)) sinks))))

  (send! [this value]
    (-notify-watches this nil value))

  (add-sink! [this that]
    (set! (.-sinks this) (conj (.-sinks this) that)))

  Requestable
  (requests! [this]
    (let [e (event [this] (fn [me x y a b] (let [url (:url b)] (xhrio/send url (fn [ev] (propagate! me (.-target ev)))))))]
      (add-sink! this e) e))

  EventStream
  (filter! [this filter-fn]
    (let [e (event [this] (fn [me x y a b] (let [v (apply filter-fn [b])]
                       (if (true? v) (propagate! me b)))))]
      (add-sink! this e) e))

  (merge! [this that]
    (let [s (vector this that) e (event s (fn [me x y a b] (propagate! me b)))]
      (doall (map (fn [x] (add-sink! x e)) s)) e))

  (map! [this map-fn]
    (let [e (event [this] (fn [me x y a b] (propagate! me (apply map-fn [b]))))]
      (add-sink! this e) e))

  (delay! [this interval]
    (let [e (event [this] (fn [me x y a b] (js/setTimeout (fn [] (propagate! me b)) interval)))]
      (add-sink! this e) e))

  (snapshot! [this that]
    (let [e (event [this] (fn [me x y a b] (propagate! me (deref that))))]
      (add-sink! this e) e)))

(extend-type js/HTMLElement
  Observable
  (event! [this event-type]
    (event! this event-type (fn [x] (identity x))))
  (event! [this event-type value-fn]
    (let [e (event [] (fn [me x y a b] (propagate! me b)))]
      (listen this event-type (fn [ev] (send! e (apply value-fn [ev])))) e))
  (events! [this event-types]
    (events! this event-types (fn [x] (identity x))))
  (events! [this event-types value-fn]
    (reduce (fn [acc x] (merge! acc x))
            (map (fn [event-type]
                   (event! this event-type value-fn)) event-types))))
