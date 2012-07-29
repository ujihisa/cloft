(ns cloft.sanctuary
  (:require [cloft.cloft :as c])
  (:import [org.bukkit Bukkit]))

""" looks like protected area from:"""
"""  a. block placement by player """
"""  b. creeper explosion 2.  not for 1 and 3, some how. """

(def world (Bukkit/getWorld "world"))

(def sanctuary-players (atom #{}))

(def region [(org.bukkit.Location. world 45 30 -75)
                (org.bukkit.Location. world 84 90 -44)])

(defn is-in? [loc]
  """used as follow in block-place-event. maybe we need  on-block-place-event [block] for that."""
  """#_(when (sanctuary/is-in? (.getLocation block))"""
  """      (.setCancelled evt true)) """
    (c/location-bound? loc (first region) (second region)))

(defn on-player-move-event [player]
  (let [name (.getDisplayName player)]
    (if (get @sanctuary-players name)
      (when-not (c/location-bound? (.getLocation player) (first region) (second region))
                (swap! sanctuary-players disj name))
      (when (c/location-bound? (.getLocation player) (first region) (second region))
        (c/broadcast name " entered the sanctuary.")
        (swap! sanctuary-players conj name)))))

