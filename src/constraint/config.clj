(ns constraint.config
  (:require [constraint.config.coercions :refer [string-coercions]]
            [constraint.core :as constraint]))

;; Borrowed from Medley to avoid applications having to pull in the
;; dependency.
;;
;; Original source can be found on GitHub here: http://j.mp/1nRXNQJ
(defn- map-vals
  "Maps a function over the values of an associative collection."
  [f coll]
  (persistent! (reduce-kv #(assoc! %1 %2 (f %3))
                          (transient (empty coll))
                          coll)))

(defn- errors? [m]
  (some-> m :errors seq))

(defn- build-constraint [definition]
  (map-vals :constraint definition))

(defn- read-key
  "Return a key from a configuration definition, taking into account
  some keys might be wrapped in a `constraint.core.Optional`."
  [key]
  (if (constraint/optional? key)
    (.constraint key)
    key))

(defn- build-config
  "Return a hash-map of paths and values specified by `definition`."
  [{transformed :value} definition]
  (reduce-kv #(assoc-in %1 (:path %3) ((read-key %2) transformed))
             {}
             definition))

(defn- transform
  "Transform a hash-map of configuration using the given
  `constraint`."
  [constraint value]
  (constraint/transform constraint value string-coercions))

(defn- normalize-definition-pair
  "Returns a key-value pair where the value has been normalised to a
  hash-map containing `:path` and `:constraint`.

  The default constraint applied to all configuration variables is
  `String`."
  [[k v]]
  (let [default {:path [(read-key k)] :constraint String}]
    [k (if (map? v)
         (merge default v)
         (assoc default :constraint v))]))

(defn- normalize-definition [definition]
  (into {} (map normalize-definition-pair definition)))

(defn transform-config
  "Transform the given configuration `value` using the given
  `definition`.

  The `definition` should contain a map of incoming configuration
  variables.

  For example,

    {:user String}   ;=> Return :user value as-is.
    {:user Long}     ;=> Make sure the user is a number
    {:user {:path [:person]}}     ;=> Move user into :person
    {:user {:constraint Long}}    ;=> Make sure the user is a number

  `:path` and `:constraint` can be combined.

  `value` is then another flat hash-map of configuration you want to
  transform."
  [definition value]
  (let [definition' (normalize-definition definition)
        constraint  (build-constraint definition')
        transformed (transform constraint value)]
    (if (errors? transformed)
      transformed
      (assoc transformed :config (build-config transformed definition')))))

(defn verify-config
  "Transform the given configuration using `transform-config`,
  returning the config directly when transformation succeeds, and
  throwing an exception otherwise."
  [definition value]
  (let [transformed (transform-config definition value)]
    (if (errors? transformed)
      (throw (ex-info "Invalid configuration!" transformed))
      (:config transformed))))
