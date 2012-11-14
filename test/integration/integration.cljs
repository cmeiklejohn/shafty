(ns test.integration.integration
  (:require [shafty.core :as shafty]))

(.log js/console "Starting Tests")

;; Generate a series of events, and verify that after all changes have
;; propgated a filtered event only contains the correct values.
;;
(let [e1 (shafty/event)
      r1 (shafty/generate-receiver e1 (fn [x] (identity x)))
      e2 (shafty/filter! e1 (fn [x] (= 1 x)))
      e3 (shafty/map! e2 (fn [x] (assert (= 1 x))))]
  (r1 2)
  (r1 1)
  (r1 1))

;; Generate a series of events, and verify that after all changes have
;; propgated a mapped event only contains the correct values.
;;
(let [e1 (shafty/event)
      r1 (shafty/generate-receiver e1 (fn [x] (identity x)))
      e2 (shafty/map! e1 (fn [x] (identity 3)))
      e3 (shafty/map! e2 (fn [x] (assert (= 3 x))))]
  (r1 2)
  (r1 1)
  (r1 1))

;; Generate a series of events, hold to a behaviour, and assert that the
;; behaviour deref's to the correct value.
;;
(let [e1 (shafty/event)
      r1 (shafty/generate-receiver e1 (fn [x] (identity x)))
      e2 (shafty/map! e1 (fn [x] (identity 3)))
      b1 (shafty/hold! e2 nil)]
  (r1 2)
  (r1 1)
  (r1 1)
  (assert (= 3 @b1)))

(.log js/console "Ending Tests")
