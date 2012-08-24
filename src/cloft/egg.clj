(ns cloft.egg
  (:require [cloft.cloft :as c])
  (:require [cloft.material :as m]))

(def user-skills (atom {}))

(defn skill-of [player]
  (get @user-skills (.getDisplayName player)))

(defn skill-teleport [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)
        shooter (.getShooter entity)]
    (.setFallDistance shooter 0.0)
    (c/teleport-without-angle shooter location))
  (.remove entity))

(defn set-skill [player skill]
  (swap! user-skills assoc (.getDisplayName player) skill))
