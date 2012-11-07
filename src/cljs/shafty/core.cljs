(ns shafty.core)

(deftype Signal [state meta watches]
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

(defn signal
  "Define a signal."
  ([] (Signal. nil nil nil)))

(deftype Lift [signal name value-fn state meta watches]
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
  "Lift a normal function onto a signal."
  ([signal name value-fn]
   (let [lift (Lift. signal name value-fn nil nil nil)]
     (-add-watch signal name (fn [x y a b]
                                (swap! lift (fn [] (apply value-fn [b])))))
     lift)))
