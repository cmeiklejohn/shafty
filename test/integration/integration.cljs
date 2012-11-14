(ns test.integration.integration
  (:require [shafty.core :as shafty]))

(.log js/console "Starting Tests")

;; Create an event.
(def my-event (shafty/event))

;; Generate a receiver, and send events to it.
(def my-event-receiver (shafty/generate-receiver my-event
                                          (fn [x]
                                            (.log js/console (str "Event selector called with " x))
                                            (identity x))))

;; Filter events into a new stream that are equal to the value 1.
(def my-filtered-event (shafty/filter! my-event
                                (fn [x]
                                  (.log js/console (str "Filter called with " x))
                                  (= 1 x))))

;; Map events into a new stream with the identity of 1.
(def my-mapped-event (shafty/map! my-event
                           (fn [x]
                             (.log js/console (str "Map called with " x))
                             (identity 1))))

;; Map mapped into a new stream and perform assertions that all received values
;; are 1.
(def my-secondary-mapped-event (shafty/filter! my-mapped-event
                                        (fn [x]
                                          (.log js/console (str "Secondary map called with " x))
                                          (assert (= 1 x)))))

;; Map filtered into a new stream and perform assertions that all received values
;; are 1.
(def my-secondary-filtered-event (shafty/filter! my-mapped-event
                                          (fn [x]
                                            (.log js/console (str "Secondary filter called with " x))
                                            (assert (= 1 x)))))

;; Convert event stream into a behavior.
(def my-behaviour-of-ones (shafty/hold! my-filtered-event 2))

;; Convert behaviour back into an event stream and map it with
;; assertions.
(def my-event-from-behaviour (shafty/changes! my-behaviour-of-ones))

(def my-mapped-event-from-behaviour (shafty/filter! my-event-from-behaviour
                                        (fn [x]
                                          (.log js/console (str "Mapped behaviour called with " x))
                                          (assert (= 1 x)))))

;; Send events.
(my-event-receiver 1)
(my-event-receiver 2)
(my-event-receiver 1)

;; Assert behavior has the correct value.
(assert (= 1 @my-behaviour-of-ones))

(.log js/console "Ending Tests")
