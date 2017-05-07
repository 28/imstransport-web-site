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
            ol.Overlay
            ol.extent
            ol.source.OSM
            ol.interaction.Draw
            ol.geom.Polygon
            ol.geom.Point
            [ol.animation :as a :refer [pan bounce]]
            [goog.dom :as dom]
            [goog.events :as events]
            [goog.style :as style]
            [ajax.core :refer [json-response-format json-request-format ajax-request]]
            [cljs.reader :as reader]
            [keybind.core :as key]))

(enable-console-print!)

(def belgrade-center
  #js [20.4489 44.7866])

(defn set-end-message
  [msg od-msg tbe do ec]
  (let [message-el-paragraph (dom/createDom "p" (clj->js {"class" "message-paragraph"}))
        route-el-paragraph (dom/createDom "p" (clj->js {"class" "cloud-p"}))]
    (dom/setTextContent message-el-paragraph msg)
    (dom/setTextContent route-el-paragraph od-msg)
    (dom/appendChild tbe message-el-paragraph)
    (dom/appendChild tbe route-el-paragraph)
    (.setPosition do ec)
    (style/setStyle tbe #js {:display "block"})))

(defn get-price-information
  [end-coord map v-source description-overlay toolbar-element]
  (fn [[ok response]]
    (let [r (clj->js response)
          msg (if (aget r "info-message")
                (aget r "info-message")
                (aget r "error-message"))
          partial-set-end-message #(set-end-message
                                    msg
                                    %
                                    toolbar-element
                                    description-overlay
                                    end-coord)]
      (if ok
        (if-let [da (aget r "destination-addresses")]
          (partial-set-end-message (str "Ruta: " (aget da 0) " - " (aget da 1)))
          (partial-set-end-message nil))
        (let [e (-> r
                    (aget "response")
                    (aget "error-message"))]
          (dom/setTextContent toolbar-element e)
          (.setPosition description-overlay end-coord)
          (style/setStyle toolbar-element #js {:display "block"}))))))

(defn clear
  [v-source toolbar-element]
  (.clear v-source)
  (dom/removeChildren toolbar-element)
  (style/setStyle toolbar-element #js {"display" "none"}))

(defn center-and-clear
  [map v-source toolbar-element]
  (fn [e]
    (let [view (.getView map)
          pan (a/pan #js {:source (.getCenter view)})
          bounce (a/bounce #js {:resolution (.getResolution view)})]
      (clear v-source toolbar-element)
      (.beforeRender map pan)
      (.setCenter view belgrade-center)
      (.beforeRender map bounce)
      (.setZoom view 11))))

(defn display-map-info
  []
  (let [e (dom/getElement "map-info-div")
        d (style/getStyle e "display")]
    (if (= "none" d)
      (style/setStyle e #js {:display "block"})
      (style/setStyle e #js {:display "none"}))))

(defn draw-start-handler [v-source toolbar-element]
  (fn [e]
    (clear v-source toolbar-element)))

(defn draw-end-handler [map v-source description-overlay toolbar-element]
  (fn [e]
    (let [coords (.getCoordinates (.getGeometry (.-feature e)))
          first-coord (aget coords 0)
          end-coord  (last coords)
          req {:uri             "/api"
               :method          :post
               :params          {:origin {:lat (aget first-coord 1) :long (aget first-coord 0)}
                                 :dest   {:lat (aget end-coord 1) :long (aget end-coord 0)}}
               :handler         (get-price-information end-coord map v-source description-overlay toolbar-element)
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
        toolbar-element    (dom/getElement "description-toolbar")
        descriptionOverlay (ol.Overlay. #js {:element toolbar-element})
        resetButton        (dom/createDom "button" (clj->js {"class" "ol-control-button" "id" "resetButton" "title" "Centriraj na Beograd"}))
        infoButton        (dom/createDom "button" (clj->js {"class" "ol-control-button" "id" "infoButton" "title" "Informacije o mapi"}))
        controlElement     (dom/createDom "div" (clj->js {"class" "reset-position ol-unselectable ol-control"}))
        zoomControl        (ol.control.Zoom. #js {})
        map (ol.Map. #js {:layers #js [rasterLayer, vectorLayer]
                          :target "map"
                          :view   view
                          :controls #js []})]
    (dom/setTextContent resetButton "R")
    (dom/setTextContent infoButton "I")
    (dom/appendChild controlElement resetButton)
    (dom/appendChild controlElement infoButton)
    (.addControl map zoomControl)
    (.addControl map (ol.control.Control. #js {:element controlElement}))
    (.addInteraction map drawInteraction)
    (.addOverlay map descriptionOverlay)
    (events/listen resetButton "click" (center-and-clear map vectorSource toolbar-element))
    (events/listen infoButton "click" (fn [e] (display-map-info)))
    (.on drawInteraction "drawend" (draw-end-handler map vectorSource descriptionOverlay toolbar-element))
    (.on drawInteraction "drawstart" (draw-start-handler  vectorSource toolbar-element))
    (key/bind! "esc" ::clear-drawing (fn []
                                       (try
                                         (do
                                           (clear vectorSource toolbar-element)
                                           (. drawInteraction removeLastPoint))
                                         (catch :default _))))))

(set! (.-onload js/window)
      (fn []
        (create-transport-map)))
