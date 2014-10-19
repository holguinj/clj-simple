(ns clj-simple.core-test
  (:require [clojure.test :refer :all]
            [clj-simple.core :refer :all]))

(deftest json-key->clj-key-test
  (testing "json-key->clj-key converts underscores into dashes and strings into keywords"
    (is (= :safe-to-spend (json-key->clj-key "safe_to_spend")))))
