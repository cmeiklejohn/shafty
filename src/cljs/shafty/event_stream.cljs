(ns shafty.event-stream)

(defprotocol EventStream
  "The composable stream interface provides a series of filtering and
  selection methods for working with objects as they enter and leave the
  event stream."
  (propagate! [this value]
              "Propagate the value to all of the sinks.")
  (filter! [this filter-fn]
           "Filter the objects of the event stream using the provided
           filter-fn.")
  (map! [this map-fn]
           "Map the objects of the event stream using the provided
           map-fn.")
  (merge! [this that]
          "Merge supplied event stream and other event stream into one
          event stream")
  (delay! [this interval]
          "Delay propagation for interval.")
  (snapshot! [this that]
             ""))
