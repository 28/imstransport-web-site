(ns imstransport-web-site.component.google-road-api-proxy
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as st]
            [imstransport-web-site.util.util :as url]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [imstransport-web-site.component.logger-component :refer :all]))

(def ^:private google-api-format "json")
(def ^:private google-api-units "metric")

(defn- format-coordinates
  [c]
  (st/join "," (vals c)))

(defn- request-url
  [base-url api-key origin dest]
  (let [base (str base-url google-api-format)]
    (url/create-url base {"units" google-api-units
                          "origins" (format-coordinates origin)
                          "destinations" (format-coordinates dest)
                          "key" api-key})))
(defn- status-ok?
  [resp]
  (and
   (= (:status resp) "OK")
   (every?
    (fn [e]
      (every?
       (fn [e1] (= (:status e1) "OK"))
       (:elements e)))
    (:rows resp))))

(defn- error-response
  [flag msg]
  {:success false
   :error-flag flag
   :error-message msg})

(defn- convert-results
  [google-response]
  {:success true
   :destination-addresses (:destination_addresses google-response)
   :origin-addresses (:origin_addresses google-response)
   :total-distance (-> google-response :rows first :elements first :distance :text)
   :total-duration (-> google-response :rows first :elements first :duration :text)
   :total-distance-m (-> google-response :rows first :elements first :distance :value)
   :total-duration-s (-> google-response :rows first :elements first :duration :value)})

(defprotocol GoogleRoadApiBind
  (get-distance [this origin destination]))

(defrecord GoogleRoadApiProxy [api-key base-url]
  GoogleRoadApiBind
  (get-distance [this origin destination]
    (log :info "get-distance" origin destination)
    (try (let [u (request-url base-url api-key origin destination)
               json-response (http/get u {:accept :json})
               response (json/parse-string (:body json-response) true)]
           (log :info "Google response: " response)
           (if (status-ok? response)
             (convert-results response)
             (error-response :invalid-google-request (:status response))))
         (catch Exception e (error-response :internal (. e getMessage))))))

(defn google-road-api-proxy [config]
  (->GoogleRoadApiProxy (:dm-api-key config) (:dm-base-url config)))
