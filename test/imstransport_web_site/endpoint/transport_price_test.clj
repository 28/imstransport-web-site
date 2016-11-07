(ns imstransport-web-site.endpoint.transport-price-test
  (:require [clojure.test :refer :all]
            [shrubbery.core :as shrub]
            [imstransport-web-site.endpoint.transport-price :as transport-price]))

(def handler
  (transport-price/transport-price-endpoint {}))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
