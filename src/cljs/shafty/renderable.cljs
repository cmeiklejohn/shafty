;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 which can be found in the file
;; LICENSE.html at the root of this distribution.  By using this
;; software in any fashion, you are agreeing to be bound by the terms of
;; this license. You must not remove this notice, or any other, from
;; this software.
;;
(ns shafty.renderable)

(defprotocol Renderable
  "Renders content into the DOM from streams."
  (insert! [this element]
           "Insert an element into the DOM.")
  (add-outlet! [this that]
               "Add a element to the list of renderable elements."))
