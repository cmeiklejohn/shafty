(ns shafty.event
  (:use [shafty.behaviour-conversion :only [BehaviourConversion]]
        [shafty.event-stream :only [EventStream propagate!]]
        [shafty.behaviour :only [behaviour]]))

(deftype Event [sinks watches]
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
   (let [e (Event. nil nil)]
     (-add-watch e (gensym "watch") (fn [x y a b]
                                      (propagate! e b))) e))
  ([update-fn]
   (let [e (Event. nil nil)]
     (-add-watch e (gensym "watch") (partial update-fn e)) e)))

(extend-type Event
  BehaviourConversion
  (hold! [this init]
    (let [b (behaviour init this)]
      (set! (.-sinks this) (conj (.-sinks this) b)) b)))

(extend-type Event
  EventStream
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
