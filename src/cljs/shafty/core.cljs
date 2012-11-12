(ns shafty.core)

(defprotocol IRawEventReceiver
  "The raw event receiver interface provides a mechanism to generate
  functions which can be the target of DOM, XHR or other events which
  add an object to the event stream."
  (generate-receiver [this select-fn]
                      "Generate a receiver function which can be used as
                      the target of an addEventReceiver, or an
                      XmlHttpRequest response.  When called, the result
                      of the select-fn applied to the event will be
                      added to the event stream."))

(defprotocol IEventStream
  "The event stream interface provides a series of filtering and
  selection methods for working with objects as they enter and leave the
  event stream"
  (filter! [this filter-fn]
           "Filter the objects of the event stream using the provided
           filter-fn")
  (map! [this map-fn]
           "Map the objects of the event stream using the provided
           map"))

(defprotocol IBehaviourConversion
  "Convert an event stream into a behaviour initializing with a default value"
  (hold! [this init]
         "Given an initial value, create a behaviour from an event stream."))

(deftype Behaviour [state watches]
  IDeref
  (-deref [_] state)

  IRawEventReceiver
  (generate-receiver [this select-fn]
    (fn [event] (swap! this #(apply select-fn [event]))))

  IEventStream
  (filter! [this filter-fn]
    (let [e (Behaviour. nil nil)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (let [r (apply filter-fn [b])]
                      (if (true? r)
                        (swap! e #(identity b))))))
      e))

  (map! [this map-fn]
    (let [e (Behaviour. nil nil)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (swap! e #(apply map-fn [b]))))
      e))


  IWatchable
  (-notify-watches [this oldval newval]
    (doseq [[key f] watches]
      (f key this oldval newval)))
  (-add-watch [this key f]
    (set! (.-watches this) (assoc watches key f)))
  (-remove-watch [this key]
    (set! (.-watches this) (dissoc watches key))))

(deftype Event [watches]
  IRawEventReceiver
  (generate-receiver [this select-fn]
    (fn [event] (swap! this #(apply select-fn [event]))))

  IBehaviourConversion
  (hold! [this init]
    (.log js/console (str "init at " init))
    (let [be (Behaviour. init nil)]
      (-add-watch this (gensym "behaviour")
                  (fn [x y a b]
                    (swap! be (constantly b))))
      be))

  IEventStream
  (filter! [this filter-fn]
    (let [ev (Event. nil)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (let [r (apply filter-fn [b])]
                      (if (true? r)
                        (swap! ev #(identity b))))))
      ev))

  (map! [this map-fn]
    (let [ev (Event. nil)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (swap! ev #(apply map-fn [b]))))
      ev))

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
  ([x] (Behaviour. x nil)))

(defn event
  "Define an event, which is a time-varying value with finite
  occurences."
  ([] (Event. nil)))
