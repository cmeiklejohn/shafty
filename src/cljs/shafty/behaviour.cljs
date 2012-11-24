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
        [shafty.event-stream :only [map!]]
        [shafty.propagatable :only [Propagatable propagate!]]
        [shafty.liftable :only [Liftable]]))

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
                                      (propagate! e (set! (.-state e) b)))) e)))

(extend-type Behaviour
  EventConversion
  (changes! [this] (.-stream this)))

(extend-type Behaviour
  Propagatable
  (propagate! [this value]
    (let [sinks (.-sinks this)]
      (doall (map (fn [x] (-notify-watches x nil value)) sinks)))))

(extend-type Behaviour
  Liftable
  (lift! [this lift-fn]
    (hold! (map! (changes! this) lift-fn) nil)))
