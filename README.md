# shafty

ClojureScript functional reactive programming library.

## Disclaimer

Shafty is currently under development and the API is subject to change.

Shafty shouldn't be used in production.

Get in touch if you are interested in helping out.

## Usage

### Events

Events represent time-varying functions which have a finite set of
occurences over time.

### Behaviours

Behaviours are time-varying functions which constantly have a value.
Derive a behaviour from an event, or generate a receiver to watch a
particular DOM element, such as an input field.

## Incomplete

* Figure out how to properly perform the multi-arity lift, and replace
  the examples with that.
* Add a requests! function for taking a series of web requests and
  returning web responses.
* Fix elapsed demo with proper lifting.
* Switching.

## References

* Elliott, [_Push-Pull Functional Reactive Programming_](http://dl.acm.org/citation.cfm?id=1596643)
* Meyerovich, [_Flapjax: Functional Reactive Web Programming_](http://www.cs.brown.edu/research/pubs/theses/ugrad/2007/lmeyerov.pdf)
* Meyerovich, Guha, Baskin, Cooper, Greenberg, Bromfield,  Krishnamurthi, [_Flapjax: A Programming Language for Ajax Applications_](http://dl.acm.org/citation.cfm?id=1640091)

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
