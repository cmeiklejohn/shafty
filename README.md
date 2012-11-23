# shafty

ClojureScript functional reactive programming library.

## Disclaimer

Shafty is currently under development and the API is subject to change.  Shafty shouldn't be used in production.

Get in touch if you are interested in helping out.

## Usage

### Events

Events represent time-varying functions which have a finite set of
occurences over time.

```clojure
(def my-event (shafty/event))
```

Compose event streams using filter!, map!, reduce!, etc.

```clojure
(def my-filtered-event (shafty/filter! my-event
                                (fn [x] (= 1 x))))

(def my-mapped-event (shafty/map! my-event
                           (fn [x] (identity 1))))

(def my-combo-event (shafty/merge! my-filtered-event
                                   my-mapped-event))

(def my-delayed-event (shafty/delay! my-filtered-event 50000)
```

### Behaviours

Behaviours are time-varying functions which constantly have a value. Derive a behaviour from an event, or generate a receiver to watch a particular DOM element, such as an input field.

Behaviours share the same IEventStream protocol, so you can also use merge!, delay!, map!, filter!, etc. to compose them.

You can also call changes! to convert a Behaviour back to an Event.

```clojure
(def my-behaviour-of-ones (shafty/hold! my-filtered-event 1))

(def my-behaviour-as-event (shafty/changes! my-behaviour-of-ones))
```

## References

* Elliott, [_Push-Pull Functional Reactive Programming_](http://dl.acm.org/citation.cfm?id=1596643)
* Meyerovich, [_Flapjax: Functional Reactive Web Programming_](http://www.cs.brown.edu/research/pubs/theses/ugrad/2007/lmeyerov.pdf)
* Meyerovich, Guha, Baskin, Cooper, Greenberg, Bromfield,  Krishnamurthi, [_Flapjax: A Programming Language for Ajax Applications_](http://dl.acm.org/citation.cfm?id=1640091)

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
