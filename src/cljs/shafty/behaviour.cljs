;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0
;; (http://opensource.org/licenses/eclipse-1.0.php) which can be found
;; in the file LICENSE.html at the root of this distribution.  By using
;; this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other,
;; from this software.
;;
(ns shafty.behaviour
  (:use [shafty.event-conversion :only [EventConversion]]
        [shafty.propagatable :only [Propagatable propagate!]]))

(deftype Behaviour [state stream sinks sources watches]
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
   (let [e (Behaviour. state stream nil nil nil)]
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (set! (.-state e) b)))
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (propagate! e b))) e)))

(extend-type Behaviour
  EventConversion
  (changes! [this] (.-stream this)))

(extend-type Behaviour
  Propagatable
  (propagate! [this value]
    (let [sinks (.-sinks this)]
      (doall (map (fn [x] (-notify-watches x nil value)) sinks)))))
