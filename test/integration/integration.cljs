(ns test.integration.integration
  (:require [shafty.core :as shafty]))

(.log js/console "Starting Tests")

; Generate a series of events, and verify that after all changes have
; propgated a filtered event only contains the correct values.
;
 (let [e1 (shafty/event)
       e2 (shafty/filter! e1 (fn [x] (= 1 x)))
       e3 (shafty/map! e2 (fn [x] (assert (= 1 x))))]
   (-notify-watches e1 nil 2)
   (-notify-watches e1 nil 1)
   (-notify-watches e1 nil 1))

;; Generate a series of events, and verify that after all changes have
;; propgated a mapped event only contains the correct values.
;;
(let [e1 (shafty/event)
      e2 (shafty/map! e1 (fn [x] (identity 3)))
      e3 (shafty/map! e2 (fn [x] (assert (= 3 x))))]
  (-notify-watches e1 nil 2)
  (-notify-watches e1 nil 1)
  (-notify-watches e1 nil 1))

;; Generate a series of events, hold to a behaviour, and assert that the
;; behaviour deref's to the correct value.
;;
(let [e1 (shafty/event)
      e2 (shafty/map! e1 (fn [x] (identity 3)))
      b1 (shafty/hold! e2 nil)]
  (-notify-watches e1 nil 2)
  (-notify-watches e1 nil 1)
  (-notify-watches e1 nil 1)
  (assert (= 3 @b1)))

;; Generate multiple event streams, merge into one, hold into a
;; behaviour and verify that all events are received.
;;
(let [e1 (shafty/event)
      e2 (shafty/event)
      e3 (shafty/merge! e1 e2)
      b1 (shafty/hold! e3 0)]
  (-notify-watches e1 nil 1)
  (assert (= 1 @b1))
  (-notify-watches e2 nil 2)
  (assert (= 2 @b1))
  (-notify-watches e1 nil 3)
  (assert (= 3 @b1))
  (-notify-watches e2 nil 4)
  (assert (= 4 @b1)))

(.log js/console "Ending Tests")
