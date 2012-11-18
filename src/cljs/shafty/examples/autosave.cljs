(ns shafty.examples.autosave
  (:use [shafty.core :only [bind-event! bind-timer!]]
        [shafty.event_stream :only [merge! map!]]))

(defn main []
  "Run the autosave example"

  (let [as (fn [] (.log js/console "Autosave function firing!"))
        e1 (bind-timer! 5000)
        e2 (bind-event! "save-button" "click")
        e3 (merge! e1 e2)
        e4 (map! e3 as)]
    (.log js/console "Running autosave example.")))
