(ns cloft.egg
  (:require [cloft.cloft :as c])
  (:require [cloft.material :as m])
  (:require [cloft.loc :as loc])
  (:require [cloft.block :as block])
  (:import [org.bukkit Effect]))

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

(defn change-skill [player block block-against]
  (when (block/blazon? m/cobblestone (.getBlock (.add (.getLocation block) 0 -1 0)))
    (let [table {m/yellow-flower [skill-teleport "TELEPORT"]}]
      (when-let [[skill skill-name] (table (.getType block))]
        (set-skill player skill)
        (loc/play-effect (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
        (c/broadcast (.getDisplayName player) " changed egg-skill to " skill-name)))))
