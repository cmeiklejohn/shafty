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
        [shafty.propagatable :only [Propagatable propagate!
                                    send! add-sink! SENTINEL]]
        [shafty.observable :only [Observable event! events!]]
        [shafty.requestable :only [Requestable]]
        [shafty.behaviour :only [behaviour]]
        [clojure.browser.event :only [listen]]
        [clojure.browser.net :only [xhr-connection transmit]]))

(deftype Event [sources sinks rank update-fn])

(defn event
  "Define an event, which is a time-varying value with finite
  occurences."
  ([sources update-fn]
   (let [max-source-rank  (apply max (map #(.-rank %1) sources))
         rank             (inc (or max-source-rank 0))]
     (Event. sources nil rank update-fn))))

(extend-type Event
  BehaviourConversion
  (hold! [this initial]
    (let [b (behaviour initial this)]
      (set! (.-sinks this) (conj (.-sinks this) b)) b))

  Propagatable
  (propagate! [this value]
    (let [empty-queue   cljs.core.PersistentQueue/EMPTY
          initial-value {:node this :value value}
          initial-queue (conj empty-queue initial-value)]
      (loop [pq initial-queue]
        (if (= 0 (count pq))
          value
          (let [{:keys [node value]} (peek pq)
                v                    (apply (.-update-fn node) [node value])]
            (if (not= SENTINEL v)
              (recur (reduce conj (pop pq)
                (map (fn [y] {:node y :value v}) (.-sinks node))))
              (recur (pop pq))))))))

  (send! [this value]
    (propagate! this value))

  (add-sink! [this that]
    (set! (.-sinks this) (conj (.-sinks this) that)) this)

  Requestable
  (requests! [this]
    (let [xhr (xhr-connection)
          e (event [this] (fn [me x]
                            (let [url (:url x)]
                              (transmit xhr url))))]
      (add-sink! this e) e))

  EventStream
  (filter! [this filter-fn]
    (let [e (event [this] (fn [me x] (let [v (apply filter-fn [x])]
                       (if (true? v) x SENTINEL))))]
      (add-sink! this e) e))

  (merge! [this that]
    (let [s (vector this that)
          e (event s (fn [me x] x))]
      (doall (map (fn [x] (add-sink! x e)) s)) e))

  (map! [this map-fn]
    (let [e (event [this] (fn [me x] (apply map-fn [x])))]
      (add-sink! this e) e))

  (delay! [this interval]
    (let [t (fn [me x] (js/setTimeout (fn [x] (send! me x)) interval))
          e (event [this] t)]
      (add-sink! this e) e))

  (snapshot! [this that]
    (let [e (event [this] (fn [me x] (deref that)))]
      (add-sink! this e) e)))

(extend-type js/HTMLElement
  Observable
  (event! [this event-type]
    (event! this event-type (fn [x] (identity x))))

  (event! [this event-type value-fn]
    (let [e (event [] (fn [me x] x))]
      (listen this event-type (fn [ev] (send! e (apply value-fn [ev])))) e))

  (events! [this event-types]
    (events! this event-types (fn [x] (identity x))))

  (events! [this event-types value-fn]
    (reduce (fn [acc x] (merge! acc x))
            (map (fn [event-type]
                   (event! this event-type value-fn)) event-types))))
