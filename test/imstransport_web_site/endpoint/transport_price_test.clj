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
                                                 :transport-config
                                                 {:km-factor 14
                                                  :fuel-factor 1
                                                  :fixed-price-part 0
                                                  :bg-fixed-price 2500
                                                  :bg-coord {:ul-lat 4
                                                             :ul-long 4
                                                             :ur-lat 4
                                                             :ur-long 4
                                                             :bl-lat 4
                                                             :bl-long 4
                                                             :br-lat 4
                                                             :br-long 4}}})
      kwp/wrap-keyword-params
      rj/wrap-json-params
      rj/wrap-json-response)) ;; This happens on system start here is only replicated - todo - see if there is a better way for this.

(defn- execute-request
  [data]
  (let [data-json (json/generate-string data)
        request (-> (mock/request :post "/api" data-json)
                    (mock/content-type "application/json"))]
    (handler request)))

(deftest response-test
  (testing "endpoint returns a response"
    (let [response (execute-request {:origin {:lat 1 :long 2}
                                     :dest {:lat 3 :long 4}})]
      (is (= (:status response) 200))
      (is (= (:price (json/parse-string (:body response) true)) 14))))
  (testing "endpoint handles a bad request"
    (let [response (execute-request {:origin {:lat 1 :long 2}
                                     :dest {:lat 3}})]
      (is (= (:status response) 400))
      (is (:error-message (json/parse-string (:body response) true))))))
