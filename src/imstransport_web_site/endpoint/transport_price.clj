(ns imstransport-web-site.endpoint.transport-price
  (:require [compojure.core :refer :all]
            [schema.core :as s]
            [imstransport-web-site.component.google-road-api-proxy :refer :all]))

;; Input data validation

(def ^{:private true} TransportData {:origin {:lat s/Num :long s/Num}
                                     :dest {:lat s/Num :long s/Num}}) 

(defn- valid-input-data? [data]
  (try (s/validate TransportData data)
       true
       (catch Exception _ false)))

;; Response construction

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

;; Transport route and price details

(defn- calculate-out-belgrade-price
  [fix dist km]
  (+ fix
     (int (Math/ceil (* (double (/ dist 1000)) km)))))

(defn- transport-not-in-serbia?
  [data-map]
  ;; TODO - Check coordinates here
  true)

(defn- response-not-in-serbia
  [data-map]
  {:info-message})

(defn- transport-in-belgrade?
  [data-map]
  ;; TODO - Check coordinates here
  false)

(defn- response-in-belgrade
  [{dm :dm price :bg-fixed-price}]
  (wrap-response (conj dm {:price price})))

(defn- response-not-in-belgrade
  [{dm :dm dist {:total-distance-m dm} fix :fixed-price-part km :km-factor}]
  (wrap-response (conj
                  dm
                  {:price (calculate-out-belgrade-price fix dist km)})))

(defn get-transport-details
  [proxy config {origin :origin dest :dest :as input}]
  (let [distance-map (get-distance proxy origin dest)
        all-map (merge input config {:dm distance-map})]
    (if (:success distance-map)
      (cond
        (transport-not-in-serbia? all-map) (response-not-in-serbia all-map)
        (transport-in-belgrade? all-map) (response-in-belgrade all-map)
        :else (response-not-in-belgrade all-map))
      (error-response (:error-flag distance-map)
                      (:error-message distance-map)))))

;; Request handling

(defn handle-request
  [proxy config input]
  (if (valid-input-data? input)
    (get-transport-details proxy input config)
    (error-response :invalid-request (str "Input data is not valid!"))))

(defn transport-price-endpoint
  [config]
  (context "/api" []
           (POST "/" req (handle-request
                          (:google-api config)
                          (:transport-config config)
                          (:params req)))))
