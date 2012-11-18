(ns shafty.examples.autosave
  (:use [shafty.event :only [event]]
        [shafty.event_stream :only [merge! map!]])
  (:require [goog.events :as events]
            [goog.dom :as dom]))

(defn main []
  "Run the autosave example"
  (.log js/console "Running autosave example.")

  (let [ef (fn [] (js/Date))
        as (fn [] (.log js/console "Autosave function firing!"))
        e1 (event)
        e2 (event)
        e3 (merge! e1 e2)
        e4 (map! e3 as)]

    ;; Bind the event receiver to a timer.
    (js/setInterval (fn [] (-notify-watches e1 nil (ef))) 1000)

    ;; Bind event receiver to the save button.
    (let [element (dom/getElement "save-button")]
      (events/listen
        element "click" (fn [] (-notify-watches e2 nil (ef)))))))
