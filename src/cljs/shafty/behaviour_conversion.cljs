(ns shafty.behaviour_conversion)

(defprotocol BehaviourConversion
  "Convert an event stream into a behaviour initializing with a default
  value."
  (hold! [this init]
         "Given an initial value, create a behaviour from an event
         stream."))
