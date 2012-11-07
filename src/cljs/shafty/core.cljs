(ns shafty.core)

(defprotocol IReactive
  "Event occurance handler."
  (receive-event! [this occurrence]
            "Receive a particular event occurrence and apply behavior to
            event occurrence to generate a new behavior."))

(deftype Behavior [state meta watches]
  IDeref
  (-deref [_] (apply state []))

  IMeta
  (-meta [_] meta)

  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key)))

  IReactive
  (receive-event! [this occurrence]
    (swap! this (fn [] (apply state [occurrence])))))

(defn behavior
  "Define a behavior."
  ([x] (Behavior. x nil nil)))

(deftype Lift [behavior name value-fn state meta watches]
  IDeref
  (-deref [_] state)

  IMeta
  (-meta [_] meta)

  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

(defn lift
  "Lift a normal function onto a behavior."
  ([behavior name value-fn]
   (let [lift (Lift. behavior name value-fn nil nil nil)]
     (-add-watch behavior name (fn [x y a b]
                                 (let [v (apply b [])]
                                  (swap! lift (fn [] (apply value-fn [v]))))))
     (swap! lift #(identity (apply value-fn [@behavior])))
     lift)))
