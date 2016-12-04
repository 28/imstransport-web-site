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
            ol.geom.Point))

(enable-console-print!)

(defn draw-end-handler []
  (js/alert "Function"))

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
