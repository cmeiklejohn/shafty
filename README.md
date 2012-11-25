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
* Fix elapsed demo with proper lifting.
* Switching.
* Clean up repetition in shafty.event.

## References

* Elliott, [_Push-Pull Functional Reactive Programming_](http://dl.acm.org/citation.cfm?id=1596643)
* Meyerovich, [_Flapjax: Functional Reactive Web Programming_](http://www.cs.brown.edu/research/pubs/theses/ugrad/2007/lmeyerov.pdf)
* Meyerovich, Guha, Baskin, Cooper, Greenberg, Bromfield,  Krishnamurthi, [_Flapjax: A Programming Language for Ajax Applications_](http://dl.acm.org/citation.cfm?id=1640091)

## Examples

### Autosave form

```clojure
(defn- build-request [value]
  "Generate a request object."
  { :url "/save" :data { :value value } :method "post" })

(defn- live-content []
  "Generate a behaviour for the live content area."
  (behaviour! (dom/getElement "live-content") nil))

(defn- timer []
  "Generate a timer."
  (-> (timer! 10000 (fn [] (js/Date.)))
      (map! (fn [x] (.log js/console "Timer ticked.") x))))

(defn main []
  "Run the autosave example."

  (-> (event! (dom/getElement "save-button") "click")
      (map! (fn [x] (.log js/console "Button clicked.") x))
      (merge! (timer))
      (snapshot! (live-content))
      (map! build-request)
      (requests!))

  (.log js/console "Starting the autosave example."))
```

## License

Copyright (C) 2012 Christopher Meiklejohn.

Distributed under the Eclipse Public License, the same as Clojure.
