(ns imstransport-web-site.component.google-road-api-proxy
  (:require [com.stuartsierra.component :as component]))

(defprotocol GoogleRoadApiBind
  (get-distance [this origin destination]))

(defrecord GoogleRoadApiProxy [api-key]
  GoogleRoadApiBind
  (get-distance [this origin destination] {:distance nil}))

(defn google-road-api-proxy [config]
  (->GoogleRoadApiProxy (:api-key config)))
