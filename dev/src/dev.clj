(ns dev
  (:refer-clojure :exclude [test])
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [duct.generate :as gen]
            [duct.util.repl :refer [setup test cljs-repl migrate rollback]]
            [duct.util.system :refer [load-system]]
            [reloaded.repl :refer [system init start stop go reset]]
            [duct.component.figwheel :as figwheel]
            [imstransport-web-site.util.util :as util]))

(defn new-system []
  (let [coords (util/load-resource-edn "imstransport_web_site/polygon.edn")]
    (load-system (keep io/resource ["imstransport_web_site/system.edn" "dev.edn" "local.edn"])
                 {'bg-poly (-> coords :bg-poly util/linefy-poly-coords)
                  'serbia-poly (-> coords :serbia-poly util/linefy-poly-coords)})))

(when (io/resource "local.clj")
  (load "local"))

(gen/set-ns-prefix 'imstransport-web-site)

(reloaded.repl/set-init! new-system)

;; Figwheel util

(defn reload-cljs []
  (figwheel/build-cljs (:figwheel system)))
