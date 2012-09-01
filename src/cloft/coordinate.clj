(ns cloft.coordinate
  (:import [org.bukkit.util Vector]))

(defn local-to-world [player origin-block dx hx rx]
  """returns a bukkit Vector"""
  (defn direction-of [loc]
    (let [height (Vector. 0.0 1.0 0.0)
          xz-normalized-vector #(.normalize (Vector. (.getX %) 0.0 (.getZ %)))
          depth (xz-normalized-vector (.getDirection loc))
          right-hand (.crossProduct (.clone depth) height)]
     [depth height right-hand]))

  (let [[d h r] (direction-of (.getLocation player))]
    (.add (.toVector (.getLocation origin-block))
          (.add (.add (.multiply d dx) (.multiply h hx)) (.multiply r rx)))))
