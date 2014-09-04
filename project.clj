(defproject listora/constraint-config "0.1.2"
  :description "Verify and transform you application config"
  :url "https://github.com/listora/constraint-config"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [listora/constraint "0.0.6"]]

  :deploy-repositories [["releases" :clojars]]

  :profiles {:dev {:plugins [[lein-difftest "2.0.0"]
                             [listora/whitespace-linter "0.1.0"]
                             [jonase/eastwood "0.1.1"]]

                   :aliases {"lint" ["do"
                                     ["whitespace-linter"]
                                     ["eastwood"]]}}})
