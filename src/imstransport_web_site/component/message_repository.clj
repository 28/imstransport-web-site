(ns imstransport-web-site.component.message-repository
  (:require [com.stuartsierra.component :as component]
            [clojurewerkz.propertied.properties :as p]
            [clojure.java.io :as io]
            [imstransport-web-site.component.logger-component :refer :all]))

(defprotocol IMessageRepository
  (get-message [this k args]))

(defrecord MessageRepository [filename]
  component/Lifecycle
  (start [this]
    (let [msg (p/load-from (io/resource filename))
          msg-map (p/properties->map msg true)]
      (merge this msg-map)))
  (stop [this] this)

  IMessageRepository
  (get-message [this k args]
    (log :info "get-message" k args)
    (if (nil? args)
      (get this k)
      (apply format (flatten [(get this k) args])))))

(defn message-repository [{:keys [filename]}]
  (->MessageRepository filename))
