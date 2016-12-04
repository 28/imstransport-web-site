(ns cljs.map
    (:require ol.Map
            ol.Collection
            ol.source.Vector
            ol.layer.Vector
            ol.layer.Tile
            ol.View
            ol.proj
            ol.extent
            ol.source.OSM
            ol.interaction.Draw
            ol.geom.Polygon
            ol.geom.Point
            [ajax.core :refer [GET POST]]
            [cljs.reader :as reader]))

(enable-console-print!)

(defn get-price-information [response]
  (let [price (aget  (.-arr response) 1)
        message (str "Cena je: " price " dinara.")]
  (js/alert  message)))

(defn draw-end-handler [e]

  (let [coords (.getCoordinates (.getGeometry (.-feature e)))
        first-coord (aget js/coords 0)
        end-coord (aget js/coords 1)]
    (POST "/api"
    {:params {:origin {:lat (aget js/first-coord 0) :long (aget js/first-coord 1)}
                        :dest {:lat (aget js/end-coord 0) :long (aget js/end-coord 1)}
                        :in-belgrade true}
     :format :json
     :response-format :json
     :handler get-price-information})))

(defn create-transport-map []
  (let [rasterSource (ol.source.OSM. #js {:layer "sat"})
        rasterLayer (ol.layer.Tile. #js {:source rasterSource})
        view (ol.View. #js {:center (ol.proj.fromLonLat #js [20.4489 44.7866])
                            :zoom 11
                            :maxZoom 18})
        vectorSource (ol.source.Vector.)
        vectorLayer  (ol.layer.Vector. #js {:source vectorSource})
        drawInteraction (ol.interaction.Draw. #js {:source vectorSource
                                                   :type "LineString"})
       map (ol.Map. #js { :layers #js [rasterLayer, vectorLayer]
                      :target "map"
                      :view   view
                      })]
        (do
          (.addInteraction map drawInteraction)
          (.on drawInteraction "drawend" draw-end-handler))))

(set! (.-onload js/window)
  (fn []
    (create-transport-map)))
