(ns cloft.coordinate
  (:import [org.bukkit.util Vector]))

(defn xz-normalized-vector [v]
  (.normalize (Vector. (.getX v) 0.0 (.getZ v))))

(defn player-direction [player]
  (let [loc (.getLocation player)
        height (Vector. 0.0 1.0 0.0)
        depth (xz-normalized-vector (.getDirection loc))
        right-hand (.crossProduct (.clone depth) height)]
   [depth height right-hand]))

(defn local-to-world [player origin-block dx hx rx]
  (let [[d h r] (player-direction player)
        loc (.toVector (.getLocation origin-block))]
    (.add loc (.add (.add (.multiply d dx) (.multiply h hx)) (.multiply r rx)))))
