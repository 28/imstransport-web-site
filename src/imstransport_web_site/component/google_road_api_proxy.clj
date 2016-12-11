(ns imstransport-web-site.component.google-road-api-proxy
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as st]
            [imstransport-web-site.util.url :as url]
            [cheshire.core :as json]
            [clj-http.client :as http]))

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
  (println resp)
  (and
   (= (:status resp) "OK")
   (every?
    (fn [e]
      (every?
       (fn [e1] (= (:status e1) "OK"))
       (:elements e)))
    (:rows resp))))

(defprotocol GoogleRoadApiBind
  (get-distance [this origin destination]))

(defrecord GoogleRoadApiProxy [api-key base-url]
  GoogleRoadApiBind
  (get-distance [this origin destination]
    (let [u (request-url base-url api-key origin destination)
          response (http/get u {:accept :json})
          json-response (json/parse-string (:body response) true)]
      (when (status-ok? json-response)
        (println json-response)
        json-response))))

(defn google-road-api-proxy [config]
  (->GoogleRoadApiProxy (:dm-api-key config) (:dm-base-url config)))
