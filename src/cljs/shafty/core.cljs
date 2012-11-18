(ns shafty.core)

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

(deftype Event [sinks watches]
  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

(defprotocol IComposableEventStream
  "The composable stream interface provides a series of filtering and
  selection methods for working with objects as they enter and leave the
  event stream."
  (propagate! [this value]
              "Propagate the value to all of the sinks.")
  (filter! [this filter-fn]
           "Filter the objects of the event stream using the provided
           filter-fn.")
  (map! [this map-fn]
           "Map the objects of the event stream using the provided
           map-fn.")
  (merge! [this that]
          "Merge supplied event stream and other event stream into one
          event stream")
  (delay! [this interval]
          "Delay propagation for interval."))

(defn event
  "Define an event, which is a time-varying value with finite
  occurences."
  ([]
   (let [e (Event. nil nil)]
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (propagate! e b))) e))
  ([update-fn]
   (let [e (Event. nil nil)]
     (-add-watch e (gensym "watch") (partial update-fn e)) e)))

(defn behaviour
  "Define a behaviour, which is a time-varying value providing constant
  values."
  ([state stream]
   (let [e (Behaviour. state stream nil)]
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (set! (.-state e) b))) e)))

(extend-type Event
  IComposableEventStream
  (propagate! [this value]
    (let [sinks (.-sinks this)]
      (doall (map (fn [x] (-notify-watches x nil value)) sinks))))

  (filter! [this filter-fn]
    (let [e (event (fn [me x y a b]
                     (let [v (apply filter-fn [b])]
                       (if (true? v) (propagate! me b)))))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      e))

  (merge! [this that]
    (let [e (event)]
      (set! (.-sinks this) (conj (.-sinks this) e))
      (set! (.-sinks that) (conj (.-sinks that) e))
      e))

  (map! [this map-fn]
    (let [e (event (fn [me x y a b]
                       (propagate! me (apply map-fn [b]))))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      e))

  (delay! [this interval]
    (let [e (event (fn [me x y a b]
                       (js/setTimeout (fn []
                                        (propagate! me b)) interval)))]
      (set! (.-sinks this) (conj (.-sinks this) e))
      e)))

(defprotocol IEventConversion
  "Convert a behaviour back to an event stream."
  (changes! [this]
            "Given a behaviour, convert the behaviour back to an event
            stream."))

(extend-type Behaviour
  IEventConversion
  (changes! [this] (.-stream this)))

(defprotocol IBehaviourConversion
  "Convert an event stream into a behaviour initializing with a default
  value."
  (hold! [this init]
         "Given an initial value, create a behaviour from an event
         stream."))

(extend-type Event
  IBehaviourConversion
  (hold! [this init]
    (let [b (behaviour init this)]
      (set! (.-sinks this) (conj (.-sinks this) b)) b)))
