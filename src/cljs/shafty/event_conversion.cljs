;; Copyright (c) Christopher Meiklejohn. All rights reserved.
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0
;; (http://opensource.org/licenses/eclipse-1.0.php) which can be found
;; in the file LICENSE.html at the root of this distribution.  By using
;; this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other,
;; from this software.
;;
(ns shafty.event-conversion)

(defprotocol EventConversion
  "Convert a behaviour back to an event stream."
  (changes! [this]
            "Given a behaviour, convert the behaviour back to an event
            stream."))
