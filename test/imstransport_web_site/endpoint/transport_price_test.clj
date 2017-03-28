(ns imstransport-web-site.endpoint.transport-price-test
  (:require [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [shrubbery.core :as shrub]
            [ring.mock.request :as mock]
            [ring.middleware.json :as rj]
            [ring.middleware.keyword-params :as kwp]
            [cheshire.core :as json]
            [imstransport-web-site.component.google-road-api-proxy :as google-proxy]
            [imstransport-web-site.endpoint.transport-price :as transport-price]
            [imstransport-web-site.component.message-repository :as repo]))

(def google-api-stub
  (shrub/stub google-proxy/GoogleRoadApiBind
              {:get-distance {:success true
                              :destination-addresses ["A"]
                              :origin-addresses ["B"]
                              :total-distance "1 km"
                              :total-duration "1 h"
                              :total-distance-m 1000
                              :total-duration-s 60}}))

(def repo-stub
  (shrub/stub repo/IMessageRepository
              {:get-message "msg"}))

(def handler
  (-> (transport-price/transport-price-endpoint {:google-api google-api-stub
                                                 :transport-config
                                                 {:km-factor 14
                                                  :fixed-price-part 0
                                                  :bg-fixed-price 2500
                                                  :bg-poly '(((1 1) (1 1))
                                                             ((1 1) (1 1))
                                                             ((1 1) (1 1))
                                                             ((1 1) (1 1)))
                                                  :serbia-poly '(((1 1) (1 10))
                                                                 ((1 10) (10 10))
                                                                 ((10 1) (10 10))
                                                                 ((1 1) (10 1)))}
                                                 :messages repo-stub})
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
                                     :dest {:lat 3 :long 4}})
          r (json/parse-string (:body response) true)]
      (is (= (:status response) 200))
      (is (= (:price r) 14))
      (is (= (:destination-addresses r) ["A"]))
      (is (= (:origin-addresses r) ["B"]))
      (is (= (:total-distance r) "1 km"))
      (is (= (:total-duration r) "1 h"))
      (is (= (:total-distance-m r) 1000))
      (is (= (:total-duration-s r) 60))
      (is (= (:info-message r) "msg"))
      (is (nil? (:success r))))) ;; succes flag is removed from end map
  (testing "endpoint handles a bad request"
    (let [response (execute-request {:origin {:lat 1 :long 2}
                                     :dest {:lat 3}})
          r (json/parse-string (:body response) true)]
      (is (= (:status response) 400))
      (is (= (:error-flag r) "invalid-request"))
      (is (= (:error-message r) "msg")))))
