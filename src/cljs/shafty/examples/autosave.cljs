(ns shafty.examples.autosave
  (:use [shafty.observable :only [bind! bind-timer!]]
        [shafty.event_stream :only [merge! map!]])
  (:require [goog.dom :as dom]))

(defn main []
  "Run the autosave example"

  (let [as (fn [] (.log js/console "Autosave function firing!"))
        e1 (bind-timer! 5000)
        e2 (bind! (dom/getElement "save-button") "click")
        e3 (merge! e1 e2)
        e4 (map! e3 as)]
    (.log js/console "Running autosave example.")))
