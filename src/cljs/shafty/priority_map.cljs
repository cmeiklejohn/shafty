;; Copyright (c) Mark Engelberg and Christopher Meiklejohn.
;;
;; Priority Map implementation based on Mark Engelberg's work in
;; clojure.data.priority-map, ported to work with ClojureScript.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.priority-map)

(deftype PersistentPriorityMap [priority->set-of-items item->priority _meta]
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
  (-empty [coll] (-with-meta shafty.priority-map.PersistentPriorityMap/EMPTY meta))

  IEquiv
  (-equiv [coll other] (equiv-map item->priority other))

  IReversible
  (-rseq [coll]
    (seq (for [[priority item-set] (rseq priority->set-of-items), item item-set]
           (vector item priority))))

  ISeqable
  (-seq [coll]
    (seq (for [[priority item-set] priority->set-of-items, item item-set]
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
                  current-priority (disj (get priority->set-of-items current-priority) k)
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

(set! shafty.priority-map.PersistentPriorityMap/EMPTY (PersistentPriorityMap. (sorted-map) {} {}))
