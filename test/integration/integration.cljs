(ns test.integration.integration
  (:require [shafty.core :as shafty]))

(let [s (shafty/behavior)
      l (shafty/lift s :add-one (fn [x] (+ 1 x)))]
  (swap! s (fn [] (+ 1 1)))
  (assert (= 2 @s))
  (assert (= 3 @l)))
