(ns shafty.core)

;; Core

(deftype Behaviour [state watches]
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

(deftype Event [watches]
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

;; Raw Event Receiver

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

(extend-type Event
  IRawEventReceiver
  (generate-receiver [this select-fn]
    (fn [event] (swap! this #(apply select-fn [event])))))

(extend-type Behaviour
  IRawEventReceiver
  (generate-receiver [this select-fn]
    (fn [event] (swap! this #(apply select-fn [event])))))

;; Event Stream

(defprotocol IEventStream
  "The event stream interface provides a series of filtering and
  selection methods for working with objects as they enter and leave the
  event stream."
  (filter! [this filter-fn]
           "Filter the objects of the event stream using the provided
           filter-fn.")
  (map! [this map-fn]
           "Map the objects of the event stream using the provided
           map-fn."))

(extend-type Event
  IEventStream
  (filter! [this filter-fn]
    (let [ev (event)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (let [r (apply filter-fn [b])]
                      (if (true? r)
                        (swap! ev #(identity b))))))
      ev))

  (map! [this map-fn]
    (let [ev (event)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (swap! ev #(apply map-fn [b]))))
      ev)))

(extend-type Behaviour
  IEventStream
  (filter! [this filter-fn]
    (let [be (behaviour nil)]
      (-add-watch this (gensym "behaviour")
                  (fn [x y a b]
                    (let [r (apply filter-fn [b])]
                      (if (true? r)
                        (swap! be #(identity b))))))
      be))

  (map! [this map-fn]
    (let [be (behaviour nil)]
      (-add-watch this (gensym "behaviour")
                  (fn [x y a b]
                    (swap! be #(apply map-fn [b]))))
      be)))

;; Event Conversion

(defprotocol IEventConversion
  "Convert a behaviour back to an event stream."
  (changes! [this]
            "Given a behaviour, convert the behaviour back to an event
            stream."))

(extend-type Behaviour
  IEventConversion
  (changes! [this]
    (let [ev (event)]
      (-add-watch this (gensym "event")
                  (fn [x y a b]
                    (swap! ev (constantly b))))
      ev)))

;; Behaviour Conversion

(defprotocol IBehaviourConversion
  "Convert an event stream into a behaviour initializing with a default value."
  (hold! [this init]
         "Given an initial value, create a behaviour from an event stream."))

(extend-type Event
  IBehaviourConversion
  (hold! [this init]
    (let [be (Behaviour. init nil)]
      (-add-watch this (gensym "behaviour")
                  (fn [x y a b]
                    (swap! be (constantly b))))
      be)))
