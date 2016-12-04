(ns imstransport-web-site.util.url
  (:require [clojure.string :as st]))

(defn create-url
  [base params]
  (str base "?" (st/join "&" (map (fn [[k v]] (str k "=" v)) params))))
