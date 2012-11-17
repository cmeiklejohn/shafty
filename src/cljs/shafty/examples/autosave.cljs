(ns shafty.examples.autosave
  (:require [shafty.core :as shafty]
            [goog.events :as events]
            [goog.dom :as dom]))

(defn main []
  "Run the autosave example"
  (.log js/console "Running autosave example.")

  (let [ef (fn [] (js/Date))
        e1 (shafty/event)
        e2 (shafty/event)
        e3 (shafty/merge! e1 e2)
        e4 (shafty/map! e3 (fn [] (.log js/console "Saved!")))]

    ;; Bind the event receiver to a timer.
    (js/setInterval (fn [] (-notify-watches e1 nil (ef))) 1000)

    ;; Bind event receiver to the save button.
    (events/listen (dom/getElement "save-button") "click" (fn [] (-notify-watches e2 nil (ef))))))
