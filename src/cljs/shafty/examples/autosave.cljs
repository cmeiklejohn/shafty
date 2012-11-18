(ns shafty.examples.autosave
  (:use [shafty.observable :only [bind! bind-timer! bind-behaviour!]]
        [shafty.event_stream :only [merge! map!]])
  (:require [goog.dom :as dom]))

(defn- update-save-status []
  (let [element (dom/getElement "save-status")
        curtime (js/Date)]
    (set! (.-innerHTML element) (str "Last save at " curtime))))

(defn main []
  "Run the autosave example"

  (let [e1 (bind-timer! 5000)
        e2 (bind! (dom/getElement "save-button") "click")
        e3 (merge! e1 e2)
        e4 (map! e3 update-save-status)
        b1 (bind! (dom/getElement "data"))]
    (.log js/console "Running autosave example.")))
