(ns constraint.config.coercions
  (:import clojure.lang.Keyword))

;; Borrowed from Constraint as it's currently private.
(defn- failed-coercion [type data]
  {:error    :failed-coercion
   :coercion type
   :found    data})

(defn- string->long [s]
  (try
    {:value (Long/parseLong s)}
    (catch NumberFormatException _
      {:errors #{(failed-coercion Long s)}})))

(defn- string->boolean [s]
  {:value (= s "true")})

(defn- string->keyword [s]
  {:value (keyword s)})

(def string-coercions
  {[String Long] string->long
   [String Boolean] string->boolean
   [String Keyword] string->keyword})
