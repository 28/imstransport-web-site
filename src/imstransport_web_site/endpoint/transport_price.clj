(ns imstransport-web-site.endpoint.transport-price
  (:require [compojure.core :refer :all]
            [schema.core :as s]
            [imstransport-web-site.component.google-road-api-proxy :refer :all]))

(def TransportData {:origin {:lat s/Num :long s/Num}
                    :dest {:lat s/Num :long s/Num}
                    :in-belgrade s/Bool}) 

(defn- wrap-response [body]
  {:body body})

(defn- error-response [msg]
  (str "Error in request: " msg))

(defn valid-input-data? [data]
  (try (s/validate TransportData data)
       true
       (catch Exception e false)))

(defn do-calculate-price [proxy input]
  (if-let [distance (get-distance proxy (:origin input) (:dest input))]
    (wrap-response {:price 100})
    (error-response "Internal server error.")))

(defn calculate-price [proxy input]
  (if (valid-input-data? input)
    (do-calculate-price proxy input)
    (error-response (str "Input data is not valid!"))))

(defn transport-price-endpoint [config]
  (context "/api" []
           (POST "/" req (calculate-price
                          (:google-api config)
                          (:params req)))))
