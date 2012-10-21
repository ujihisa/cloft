(ns cloft.arrow
  (:import [org.bukkit.entity Arrow]))

(defn reflect [evt arrow target]
  """assumes arrow is an arrow"""
  (let [shooter (.getShooter arrow)]
    (.setCancelled evt true)
    (.remove arrow)
    (let [a (.launchProjectile target Arrow)]
      (future
        (Thread/sleep 100)
        (.setShooter a shooter))
      (.setVelocity a (.multiply (.getVelocity arrow) -1)))))
