;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.behaviour
  (:use [shafty.event-conversion :only [EventConversion changes!]]
        [shafty.behaviour-conversion :only [hold!]]
        [shafty.event-stream :only [map! merge!]]
        [shafty.propagatable :only [Propagatable propagate! send!]]
        [shafty.renderable :only [Renderable insert!]]
        [shafty.liftable :only [Liftable lift! lift2!]]
        [shafty.observable :only [Observable events!]]))

(deftype Behaviour [state stream watches]
  IDeref
  (-deref [_] state)

  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

(defn behaviour
  "Define a behaviour, which is a time-varying value providing constant
  values."
  ([state stream]
   (let [e (Behaviour. state stream nil)]
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (propagate! e (set! (.-state e) b))
                                      )) e)))

(extend-type Behaviour
  EventConversion
  (changes! [this] (.-stream this))

  Propagatable
  (propagate! [this value]
    (let [sinks (.-sinks this)]
      (doall (map (fn [x] (send! x value)) sinks))))

  (send! [this value]
    (-notify-watches this nil value))

  Liftable
  (lift! [this lift-fn]
    (-> (changes! this) (map! lift-fn) (hold! nil)))
  (lift2! [this that lift-fn]
    (lift2! this that lift-fn nil))
  (lift2! [this that lift-fn initial]
    (-> (merge! (changes! this) (changes! that))
        (map! (fn [] (apply lift-fn [@this @that])))
        (hold! initial)))

  Renderable
  (insert! [this element]
    (-add-watch this (gensym "watch") (fn [x y a b]
                                     (set! (.-innerHTML element) b)))
    (set! (.-innerHTML element) (deref this))
    this))

(extend-type js/HTMLElement
  Observable
  (behaviour! [this initial]
    (-> (events! this ["change" "keyup"])
        (hold! initial)
        (insert! this))))
