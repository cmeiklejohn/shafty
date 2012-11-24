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
        [shafty.event-stream :only [EventStream]]
        [shafty.propagatable :only [Propagatable propagate!]]
        [shafty.behaviour :only [behaviour]]))

(deftype Event [sinks sources watches]
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
  ([]
   (let [e (Event. nil nil nil)]
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (propagate! e b))) e))
  ([update-fn]
   (let [e (Event. nil nil nil)]
     (-add-watch e (gensym "watch") (partial update-fn e)) e)))

(extend-type Event
  BehaviourConversion
  (hold! [this initial]
    (let [b (behaviour initial this)]
      (set! (.-sinks this) (conj (.-sinks this) b)) b)))

(extend-type Event
  Propagatable
  (propagate! [this value]
    (let [sinks (.-sinks this)]
      (doall (map (fn [x] (-notify-watches x nil value)) sinks)))))

(extend-type Event
  EventStream
  (filter! [this filter-fn]
    (let [e (event (fn [me x y a b]
                     (let [v (apply filter-fn [b])]
                       (if (true? v) (propagate! me b)))))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      (set! (.-sources e) (conj (.-sources e) this)) e))

  (merge! [this that]
    (let [e (event)]
      (set! (.-sinks this) (conj (.-sinks this) e))
      (set! (.-sinks that) (conj (.-sinks that) e))
      (set! (.-sources e) (conj (.-sources e) this))
      (set! (.-sources e) (conj (.-sources e) that)) e))

  (map! [this map-fn]
    (let [e (event (fn [me x y a b]
                       (propagate! me (apply map-fn [b]))))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      (set! (.-sources e) (conj (.-sources e) this)) e))

  (delay! [this interval]
    (let [e (event (fn [me x y a b]
                       (js/setTimeout (fn []
                                        (propagate! me b)) interval)))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      (set! (.-sources e) (conj (.-sources e) this)) e))

  (snapshot! [this that]
    (let [e (event (fn [me x y a b] (propagate! me (deref that))))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      (set! (.-sources e) (conj (.-sources e) this)) e)))
