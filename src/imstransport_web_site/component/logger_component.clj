(ns imstransport-web-site.component.logger-component
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(defrecord LoggerComponent [config]
  component/Lifecycle
  (start [this]
    (timbre/merge-config!
     {:level (:global-level config)
      :appenders {:spit (appenders/spit-appender {:fname (:filename config)})
                  :println {:enabled? false}}})
    (timbre/debug "Logger service started!")
    this)
  (stop [this]
    (timbre/debug "Logger service stopping...")
    this))

(defn logger-component [config]
  (->LoggerComponent config))

(defn log
  [level msg & args]
  (timbre/log level msg args))
