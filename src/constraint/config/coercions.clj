(ns constraint.config.coercions)

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

(def string-coercions
  {[String Long] string->long
   [String Boolean] string->boolean})
