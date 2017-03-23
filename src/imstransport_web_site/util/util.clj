(ns imstransport-web-site.util.util
  (:require [clojure.string :as st]
            [clojure.java.io :as io]))

(defn create-url
  [base params]
  (str base "?" (st/join "&" (map (fn [[k v]] (str k "=" v)) params))))

(defn load-resource-edn
  [file-name]
  (clojure.edn/read-string
   (slurp (io/resource file-name))))

(defn linefy-poly-coords
  [polygon]
  (let [polygon-points (partition 2 polygon)
        polygon-points-number (count polygon-points)]
    (map-indexed (fn [i p] (list p (nth polygon-points (mod (inc i) polygon-points-number)))) polygon-points)))

(defn intersects?
  [[a b :as side] point]
  (let [ax (second a)
        ay (first a)
        bx (second b)
        by (first b)
        px (second point)
        py* (first point)
        py (if (or (= py* ay) (= py* by))
             (+ py* 0.0001)
             py*)]
    (cond
      (or
       (< py ay)
       (> py by)) false

      (> px (max ax bx)) false
      
      (< px (min ax bx)) true

      :else (let [red (if (not= ax bx) (/ (- by ay) (bx ax)) 0)
                  blue (if (not= ax px) (/ (- by ay) (px ax)) 0)]
              (>= blue red)))))

(defn is-in-polygon?
  [polygon-lines point]
  (odd? (count (filter true? (map #(intersects? % point) polygon-lines)))))
