(ns cloft.arrow
  (:import [org.bukkit.entity Arrow]))

(defn reflect
  "assumes `arrow` is an arrow"
  [evt arrow target]
  (let [shooter (.getShooter arrow)]
    (.setCancelled evt true)
    (.remove arrow)
    (let [a (.launchProjectile target Arrow)]
      (later
        (.setShooter a shooter))
      (.setVelocity a (.multiply (.getVelocity arrow) -1)))))
