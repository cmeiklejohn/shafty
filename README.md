# shafty

ClojureScript functional reactive programming library inspired by [Flapjax](http://www.flapjax-lang.com/).  Shafty provides the core abstractions provided by Flapjax for dataflow programming with reactive values, however does not provide the DOM manipulation abstractions provided by Flapjax.  

## Disclaimer

Shafty is currently under development and the API is subject to change.

Get in touch if you are interested in helping out.

## Motivation

Shafty aims to bring a native implementation of composable behaviours and event streams to ClojureScript as a micro-library which can be used to build larger reactive applications.  The long-term goal is to provide a Clojure implementation which can be used in both server-side and client-side applications.

## Concepts

### Events

Events represent time-varying functions which have a finite set of
occurences over time.

### Behaviours

Behaviours are time-varying functions which constantly have a value.
Derive a behaviour from an event, or generate a receiver to watch a
particular DOM element, such as an input field.

## API Reference

As Shafty is heavily inspired by Flapjax, provided below is an API reference outlining which parts of the Flapjax API have been implemented and what the equivalent function name is in Shafty.  Some of the functions have similar mappings leveraging Clojure's protocols.

The functions below appear in the order of the appearance of each in the Flapjax implementation.

<table>
<tr><th>F.Event</th><th>shafty.core/event</th></tr>
<tr><td>sendEvent</td><td>send!</td></tr>
<tr><td>mergeE</td><td>merge!</td></tr>
<tr><td>constantE</td><td></td></tr>
<tr><td>bindE</td><td></td></tr>
<tr><td>mapE</td><td>map!</td></tr>
<tr><td>notE</td><td></td></tr>
<tr><td>filterE</td><td>filter!</td></tr>
<tr><td>onceE</td><td></td></tr>
<tr><td>skipFirstE</td><td></td></tr>
<tr><td>collectE</td><td></td></tr>
<tr><td>switchE</td><td></td></tr>
<tr><td>delayE</td><td>delay!</td></tr>
<tr><td>snapshotE</td><td>snapshot!</td></tr>
<tr><td>filterRepeatsE</td><td></td></tr>
<tr><td>calmE</td><td></td></tr>
<tr><td>blindE</td><td></td></tr>
</table>

<table>
<tr><th>F.Behavior</th><th>shafty.core/behaviour</th></tr>
<tr><td>startsWith</td><td></td></tr>
<tr><td>valueNow</td><td></td></tr>
<tr><td>changes</td><td></td></tr>
<tr><td>switchB</td><td></td></tr>
<tr><td>timerB</td><td></td></tr>
<tr><td>delayB</td><td></td></tr>
<tr><td>sendBehavior</td><td></td></tr>
<tr><td>liftB</td><td></td></tr>
<tr><td>notB</td><td></td></tr>
<tr><td>blindB</td><td></td></tr>
<tr><td>calmB</td><td></td></tr>
</table>

## TODO

* Add pulses.
* Do not relift on same pulse.
* Creation of behaviour generates a new event which updates it's behaviour.
* Remove outlets.
* Add rest of TODOs from notes.

## References

* Elliott, [_Push-Pull Functional Reactive Programming_](http://dl.acm.org/citation.cfm?id=1596643)
* Meyerovich, [_Flapjax: Functional Reactive Web Programming_](http://www.cs.brown.edu/research/pubs/theses/ugrad/2007/lmeyerov.pdf)
* Meyerovich, Guha, Baskin, Cooper, Greenberg, Bromfield,  Krishnamurthi, [_Flapjax: A Programming Language for Ajax Applications_](http://dl.acm.org/citation.cfm?id=1640091)

## The Name

* [Shafty, Island Tour, '98, Providence, RI.](http://www.youtube.com/watch?v=AZO2_u0jmZk)

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
