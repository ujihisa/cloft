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

(defn skill-ice [entity]
  (if (.isLiquid (.getBlock (.getLocation entity)))
    (.setType (.getBlock (.getLocation entity)) m/ice)
    (let [block (block/of-arrow entity)
          loc (.getLocation block)
          loc-above (.add (.clone loc) 0 1 0)]
      (when
        (and
          (= m/air (.getType (.getBlock loc-above)))
          (not= m/air (.getType (.getBlock loc))))
        (.setType (.getBlock loc-above) m/snow))))
  (.remove entity))

(defn set-skill [player skill]
  (swap! user-skills assoc (.getDisplayName player) skill))

(defn change-skill [player block block-against]
  (when (block/blazon? m/cobblestone (.getBlock (.add (.getLocation block) 0 -1 0)))
    (let [table {m/yellow-flower [skill-teleport "TELEPORT"]
                 m/snow-block [skill-ice "ICE"]}]
      (when-let [[skill skill-name] (table (.getType block))]
        (set-skill player skill)
        (loc/play-effect (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
        (c/broadcast (.getDisplayName player) " changed egg-skill to " skill-name)))))

(defn damages-entity-event [evt egg target]
  (when-let [shooter (.getShooter egg)]
    (condp = (skill-of shooter)
      skill-ice
      (c/freeze-for-20-sec target)

      (prn 'egg-damages-entity-event 'must-not-happen shooter (skill-of shooter)))))
