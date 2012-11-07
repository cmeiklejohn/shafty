(ns shafty.core)

(deftype Behavior [state meta watches]
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

(defn behavior
  "Define a behavior."
  ([] (Behavior. nil nil nil)))

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
                                (swap! lift (fn [] (apply value-fn [b])))))
     lift)))
