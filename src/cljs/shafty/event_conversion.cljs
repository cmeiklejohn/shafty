(ns shafty.event_conversion)

(defprotocol EventConversion
  "Convert a behaviour back to an event stream."
  (changes! [this]
            "Given a behaviour, convert the behaviour back to an event
            stream."))
