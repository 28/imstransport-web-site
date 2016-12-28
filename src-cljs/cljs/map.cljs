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
            [ajax.core :refer [json-response-format json-request-format ajax-request]]
            [cljs.reader :as reader]))

(enable-console-print!)

(defn get-price-information [[ok response]]
  (let [response-obj (clj->js response)
        price (aget response-obj "price")
        message (str "Cena je: " price " dinara.")]
    (js/console.log response-obj)
    (js/alert  message)))

(defn draw-end-handler [e]
  (let [coords (.getCoordinates (.getGeometry (.-feature e)))
        first-coord (aget js/coords 0)
        end-coord (aget js/coords 1)]
    (ajax-request
     {:uri "/api"
      :method :post
      :params {:origin {:lat (aget js/first-coord 1) :long (aget js/first-coord 0)}
               :dest {:lat (aget js/end-coord 1) :long (aget js/end-coord 0)}}
      :handler get-price-information
      :response-format :json
      :format :json
      :keywords? true})))

(defn create-transport-map []
  (let [rasterSource (ol.source.OSM. #js {:layer "sat"})
        rasterLayer (ol.layer.Tile. #js {:source rasterSource})
        view (ol.View. #js {:center  #js [20.4489 44.7866]
                            :zoom 11
                            :extent #js [18.5 40.4 22.7 47.5]
                            :minZoom 7
                            :maxZoom 19
                            :projection "EPSG:4326"})
        vectorSource (ol.source.Vector.)
        vectorLayer  (ol.layer.Vector. #js {:source vectorSource})
        drawInteraction (ol.interaction.Draw. #js {:source vectorSource
                                                   :type "LineString"})
        map (ol.Map. #js {:layers #js [rasterLayer, vectorLayer]
                          :target "map"
                          :view view})]
    (.addInteraction map drawInteraction)
    (.on drawInteraction "drawend" draw-end-handler)))

(set! (.-onload js/window)
      (fn []
        (create-transport-map)))
