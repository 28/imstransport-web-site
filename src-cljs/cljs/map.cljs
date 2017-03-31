(ns cljs.map
  (:require ol.Map
            ol.Collection
            ol.source.Vector
            ol.layer.Vector
            ol.layer.Tile
            ol.View
            ol.proj

            ol.control.Zoom
            ol.control.Control
            ol.extent
            ol.source.OSM
            ol.interaction.Draw
            ol.geom.Polygon
            ol.geom.Point
            [ol.animation :as a :refer [pan bounce]]
            [goog.dom :as dom]
            [goog.events :as events]
            [ajax.core :refer [json-response-format json-request-format ajax-request]]
            [cljs.reader :as reader]))


(enable-console-print!)

(def belgrade-center
   #js [20.4489 44.7866])

(def default-resolution)


(defn get-price-information [vSource]
    (fn [[ok response]]
      (let [response-obj (clj->js response)
            message (if (aget response-obj "info-message")
                      (aget response-obj "info-message")
                      (aget response-obj "error-message"))]
         (.clear vSource)
         (js/alert message))))


(defn center-map [map]
  (fn [e]
    (let [view (.getView map)
          pan (a/pan #js {:source (.getCenter view)})
          bounce (a/bounce #js {:resolution (.getResolution view)})]
      (.beforeRender map pan)
      (.setCenter view belgrade-center)
      (.beforeRender map bounce)
      (.setZoom view 11))))


(defn draw-end-handler [vSource]
  (fn [e]
    (let [coords (.getCoordinates (.getGeometry (.-feature e)))
          first-coord (aget coords 0)
          end-coord (aget coords 1)
          req {:uri             "/api"
               :method          :post
               :params          {:origin {:lat (aget first-coord 1) :long (aget first-coord 0)}
                                 :dest   {:lat (aget end-coord 1) :long (aget end-coord 0)}}
               :handler         (get-price-information vSource)
               :response-format (json-response-format {:keywords? true})
               :format          (json-request-format)
               :keywords?       true}]
      (ajax-request req))))


(defn create-transport-map []
  (let [rasterSource (ol.source.OSM. #js {:layer "sat"})
        rasterLayer (ol.layer.Tile. #js {:source rasterSource})
        view (ol.View. #js {:center    belgrade-center
                            :zoom       11
                            :extent     #js [18.5 40.4 22.7 47.5]
                            :minZoom    7
                            :maxZoom    19
                            :projection "EPSG:4326"})
        vectorSource  (ol.source.Vector.)
        vectorLayer (ol.layer.Vector. #js {:source vectorSource})
        drawInteraction (ol.interaction.Draw. #js {:source vectorSource
                                                   :type   "LineString"})
        resetButton    (dom/createDom "button" (clj->js {"class" "ol-control-button" "id" "resetButton" "title" "Centriraj na Beograd"}))
        controlElement (dom/createDom "div" (clj->js {"class" "reset-position ol-unselectable ol-control"}))
        zoomControl (ol.control.Zoom. #js {})
        map (ol.Map. #js {:layers #js [rasterLayer, vectorLayer]
                          :target "map"
                          :view   view
                          :controls #js []})]
    (dom/setTextContent resetButton "R")
    (dom/appendChild controlElement resetButton)

    (.addControl map zoomControl)
    (.addControl map (ol.control.Control. #js {:element controlElement}))
    (.addInteraction map drawInteraction)
    (events/listen resetButton "click" (center-map map))
    (.log js/console "Rezolucija")
    (.log js/console (.getResolution view))
    (.on drawInteraction "drawend" (draw-end-handler vectorSource))))

(set! (.-onload js/window)
      (fn []
        (create-transport-map)))
