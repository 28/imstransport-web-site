(ns imstransport-web-site.endpoint.transport-price-test
  (:require [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [shrubbery.core :as shrub]
            [ring.mock.request :as mock]
            [ring.middleware.json :as rj]
            [ring.middleware.keyword-params :as kwp]
            [cheshire.core :as json]
            [imstransport-web-site.component.google-road-api-proxy :as google-proxy]
            [imstransport-web-site.endpoint.transport-price :as transport-price]))

(def google-api-stub
  (shrub/stub google-proxy/GoogleRoadApiBind
              {:get-distance {:destination-addresses ["A"]
                              :origin-addresses ["B"]
                              :total-distance "1 km"
                              :total-duration "1 h"
                              :total-distance-m 1000
                              :total-duration-s 60}}))

(def handler
  (-> (transport-price/transport-price-endpoint {:google-api google-api-stub
                                                 :km-factor 14})
      kwp/wrap-keyword-params
      rj/wrap-json-params
      rj/wrap-json-response)) ;; This happens on system start here is only replicated - todo - see if there is a better way for this.

(deftest response-test
  (testing "endpoint returns a response"
    (let [data {:origin {:lat 1 :long 2}
                :dest {:lat 3 :long 4}
                :in-belgrade true}
          data-json (json/generate-string data)
          request (-> (mock/request :post "/api" data-json)
                      (mock/content-type "application/json"))
          response (handler request)]
      (is (= (:status response) 200))
      (is (= (:price (json/parse-string (:body response) true)) 14)))))
