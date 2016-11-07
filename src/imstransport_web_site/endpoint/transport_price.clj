(ns imstransport-web-site.endpoint.transport-price
  (:require [compojure.core :refer :all]))

(defn transport-price-endpoint [config]
  (context "/api" []
   (GET "/" [] "Hello World")))
