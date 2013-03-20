;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.core
  (:use [clojure.browser.event :only [listen]]
        [clojure.browser.net   :only [xhr-connection transmit]]))

;;
;; Protocols
;;

(defprotocol IPropagatable
  "Provides a mechanism for propagating events and values through the
  graph."
  (send! [this value])
  (add-sink! [this that])
  (remove-sink! [this that])
  (propagate! [this value]))

(defprotocol IBehaviourConversion
  "Convert an event stream into a behaviour initializing with a default
  value."
  (hold! [this init]))

(defprotocol ILiftable
  "Provide a mechanism for lifting functions onto behaviours."
  (lift! [this lift-fn])
  (lift2!
    [this that lift-fn]
    [this that lift-fn initial]))

(defprotocol IObservable
  "Generate events or behaviours from browser elements or events."
  (behaviour! [this initial])
  (event!
    [this event-type]
    [this event-type value-fn])
  (events!
    [this event-types]
    [this event-type value-fn]))

(defprotocol IRequestable
  "Provide a mechanism for processing event streams composed of requests
  and responses."
  (requests! [this]))

(defprotocol IEventConversion
  "Convert a behaviour back to an event stream."
  (changes! [this]))

(defprotocol IBehaviourGenerator
  "Convert a behaviour back to an event stream and generate new
  behaviours."
  (not! [this init])
  (delay! [this interval init])
  (calm! [this interval init])
  (blind! [this interval init])
  (switch! [this init]))

(defprotocol IEventStream
  "The composable stream interface provides a series of filtering and
  selection methods for working with objects as they enter and leave the
  event stream."
  (not! [this])
  (-map! [this map-fn])
  (-bind! [this value-fn])
  (once! [this])
  (switch! [this])
  (delay! [this interval])
  (calm! [this interval])
  (blind! [this interval])
  (merge! [this that])
  (-filter! [this filter-fn])
  (-collect! [this combine-fn init])
  (snapshot! [this that])
  (constant! [this value])
  (skip-first! [this])
  (filter-repeats! [this initial]))

;;
;; Helpers to normalize the API.
;;

(defn map! [f coll]
  "Map a function over an event stream."
  (-map! coll f))

(defn bind! [f coll]
  "Bind a function to an event stream."
  (-bind! coll f))

(defn filter! [pred coll]
  "Filter values over an event stream."
  (-filter! coll pred))

(defn collect! [f coll v]
  "Collect values over an event stream."
  (-collect! coll f v))

;;
;; Pulses
;;

(deftype Pulse [value timestamp])

(defn pulse
  "Generate a pulse for a particular value."
  [value]
  (Pulse. value (.getTime (js/Date.))))

;;
;; Events
;;

(declare behaviour sentinel?)

(deftype Event [sources sinks rank update-fn])

(defn event
  "Define an event, which is a time-varying value with finite
  occurences."
  ([sources update-fn]
   (let [max-source-rank (apply max (map #(.-rank %1) sources))
         rank            (inc (or max-source-rank 0))]
     (Event. sources nil rank update-fn))))

(extend-type Event
  IBehaviourConversion
  (hold! [this initial]
    (let [b (behaviour initial this)]
      (set! (.-sinks this) (conj (.-sinks this) b)) b))

  IPropagatable
  (propagate! [this initial-pulse]
    (let [empty-queue   shafty.core.PersistentPriorityMap/EMPTY
          initial-value [{:node this :pulse initial-pulse} (.-rank this)]
          initial-queue (conj empty-queue initial-value)]
      (loop [pq initial-queue]
        (if (= 0 (count pq))
          initial-pulse
          (let [[item _]   (peek pq)
                node       (:node item)
                value      ((.-update-fn node) node (:pulse item))
                next-pulse (pulse value)]
            (if (not (sentinel? next-pulse))
              (recur (reduce conj (pop pq)
                (map (fn [y] [{:node y :pulse next-pulse} (.-rank y)])
                  (.-sinks node))))
              (recur (pop pq))))))))

  (send! [this value]
    (propagate! this (pulse value)))

  (add-sink! [this that]
    (set! (.-sinks this) (conj (.-sinks this) that))
    this)

  (remove-sink! [this that]
    (set! (.-sinks this) (vec (filter (fn [x] (= x that)) (.-sinks this))))
    this)

  IRequestable
  (requests! [this]
    (let [xhr (xhr-connection)
          e (event [this] (fn [me x]
                            (let [url (:url x)]
                              (transmit xhr url))))]
      (add-sink! this e) e))

  IEventStream
  (not! [this]
    (-map! this (fn [x] (not x))))

  (-map! [this map-fn]
    (let [e (event [this] (fn [me x] (apply map-fn [(.-value x)])))]
      (add-sink! this e) e))

  (switch! [this]
    (-bind! this (fn [x])))

  (-bind! [this value-fn]
    (let [prev  (atom false)
          out   (event [] (fn [me x] x))
          in    (event [this] (fn [me x]
                                (if (= (.instanceOf @prev) Event)
                                  (remove-sink! @prev out))
                                (swap! prev (add-sink! (value-fn x) out))
                                shafty.core.Event/SENTINEL))]
      out))

  (once! [this]
    (let [done (atom false)]
      (-filter! this (fn [x]
                      (if (false? @done)
                        (swap! done (fn [] true))
                        false)))))

  (delay! [this interval]
    (let [f (fn [me x] (js/setTimeout
                         (fn [] (.-value x)) interval)
              shafty.core.Event/SENTINEL)
          e (event [this] f)]
      (add-sink! this e) e))

  (calm! [this interval]
    (let [acc (atom nil)
          t (atom nil)
          f (fn [me x]
              (swap! acc (fn [] x))
              (if (nil? t)
                (js/setTimeout (fn []
                                 (swap! t (fn [] nil))
                                 (.-value acc)) interval))
              shafty.core.Event/SENTINEL)
          e (event [this] f)]
      (add-sink! this e) e))

  (blind! [this interval]
    (let [last-sent (atom nil)
          f (fn [me x] (let [current-time (js/Date.)]
                         (if (> (- current-time last-sent) interval) 
                           (do
                             (swap! last-sent (fn [] current-time)) (.-value x))
                           (shafty.core.Event/SENTINEL))))
          e (event [this] f)]
      (add-sink! this e) e))

  (merge! [this that]
    (let [s (vector this that)
          e (event s (fn [me x] (.-value x)))]
      (doall (map (fn [x] (add-sink! x e)) s)) e))

  (-filter! [this filter-fn]
    (let [e (event [this] (fn [me x]
                            (let [v (apply filter-fn [(.-value x)])]
                              (if (true? v)
                                (.-value x)
                                shafty.core.Event/SENTINEL))))]
      (add-sink! this e) e))

  (-collect! [this combine-fn init]
    (let [acc (atom init)]
      (-map! this (fn [x] (swap! acc (fn [] (combine-fn x @acc)))))))

  (snapshot! [this that]
    (let [e (event [this] (fn [me x] (deref that)))]
      (add-sink! this e) e))

  (constant! [this value]
    (-map! this (fn [x] value)))

  (skip-first! [this]
    (let [skipped (atom false)]
      (-filter! this (fn [x]
                      (if (false? @skipped)
                        (swap! skipped (fn [] true)) false
                        true)))))

  (filter-repeats! [this initial]
    (let [prev (atom initial)]
      (-filter! this (fn [x]
                      (if (not (= @prev x))
                        (swap! prev (fn [] x)) true
                        false))))))

(extend-type js/HTMLElement
  IObservable
  (event! [this event-type]
    (event! this event-type (fn [x] (identity x))))

  (event! [this event-type value-fn]
    (let [e (event [] (fn [me x] x))]
      (listen this event-type (fn [ev]
                                (send! e (apply value-fn [ev])))) e))

  (events! [this event-types]
    (events! this event-types (fn [x] (identity x))))

  (events! [this event-types value-fn]
    (reduce (fn [acc x] (merge! acc x))
            (map (fn [event-type]
                   (event! this event-type value-fn)) event-types))))

;;
;; Propagation Sentinels
;;

(deftype Sentinel [])

(set! shafty.core.Event/SENTINEL (Sentinel.))

(defn- sentinel?
  "Return true if provided pulse is a sentinel."
  [x]
  (= (.-value x) shafty.core.Event/SENTINEL))

;;
;; Behaviours
;;

(deftype Behaviour [state stream update-fn outlets]
  IDeref
  (-deref [_] state))

(defn behaviour
  "Define a behaviour, which is a time-varying value providing constant
  values."
  ([state stream]
   (Behaviour. state stream (fn [me x] (propagate! me x)) nil)))

(extend-type Behaviour
  IBehaviourGenerator
  (not! [this init]
    (hold! (not! (changes! this)) init))

  (delay! [this interval init]
    (hold! (delay! (changes! this) interval) init))

  (calm! [this interval init]
    (hold! (calm! (changes! this) interval) init))

  (blind! [this interval init]
    (hold! (blind! (changes! this) interval) init))

  (switch! [this init]
    (hold! (switch! (changes! this)) init))

  IEventConversion
  (changes! [this] (.-stream this))

  IPropagatable
  (propagate! [this pulse]
    (let [value   (set! (.-state this) (.-value pulse))
          outlets (.-outlets this)]
      (doall
        (map (fn [x] (set! (.-innerHTML x) value)) outlets)) value))

  (send! [this value]
    (propagate! this (pulse value)))

  ILiftable
  (lift! [this lift-fn]
    (-> (changes! this) (-map! lift-fn) (hold! nil)))
  (lift2! [this that lift-fn]
    (lift2! this that lift-fn nil))
  (lift2! [this that lift-fn initial]
    (-> (merge! (changes! this) (changes! that))
        (-map! (fn [] (apply lift-fn [@this @that])))
        (hold! initial))))

;;
;; Priority Map
;;

(deftype PersistentPriorityMap [priority->set-of-items
                                item->priority
                                _meta]
  IWithMeta
  (-with-meta [coll meta] (PersistentPriorityMap. (sorted-map) {} {}))

  IMeta
  (-meta [coll] meta)

  IStack
  (-peek [coll]
    (when-not (empty? coll)
      (let [f (first priority->set-of-items)]
        (vector (first (val f)) (key f)))))
  (-pop [coll]
    (let [f         (first priority->set-of-items),
          item-set  (val f)
          item      (first item-set),
          priority  (key f)]
      (if (= (count item-set) 1)
        ;; Remove set if it's the only item.
        (PersistentPriorityMap.
          (dissoc priority->set-of-items priority)
          (dissoc item->priority item)
          (meta coll))
        ;; Remove item.
        (PersistentPriorityMap.
          (assoc priority->set-of-items priority (disj item-set item)),
          (dissoc item->priority item)
          (meta coll)))))

  ICollection
  (-conj [coll entry]
    (let [[item priority] entry] (-assoc coll item priority)))

  IEmptyableCollection
  (-empty [coll] (-with-meta shafty.core.PersistentPriorityMap/EMPTY meta))

  IEquiv
  (-equiv [coll other] (equiv-map item->priority other))

  IReversible
  (-rseq [coll]
    (seq (for [[priority item-set]
      (rseq priority->set-of-items), item item-set]
        (vector item priority))))

  ISeqable
  (-seq [coll]
    (seq (for [[priority item-set]
      priority->set-of-items, item item-set]
        (vector item priority))))

  ICounted
  (-count [coll] (count item->priority))

  ILookup
  (-lookup [coll k]
    (-lookup coll k nil))

  (-lookup [coll k not-found]
    (get item->priority k not-found))

  IAssociative
  (-assoc [coll k v]
    (let [current-priority (get item->priority k nil)]
      (if current-priority
        ;; Reassignment.
        (if (= current-priority v)
          ;; No change.
          coll
          (let [item-set (get priority->set-of-items current-priority)]
            (if (= (count item-set) 1)
              ;; Remove entire priority set and move to different
              ;; priority set.
              (PersistentPriorityMap.
                (assoc (dissoc priority->set-of-items current-priority)
                  v (conj (get priority->set-of-items v #{}) k))
                (assoc item->priority k v)
                (meta coll))
              ;; Remove item from priority set, and add to another
              ;; priority set.
              (PersistentPriorityMap.
                (assoc priority->set-of-items
                  current-priority (disj
                      (get priority->set-of-items current-priority) k)
                  v (conj (get priority->set-of-items v #{}) k))
                (assoc item->priority k v)
                (meta coll)))))
        ;; New
        (PersistentPriorityMap.
          (assoc priority->set-of-items
            v (conj (get priority->set-of-items v #{}) k))
          (assoc item->priority k v)
          (meta coll)))))

  (-contains-key? [coll k]
    (contains? item->priority k))

  IMap
  (-dissoc [coll k]
    (let [priority (item->priority k ::not-found)]
      (if (= priority ::not-found)
  ;; No item, unchanged.
  coll
  (let [item-set (priority->set-of-items priority)]
    (if (= (count item-set) 1)
      ;; Remove priority set if it's the last item at that priority.
      (PersistentPriorityMap.
        (dissoc priority->set-of-items priority)
        (dissoc item->priority k)
        (meta coll))
      ;; Remove item from priority set.
      (PersistentPriorityMap.
       (assoc priority->set-of-items priority (disj item-set k)),
       (dissoc item->priority k)
       (meta coll)))))))

  IFn
  (-invoke [coll k]
    (-lookup coll k))

  (-invoke [coll k not-found]
    (-lookup coll k not-found)))

(set! shafty.core.PersistentPriorityMap/EMPTY
      (PersistentPriorityMap. (sorted-map) {} {}))

;;
;; Timers
;;

(defn timer!
  "Generate a new timer at a given interval, and bind to a new event
  stream."
  ([interval]
   (timer! interval #(js/Date.)))
  ([interval value-fn]
   (let [e (event nil (fn [me x] (.-value x)))]
     (js/setInterval (fn [] (send! e (value-fn))) interval) e)))

(defn held-timer!
  "Generate a new timer at a given interval, and bind to a new event
  stream returned as a behaviour representing samples of the event
  stream."
  ([interval]
   (-> (timer! interval #(js/Date.))
       (hold! (js/Date.))))
  ([interval value-fn]
    (-> (timer! interval value-fn)
        (hold! (value-fn)))))
