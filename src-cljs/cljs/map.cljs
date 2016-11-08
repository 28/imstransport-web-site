(ns cljs.map
    (:require ol.Map
            ol.Collection
            ol.layer.Tile
            ol.View
            ol.proj
            ol.extent
            ol.source.OSM
            ol.interaction.Draw
            ol.geom.Polygon
            ol.geom.Point))

(set! (.-onload js/window)
  (fn []
    (let [source (ol.source.OSM. #js {:layer "sat"})
          raster (ol.layer.Tile. #js {:source source})
          view (ol.View. #js {:center (ol.proj.fromLonLat #js [20.4489 44.7866])
                            :zoom 11
                            :maxZoom 18})]
      (map (ol.Map. #js {:layers #js [rastercd]
                          :target "map"
                          :view   view})))))

