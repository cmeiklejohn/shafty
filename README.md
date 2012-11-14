# shafty

Prototype ClojureScript functional reactive programming library. Shafty
is just an experiment and shouldn't be used in production.

## TODO

* Don't use watchers, store a sink of nodes to propgate forward to, and
  call a selection-fn on the members of the sink.
* Investigate adding sources vector to Event/Behaviour.
* Add to IEventStream:
  * collect!
  * merge!
  * delay!
* Behaviours value should only change when deref'd?
* Text box save example.

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
