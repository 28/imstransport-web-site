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
                                                   :origins "1.1,2.2"
                                                   :destinations "3.3,4.4"
                                                   :key "abc"}}
                                   {:status 200
                                    :content-type "application/json"
                                    :body (json/generate-string
                                           {:destination_addresses ["A"]
                                            :origin_addresses ["B"]
                                            :rows [{:elements [{:distance {:text "1 km"
                                                                           :value "1000"}
                                                                :duration {:text "15 mins"
                                                                           :value "900"}
                                                                :status "OK"}]}]
                                            :status "OK"})}})]
    (try
      (f)
      (finally
        (.close *stub-server*)))))

(use-fixtures :each start-and-stop-stub-server)

(deftest Example
  (let [proxy (google-road-api-proxy {:dm-api-key "abc"
                                      :dm-base-url (str (:uri *stub-server*) "/maps/api/distancematrix/")})
        response (get-distance proxy {:lat 1.1 :long 2.2} {:lat 3.3 :long 4.4})]
    (is (= "1 km" (-> response :rows first :elements first :distance :text)))))
