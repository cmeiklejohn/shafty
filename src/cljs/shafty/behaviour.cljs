(ns shafty.behaviour
  (:use [shafty.event-conversion :only [EventConversion]]))

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
                                      (set! (.-state e) b))) e)))

(extend-type Behaviour
  EventConversion
  (changes! [this] (.-stream this)))
