(ns test.integration.integration
  (:require [shafty.core :as shafty]))

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
