(ns shafty.examples.autosave
  (:use [shafty.observable :only [bind! bind-timer!]]
        [shafty.event-stream :only [merge! map!]])
  (:require [goog.dom :as dom]))

(defn- perform-save []
  "Perform the save."

  (let [element (dom/getElement "save-status")
        curtime (js/Date)]
    (set! (.-innerHTML element) (str "Last save at " curtime))))

(defn main []
  "Run the autosave example."

  (-> (bind! (dom/getElement "save-button") "click")
      (merge! (bind-timer! 10000))
      (map! perform-save)))
