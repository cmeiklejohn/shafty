# shafty

Prototype ClojureScript functional reactive programming library. Shafty
is just an experiment and shouldn't be used in production.  Shafty is
currently in development.

Get in touch if you are interested in helping out.

## Usage

### Events

Events represent time-varying functions which have a finite set of
occurences over time.  Create an event and generate a receiver for the
event, which can either be bound to a DOM element via an
addEventListener, set as the callback for a timer, or a result of an
XmlHttpRequest.

```clojure
(def my-event (shafty/event))

(def my-event-receiver (shafty/generate-receiver my-event
                                          (fn [x] (identity x))))
```

Compose event streams using filter!, map!, reduce!, etc.

```clojure
(def my-filtered-event (shafty/filter! my-event
                                (fn [x] (= 1 x))))

(def my-mapped-event (shafty/map! my-event
                           (fn [x] (identity 1))))

(def my-combo-event (shafty/merge! my-filtered-event my-mapped-event))
```

### Behaviours

Behaviours are time-varying functions which constantly have a value.
Derive a behaviour from an event, or generate a receiver to watch a
particular DOM element, such as an input field.

```clojure
(def my-behaviour-of-ones (shafty/hold! my-filtered-event 2))
```

## TODO

* Add to IEventStream:
  * reduce!
  * ~~merge!~~
  * delay!
* Should we abandon watchers?
* Should we refactor swap! calls with -notify-watcher calls. Likely.
* When we propagate records forward, we want to call an update-fn.
* Example applications:
  * Text box auto-save
  * Timer with reset button

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
