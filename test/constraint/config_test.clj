(ns constraint.config-test
  (:import clojure.lang.Keyword)
  (:require [clojure.test :refer :all]
            [constraint.config :refer :all]
            [constraint.core :refer [?]]))

(def config-in
  {:shell "zsh"
   :user-name "Bob"
   :user-email "bob@example.com"
   :user-age 21
   :enable-alpha-features ""
   :enable-beta-features "true"
   :log-level "info"
   :port "3000"})

(def definition
  {:shell String
   :user-name {:path [:user :name]}
   :user-email {:path [:user :email]}
   :user-age {:path [:user :age] :constraint Long}
   :enable-alpha-features {:constraint Boolean}
   :enable-beta-features {:constraint Boolean}
   (? :memcached) String
   (? :max-threads) {:path [:threads :max]}
   :log-level Keyword
   :port {:constraint Long}})

(def expected-config-keys
  [:enable-alpha-features
   :enable-beta-features
   :log-level
   :memcached
   :port
   :shell
   :threads
   :user])

(deftest test-transform-config
  (let [{:keys [config errors]} (transform-config definition config-in)]
    (is (empty? errors))
    (is (= (-> config keys sort) expected-config-keys))
    (are [key-seq v] (= (get-in config key-seq) v)
         [:shell] "zsh"
         [:user :name] "Bob"
         [:user :email] "bob@example.com"
         [:user :age] 21
         [:enable-alpha-features] false
         [:enable-beta-features] true
         [:log-level] :info
         [:memcached] nil
         [:threads :max] nil
         [:port] 3000)))

(deftest test-transform-config-with-errors
  (let [invalid-config-in (assoc config-in :not-allowed "We are strict")
        {:keys [config errors]} (transform-config definition invalid-config-in)
        error (first errors)]
    (is (nil? config))
    (is (= (count errors) 1))
    (is (= (:error error) :unexpected-keys))
    (is (= (:found error) #{:not-allowed}))))

(deftest test-verify-config-with-errors
  (testing "with no errors"
    (let [config (verify-config definition config-in)]
      (testing "it returns the config itself"
        (is (= (-> config keys sort) expected-config-keys))
        (is (not (contains? config :errors)))
        (is (not (contains? config :value))))))

  (testing "with errors"
    (is (thrown? Exception (verify-config definition {:invalid true})))))
