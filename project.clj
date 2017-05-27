(defproject imstransport-web-site "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.456"]
                 [com.stuartsierra/component "0.3.1"]
                 [compojure "1.5.1"]
                 [duct "0.8.2"]
                 [environ "1.1.0"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring-jetty-component "0.3.1"]
                 [ring-webjars "0.1.1"]
                 [org.slf4j/slf4j-nop "1.7.21"]
                 [org.webjars/normalize.css "3.0.2"]
                 [cljsjs/openlayers "3.15.1"]
                 [cljsjs/react "15.3.1-0"]
                 [cljsjs/react-dom "15.3.1-0"]
                 [cljs-ajax "0.5.8"]
                 [org.omcljs/om "1.0.0-alpha46"]
                 [prismatic/schema "1.1.3"]
                 [cheshire "5.6.3"]
                 [clj-http "2.3.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [clojurewerkz/propertied "1.2.0"]
                 [keybind "2.0.0"]]
  :plugins [[lein-environ "1.0.3"]
            [lein-cljsbuild "1.1.2"]
            [lein-ring "0.12.0"]]
  :main ^:skip-aot imstransport-web-site.main
  :uberjar-name "ims-standalone.jar"
  :ring {:handler duct.component.handler/handler-component
         :uberwar-name "ims.war"}
  :target-path "target/%s/"
  :resource-paths ["resources" "target/cljsbuild"]
  :prep-tasks [["javac"] ["cljsbuild" "once"] ["compile"]]
  :cljsbuild
  {:builds
   [{:id "main"
     :jar true
     :source-paths ["src-cljs"]
     :compiler
     {:main "cljs.map"
      :output-to     "target/cljsbuild/imstransport_web_site/public/js/main.js"
      :optimizations :advanced
      :closure-extra-annotations #{"api" "observable"}
      :closure-warnings {:externs-validation :off}}}]}
  :aliases {"setup"  ["run" "-m" "duct.util.repl/setup"]
            "deploy" ["do"
                      ["vcs" "assert-committed"]
                      ["vcs" "push" "heroku" "master"]]}
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :repl {:resource-paths ^:replace ["resources" "dev/resources" "target/figwheel"]
          :prep-tasks     ^:replace [["javac"] ["compile"]]}
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:dependencies [[duct/generate "0.8.2"]
                                  [reloaded.repl "0.2.3"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [eftest "0.1.3"]
                                  [com.gearswithingears/shrubbery "0.4.1"]
                                  [kerodon "0.8.0"]
                                  [binaryage/devtools "0.8.2"]
                                  [duct/figwheel-component "0.3.3"]
                                  [figwheel "0.5.8"]
                                  [ring/ring-mock "0.3.0"]
                                  [se.haleby/stub-http "0.2.1"]]
                   :source-paths   ["dev/src" "src-cljs"]
                   :resource-paths ["dev/resources"]
                   :repl-options {:init-ns user}
                   :env {:port "3000"}}
   :project/test  {}})
