(ns imstransport-web-site.component.google-road-api-proxy
  (:require [com.stuartsierra.component :as component]
            [clojure.string :as st]
            [imstransport-web-site.util.url :as url]))

(def ^:private google-api-format "json")
(def ^:private google-api-units "metric")

(defn- format-coordinates
  [c]
  (st/join "|" (vals c)))

(defn- request-url
  [base-url api-key origin dest]
  (let [base (str base-url "/" google-api-format)]
    (url/create-url base {"units" google-api-units
                          "origins" (format-coordinates origin)
                          "destination" (format-coordinates dest)
                          "key" api-key})))

(defprotocol GoogleRoadApiBind
  (get-distance [this origin destination]))

(defrecord GoogleRoadApiProxy [api-key base-url]
  GoogleRoadApiBind
  (get-distance [this origin destination]
    {:distance nil}))

(defn google-road-api-proxy [config]
  (->GoogleRoadApiProxy (:api-key config) (:base-url config)))
