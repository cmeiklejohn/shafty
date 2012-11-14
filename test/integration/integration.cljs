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

;; Generate multiple event streams, merge into one, hold into a
;; behaviour and verify that all events are received.
;;
(let [e1 (shafty/event)
      r1 (shafty/generate-receiver e1 (fn [x] (identity x)))
      e2 (shafty/event)
      r2 (shafty/generate-receiver e2 (fn [x] (identity x)))
      e3 (shafty/merge! e1 e2)
      b1 (shafty/hold! e3 0)]
  (r1 1)
  (assert (= 1 @b1))
  (r2 2)
  (assert (= 2 @b1))
  (r1 3)
  (assert (= 3 @b1))
  (r2 4)
  (assert (= 4 @b1)))

;; Generate a filtered event through a delay and verify delayed
;; propogation.
;;
(let [e1 (shafty/event)
      r1 (shafty/generate-receiver e1 (fn [x] (identity x)))
      e3 (shafty/delay! e1 50000)
      b1 (shafty/hold! e3 0)]
  (r1 1)
  (assert (= 0 @b1))
  (js/setTimeout (fn [] (assert (= 1 @b1)) 50000)))

(.log js/console "Ending Tests")
