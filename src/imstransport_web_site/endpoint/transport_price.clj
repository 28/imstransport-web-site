(ns imstransport-web-site.endpoint.transport-price
  (:require [compojure.core :refer :all]
            [schema.core :as s]
            [imstransport-web-site.component.google-road-api-proxy :refer :all]
            [imstransport-web-site.component.message-repository :refer :all]
            [imstransport-web-site.component.logger-component :refer :all]))

;; Input data validation

(def ^{:private true} TransportData {:origin {:lat s/Num :long s/Num}
                                     :dest {:lat s/Num :long s/Num}}) 

(defn- valid-input-data? [data]
  (try (s/validate TransportData data)
       true
       (catch Exception _ false)))

;; Response construction

(defn- wrap-response [body]
  (log :info "OK response: " body)
  {:headers {"Content-Type" "application/json; charset=utf-8"}
   :body body})

(defn- error-response [flag msg-repo & msg-args]
  (log :info "Error response: " flag msg-args)
  {:status (cond
             (= flag :invalid-google-request) 400
             (= flag :internal) 500
             (= flag :invalid-request) 400
             :else 500)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body {:error-flag flag
          :error-message (get-message msg-repo flag msg-args)}})

;; Transport route and price details

(defn- calculate-out-belgrade-price
  [fix dist km]
  (+ fix
     (int (Math/ceil (* (double (/ dist 1000)) km)))))

(defn- transport-not-in-serbia?
  [data-map]
  ;; TODO - Check coordinates here
  false)

(defn- response-not-in-serbia
  [{:keys [dm repo]}]
  (wrap-response (conj dm
                       {:info-message (get-message repo :not-in-serbia nil)})))

(defn- transport-in-belgrade?
  [data-map]
  ;; TODO - Check coordinates here
  false)

(defn- response-in-belgrade
  [{dm :dm price :bg-fixed-price repo :repo}]
  (wrap-response (conj dm
                       {:price price
                        :info-message (get-message repo :in-belgrade price)})))

(defn- response-not-in-belgrade
  [{dm :dm fix :fixed-price-part km :km-factor repo :repo}]
  (let [price (calculate-out-belgrade-price fix (:total-distance-m dm) km)]
    (wrap-response (conj
                    dm
                    {:price price
                     :info-message (get-message repo :not-in-belgrade price)}))))

(defn get-transport-details
  [proxy config {origin :origin dest :dest :as input} msg-repo]
  (let [distance-map (get-distance proxy origin dest)
        all-map (merge input
                       config
                       {:dm (dissoc distance-map :success)
                        :repo msg-repo})]
    (if (:success distance-map)
      (cond
        (transport-not-in-serbia? all-map) (response-not-in-serbia all-map)
        (transport-in-belgrade? all-map) (response-in-belgrade all-map)
        :else (response-not-in-belgrade all-map))
      (error-response (:error-flag distance-map)
                      msg-repo
                      (:error-message distance-map)))))

;; Request handling

(defn handle-request
  [proxy config input msg-repo]
  (log :info "Handling transport price request" input)
  (if (valid-input-data? input)
    (get-transport-details proxy config input msg-repo)
    (error-response :invalid-request msg-repo)))

(defn transport-price-endpoint
  [config]
  (context "/api" []
           (POST "/" req (handle-request
                          (:google-api config)
                          (:transport-config config)
                          (:params req)
                          (:messages config)))))
