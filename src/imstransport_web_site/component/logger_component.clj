(ns imstransport-web-site.component.logger-component
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(defprotocol Logger
  (log [this lvl msg]))

(defrecord LoggerComponent [config]
  component/Lifecycle
  (start [this]
    (timbre/merge-config!
     {:level (:global-level config)
      :appenders {:spit (appenders/spit-appender {:fname (:filename config)})
                  :println {:enabled? false}}})
    (timbre/info "Service started!")
    this)
  (stop [this]
    (timbre/info "Service stopping...")
    this)

  Logger
  (log [this lvl msg]
    (timbre/log lvl msg)))

(defn logger-component [config]
  (->LoggerComponent config))
