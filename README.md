# constraint-config [![Build Status](https://travis-ci.org/listora/constraint-config.svg?branch=master)](https://travis-ci.org/listora/constraint-config)

Pull configuration from environment variables and prepare it for use
in your Clojure app.

## Purpose

We adhere to the [Twelve Factor App][] methodology at Listora, and as
such pull application config from environment variables.

As environment variables have no support for the complex types we use
in our Clojure applications a type conversion is necessary to prepare
configuration for use in our Clojure code.

## Installation

Available via Clojars, add the following dependency to your project:

``` clj
[listora/constraint.config "0.1.0"]
```

## Getting Started

Everything happens in the `constraint.config` namespace.

``` clj
(require '[constraint.config :as conf])
```

The most basic transformation returns the configuration variable
unmodified.

``` clj
(conf/transform-config {:user true} {:user "harry"})
;; => {:config {:user "harry"} ...}
```

### Transforming structure via `:path`

Quite often you want to group logical pieces of configuration to make
destructuring simple, and we often use this to make setting up
[Components][] in our systems more straight forward.

This is supported by adding an optional `:path` to the definition of a
configuration variable.

``` clj
(conf/transform-config {:app-name {:path [:app :name]}}
                       {:app-name "My App"})
;; => {:config {:app {:name "My App"}} ...}
```

### Transforming types via `:constraint`

You can use [Constraint][] to verify and transform the incoming
configuration from a simple string into a more useful type using the
`:constraint` key.

``` clj
(conf/transform-config {:port {:path [:web :port]
                               :constraint Long}}
                       {:port "3000"})
;; => {:config {:web {:port 3000}} ...}
```

The following type transformations are available out of the box:

| Environment   | Constraint | Value   |
|---------------|------------|---------|
| `PORT=3000`   | Long       | `3000`  |
| `ENABLE=true` | Boolean    | `true`  |
| `ENABLE=`     | Boolean    | `false` |

Although support for adding new transformations is not currently
supported, suggestions for new built-in transformation are welcome,
and we can easily expose the string transformations should there be
interest.

## Usage

``` clj
(require '[constraint.config :as conf])

;; Define a mapping from the incoming flat configuration
(def config-definition
  {:user true
   :port {:constraint Long}
   :enable-beta {:constraint Boolean}
   :broker-url {:path [:broker :url]}
   :broker-threads {:path [:broker :threads]
                    :constraint Long}})

(def env
  {:user "bert"
   :port "3000"
   :enable-beta "true"
   :broker-url "amqp://localhost:5672/development"
   :broker-thread "5"})

;; Verify and transform some interesting environment variables
(conf/verify-config env)

;; Keys are nested as per your request, defaulting to where they came
;; from. Constraints are applied and transformed values returned.
{:user "bert"
 :port 3000
 :enable-beta true
 :broker {:threads 5
          :url "amqp://localhost:5672/development"}}
```

## License

Copyright © 2014 Listora

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[Components]: https://github.com/stuartsierra/component
[Constraint]: https://github.com/listora/constraint
[Twelve Factor App]: http://12factor.net/
