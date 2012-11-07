(ns test.integration.integration
  (:require [shafty.core :as shafty]))

;; Define a behavior which is a function which returns functions of
;; increasing integers, but returns the value of the current integer
;; when deref'd.
;;
(defn incr
  ([val] val)
  ([val incr-by] #(+ val incr-by)))

(let [behavior (shafty/behavior (partial incr 1))]

  ;; Assert that the current value is one.
  ;;
  (assert (= 1 @behavior))

  ;; Receive an event from the event stream.
  ;;
  (shafty/receive-event! behavior 2)

  ;; Assert that the current value is three.
  ;;
  (assert (= 3 @behavior)))

;; Define a behavior which is a function returning two,
;; and subsequently define a lift which adds one to the value of
;; behavior.
;;
(let [behavior (shafty/behavior #(identity 2))
      lift (shafty/lift behavior :add-one (fn [x] (+ 1 x)))]

  ;; Assert that these values are correct.
  ;;
  (assert (= 2 @behavior))
  (assert (= 3 @lift))

  ;; Change the behavior to a function which returns the identity 3,
  ;; and assert that the values rendered are correct.
  ;;
  (swap! behavior (fn [] #(identity 3)))
  (assert (= 3 @behavior))
  (assert (= 4 @lift)))

;; Define a behavior which is a function which needs to be called 5
;; times before performing an action.
;;
(defn threshold
  ([thr call-fn]
   #(threshold thr 1 call-fn))
  ([thr cur call-fn]
   (if (> cur thr)
     (do (apply call-fn [])
       #(threshold thr call-fn))
     #(threshold thr (inc cur) call-fn))))

(let [behavior (shafty/behavior
                 (threshold 5 (fn [] (.log js/console "Made it, Ma!"))))]
  (dotimes [n 6]
    (shafty/receive-event! behavior n)))
