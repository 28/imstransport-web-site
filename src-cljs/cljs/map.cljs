(ns cljs.map
    (:require ol.Map
            ol.Collection
            ol.layer.Tile
            ol.View
            ol.proj
            ol.extent
            ol.source.OSM

            ol.style.Style
            ol.style.Fill
            ol.style.Stroke
            ol.style.Circle
            ol.interaction.Draw
            ol.interaction.Pointer
            ol.interaction.Select

            ol.interaction.Translate
            ol.events.condition
            ol.geom.Polygon
            ol.geom.Point))

(set! (.-onload js/window)
  (fn []
    (let [source (ol.source.OSM. #js {:layer "sat"})
          raster (ol.layer.Tile. #js {:source source})
          view (ol.View. #js {:center (ol.proj.fromLonLat #js [20.4489 44.7866])
                            :zoom 11
                            :maxZoom 18})
           ]
      (map (ol.Map. #js {:layers #js [raster ]
                          :target "map"
                          :view   view})))))

