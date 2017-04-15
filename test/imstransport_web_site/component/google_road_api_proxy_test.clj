(ns imstransport-web-site.component.google-road-api-proxy-test
  (:require [clojure.test :refer :all]
            [imstransport-web-site.component.google-road-api-proxy :refer :all]
            [stub-http.core :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as client]))

(declare ^:dynamic *stub-server*)

(defn start-and-stop-stub-server [f]
  (binding [*stub-server* (start! {{:method :get
                                    :path "/maps/api/distancematrix/json"
                                    :query-params {:units "metric"
                                                   :origins "5.5%2C5.5%7C1.1%2C2.2"
                                                   :destinations "1.1%2C2.2%7C3.3%2C4.4"
                                                   :key "abc"
                                                   :language "sr"}}
                                   {:status 200
                                    :content-type "application/json"
                                    :body (json/generate-string
                                           {:destination_addresses ["A" "B"]
                                            :origin_addresses ["C" "A"]
                                            :rows [{:elements [{:distance {:text "1 km"
                                                                           :value 1000}
                                                                :duration {:text "15 mins"
                                                                           :value 900}
                                                                :status "OK"}
                                                               {:distance {:text "18 km"
                                                                           :value 18000}
                                                                :duration {:text "200 mins"
                                                                           :value 12000}
                                                                :status "OK"}]}
                                                   {:elements [{:distance {:text "8 km"
                                                                           :value 8000}
                                                                :duration {:text "15 mins"
                                                                           :value 900}
                                                                :status "OK"}
                                                               {:distance {:text "1 km"
                                                                           :value 1000}
                                                                :duration {:text "2 mins"
                                                                           :value 120}
                                                                :status "OK"}]}]
                                            :status "OK"})}
                                   {:method :get
                                    :path "/maps/api/distancematrix/json"
                                    :query-params {:units "metric"
                                                   :origins "5.5%2C5.5%7C0.0%2C0.0"
                                                   :destinations "0.0%2C0.0%7C0.0%2C0.0"
                                                   :key "abc"
                                                   :language "sr"}}
                                   {:status 200
                                    :content-type "application/json"
                                    :body (json/generate-string
                                           {:destination_addresses []
                                            :origin_addresses []
                                            :rows []
                                            :status "INVALID_REQUEST"})}})]
    (try
      (f)
      (finally
        (.close *stub-server*)))))

(use-fixtures :each start-and-stop-stub-server)

(defn- get-proxy
  []
  (google-road-api-proxy {:dm-api-key "abc"
                          :dm-base-url (str (:uri *stub-server*) "/maps/api/distancematrix/")
                          :starting-point {:lat "5.5" :long "5.5"}}))

(deftest proxy-handles-everything-ok-response
  (let [proxy (get-proxy)
        response (get-distance proxy {:lat 1.1 :long 2.2} {:lat 3.3 :long 4.4})]
    (is (and
         (:success response)
         (= ["C" "A"] (:origin-addresses response))
         (= ["A" "B"] (:destination-addresses response))
         (= 2000 (:total-distance-m response))))))

(deftest proxy-handles-invalid-request-response
  (let [proxy (get-proxy)
        response (get-distance proxy {:lat 0.0 :long 0.0} {:lat 0.0 :long 0.0})]
    (is (and
         (false? (:success response))
         (:error-flag response)
         (:error-message response)))))
