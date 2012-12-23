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
        [shafty.propagatable :only [Propagatable propagate!
                                    send! add-sink!]]
        [shafty.renderable :only [Renderable insert! add-outlet!]]
        [shafty.liftable :only [Liftable lift! lift2!]]
        [shafty.observable :only [Observable events!]]))

(deftype Behaviour [state stream update-fn outlets]
  IDeref
  (-deref [_] state))

(defn behaviour
  "Define a behaviour, which is a time-varying value providing constant
  values."
  ([state stream]
   (Behaviour. state stream (fn [me x] (propagate! me x)) nil)))

(extend-type Behaviour
  EventConversion
  (changes! [this] (.-stream this))

  Propagatable
  (propagate! [this value]
    (let [value (set! (.-state this) value) outlets (.-outlets this)]
      (doall (map (fn [x] (set! (.-innerHTML x) value)) outlets)) value))

  (send! [this value]
    (propagate! this value))

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
    (-> (add-outlet! this element)
        (propagate! (deref this))) this)

  (add-outlet! [this that]
    (set! (.-outlets this) (conj (.-outlets this) that)) this))

(extend-type js/HTMLElement
  Observable
  (behaviour! [this initial]
    (-> (events! this ["change" "keyup"])
        (hold! initial)
        (insert! this))))
