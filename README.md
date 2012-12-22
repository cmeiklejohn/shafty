# shafty

ClojureScript functional reactive programming library.

## Disclaimer

Shafty is currently under development and the API is subject to change.

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

* Functionality to add:
  * switch!.
  * ~~send! for streams, remove explicit -notify-watches.~~
* Explore:
  * Topological propagation.
  * Delayed value propagation and queueing of events.
  * Adding a sentinal value instead of explicit propagate! calls.
  * ~~Event constructors taking sources.~~
  * ~~Function to add sinks instead of direct set! command.~~
  * ~~Change order of arugments to core functions?~~
  * Time stepping.

## References

* Elliott, [_Push-Pull Functional Reactive Programming_](http://dl.acm.org/citation.cfm?id=1596643)
* Meyerovich, [_Flapjax: Functional Reactive Web Programming_](http://www.cs.brown.edu/research/pubs/theses/ugrad/2007/lmeyerov.pdf)
* Meyerovich, Guha, Baskin, Cooper, Greenberg, Bromfield,  Krishnamurthi, [_Flapjax: A Programming Language for Ajax Applications_](http://dl.acm.org/citation.cfm?id=1640091)

## The Name

* [Shafty, Island Tour, '98, Providence, RI.](http://www.youtube.com/watch?v=AZO2_u0jmZk)

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
