(ns imstransport-web-site.component.google-road-api-proxy
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as st]
            [imstransport-web-site.util.util :as url]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [schema.core :as s]
            [imstransport-web-site.component.logger-component :refer :all]))

(def ^:private google-api-format "json")
(def ^:private google-api-units "metric")
(def ^:private GoogleResponseSchema {:destination_addresses [s/Str]
                                     :origin_addresses [s/Str]
                                     :rows [{:elements [{:distance {:text s/Str
                                                                    :value s/Num}
                                                         :duration {:text s/Str
                                                                    :value s/Num}
                                                         :status s/Str}]}]
                                     :status s/Str})

(defn- format-coordinates
  [f s]
  (st/join "|" (map #(st/join "," (vals %)) [f s])))

(defn- request-url
  [base-url api-key starting-point origin dest]
  (let [base (str base-url google-api-format)]
    (url/create-url base {"units" google-api-units
                          "origins" (format-coordinates starting-point origin)
                          "destinations" (format-coordinates origin dest)
                          "key" api-key})))

(defn- validate-google-data
  [data]
  (try (s/validate GoogleResponseSchema data)
       true
       (catch Exception _ false)))

(defn- status-ok?
  [resp]
  (when (validate-google-data resp)
    (and
     (= (:status resp) "OK")
     (every?
      (fn [e]
        (every?
         (fn [e1] (= (:status e1) "OK"))
         (:elements e)))
      (:rows resp)))))

(defn- error-response
  [flag msg]
  {:success false
   :error-flag flag
   :error-message msg})

(defn- convert-results
  [google-response]
  (let [d (fn [r] (get-in r [:distance :value]))
        t (fn [r] (get-in r [:duration :value]))
        froute (get-in google-response [:rows 0 :elements 0])
        sroute (get-in google-response [:rows 1 :elements 1])]
    {:success true
     :destination-addresses (:destination_addresses google-response)
     :origin-addresses (:origin_addresses google-response)
     :total-distance-m (+ (d froute) (d sroute))
     :total-duration-s (+ (t froute) (t sroute))}))

(defprotocol GoogleRoadApiBind
  (get-distance [this origin destination]))

(defrecord GoogleRoadApiProxy [api-key base-url starting-point]
  GoogleRoadApiBind
  (get-distance [this origin destination]
    (log :info "get-distance" origin destination)
    (try (let [u (request-url base-url api-key starting-point origin destination)
               json-response (http/get u {:accept :json})
               response (json/parse-string (:body json-response) true)]
           (log :debug "Tried Google request: " u)
           (log :info "Google response: " response)
           (if (status-ok? response)
             (convert-results response)
             (error-response :invalid-google-request (:status response))))
         (catch Exception e (error-response :internal (. e getMessage))))))

(defn google-road-api-proxy [config]
  (->GoogleRoadApiProxy (:dm-api-key config)
                        (:dm-base-url config)
                        (:starting-point config)))
