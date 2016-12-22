(ns imstransport-web-site.endpoint.transport-price
  (:require [compojure.core :refer :all]
            [schema.core :as s]
            [imstransport-web-site.component.google-road-api-proxy :refer :all]))

(def TransportData {:origin {:lat s/Num :long s/Num}
                    :dest {:lat s/Num :long s/Num}
                    :in-belgrade s/Bool}) 

(defn valid-input-data? [data]
  (try (s/validate TransportData data)
       true
       (catch Exception _ false)))

(defn- wrap-response [body]
  {:body body})

(defn- error-response [msg]
  (wrap-response {:price nil
                  :error-message msg}))

(defn- calculate
  [dist km-factor]
   (int (Math/ceil (* (double (/ dist 1000)) km-factor))))

(defn do-calculate-price [proxy input km-factor]
  (let [distance-map (get-distance proxy (:origin input) (:dest input))]
    (if-not (:error-message distance-map)
      (wrap-response (conj distance-map {:price (calculate (:total-distance-m distance-map) km-factor)}))
      (error-response (:error-message distance-map)))))

(defn calculate-price [proxy input km-factor]
  (if (valid-input-data? input)
    (do-calculate-price proxy input km-factor)
    (error-response (str "Input data is not valid!"))))

(defn transport-price-endpoint [config]
  (context "/api" []
           (POST "/" req (calculate-price
                          (:google-api config)
                          (:params req)
                          (:km-factor config)))))
