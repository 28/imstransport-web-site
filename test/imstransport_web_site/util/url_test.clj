(ns imstransport-web-site.util.url-test
(:require [clojure.test :refer :all]
          [imstransport-web-site.util.url :refer :all]))

(deftest create-url-test
  (testing "create-url-will-create-a-proper-url"
    (let [base "http://localhost"
          params {"a" 1
                  "b" 2
                  "c" "1|2"
                  "d" "ddd"}]
      (is (= (create-url base params) "http://localhost?a=1&b=2&c=1|2&d=ddd")))))
