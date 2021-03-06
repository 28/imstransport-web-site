(ns imstransport-web-site.main
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [duct.util.runtime :refer [add-shutdown-hook]]
            [duct.util.system :refer [load-system]]
            [environ.core :refer [env]]
            [clojure.java.io :as io]
            [imstransport-web-site.util.util :as util]))

(defn -main [& args]
  (let [google-conf (util/load-resource-edn "imstransport_web_site/google.edn")
        polygons (util/load-resource-edn "imstransport_web_site/polygon.edn")
        bindings {'http-port (Integer/parseInt (:port env "3000"))
                  'dm-api-key (-> google-conf :dm-api-key)
                  'dm-base-url (-> google-conf :dm-base-url)
                  'bg-poly (-> polygons :bg-poly util/linefy-poly-coords)
                  'serbia-poly (-> polygons :serbia-poly util/linefy-poly-coords)}
        system   (->> (load-system [(io/resource "imstransport_web_site/system.edn")] bindings)
                      (component/start))]
    (add-shutdown-hook ::stop-system #(component/stop system))
    (println "Started HTTP server on port" (-> system :http :port))))
