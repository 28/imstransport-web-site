(ns imstransport-web-site.endpoint.transport-price
  (:require [compojure.core :refer :all]
            [schema.core :as s]
            [imstransport-web-site.component.google-road-api-proxy :refer :all]))

(def TransportData {:origin {:lat s/Num :long s/Num}
                    :dest {:lat s/Num :long s/Num}}) 

(defn valid-input-data? [data]
  (try (s/validate TransportData data)
       true
       (catch Exception _ false)))

(defn- wrap-response [body]
  {:body body})

(defn- error-response [flag msg]
  {:status (cond
             (= flag :invalid-google-request) 400
             (= flag :internal) 500
             (= flag :invalid-request) 400
             :else 500)
   :headers {"Content-Type" "application/json"}
   :body {:price nil
          :error-message msg}})

(defn- transport-in-belgrade?
  [input config]
  ;; TODO - Check coordinates here
  false)

(defn- calculate-belgrade-price
  [distance-map config]
  (:bg-fixed-price config))

(defn- calculate-price
  [distance-map config]
  ;; TODO - Refactor this (with list comprehention)
  (+ (:fixed-price-part config) (int (Math/ceil (* (double (/ (:total-distance-m distance-map) 1000)) (:km-factor config))))))

(defn- get-response
  [calc-fn distance-map config]
  (wrap-response (conj distance-map {:price (calc-fn distance-map config)})))

(defn get-transport-details
  [proxy input config]
  (let [distance-map (get-distance proxy (:origin input) (:dest input))]
    (if-not (:error-message distance-map)
      (if (transport-in-belgrade? input config)
        (get-response calculate-belgrade-price distance-map config)
        (get-response calculate-price distance-map config))
      (error-response (:error-flag distance-map) (:error-message distance-map)))))

(defn handle-request
  [proxy input config]
  (if (valid-input-data? input)
    (get-transport-details proxy input config)
    (error-response :invalid-request (str "Input data is not valid!"))))

(defn transport-price-endpoint
  [config]
  (context "/api" []
           (POST "/" req (handle-request
                          (:google-api config)
                          (:params req)
                          (:transport-config config)))))
