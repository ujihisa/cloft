(ns cloft.egg
  (:require [cloft.cloft :as c]
            [cloft.material :as m]
            [cloft.sound :as s]
            [cloft.loc :as loc]
            [cloft.block :as block])
  (:import [org.bukkit Effect]
           [org.bukkit.entity Creeper Skeleton Spider Zombie Slime Ghast
            PigZombie Enderman CaveSpider Silverfish Blaze MagmaCube Pig
            Sheep Cow Chicken Squid Wolf MushroomCow Villager Ocelot Player]
           [org.bukkit.material SpawnEgg]))

(def player-skills (atom {}))

(defn captureable? [entity]
  (defn spawnable? [entity]
    (some #(instance? % entity)
          [Creeper Skeleton Spider Zombie Slime Ghast PigZombie Enderman
           CaveSpider Silverfish Blaze MagmaCube Pig Sheep Cow Chicken Squid
           Wolf MushroomCow Villager Ocelot]))
  (and (spawnable? entity) (>= 5 (.getHealth entity))))

(defn skill-of [player]
  (get @player-skills (.getDisplayName player)))

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
      (when (and
              (= m/air (.getType (.getBlock loc-above)))
              (not= m/air (.getType (.getBlock loc))))
        (.setType (.getBlock loc-above) m/snow))))
  (.remove entity))

(defn skill-plant [entity]
  (let [inventory (.getInventory (.getShooter entity))]
    (.remove entity)
    (doseq [x (range -3 4) z (range -3 4)
            :let [loc (.add (.getLocation entity) x 0 z)]
            :when (and (.contains inventory m/seeds)
                       (= m/air (.getType (.getBlock loc)))
                       (= m/soil (.getType (.getBlock (.add (.clone loc) 0 -1 0)))))]
      (try
        (c/consume-itemstack inventory m/seeds)
        (c/consume-itemstack inventory m/seeds)
        (.setType (.getBlock loc) m/crops)
        (catch org.bukkit.event.EventException e nil)))))

(defn skill-capture [entity]
  (.remove entity))

(defn capture [captor target]
  (when (captureable? target)
    (loc/play-sound (.getLocation target) s/level-up 0.8 1.5)
    (let [spawn-egg (.toItemStack (SpawnEgg. (.getType target)))]
      (.setAmount spawn-egg 1)
      (loc/drop-item (.getLocation target) spawn-egg)
      (.remove target))))

(defn set-skill [player skill]
  (swap! player-skills assoc (.getDisplayName player) skill))

(defn change-skill [player block block-against]
  (when (block/blazon? m/cobblestone (.getBlock (.add (.getLocation block) 0 -1 0)))
    (let [table {m/yellow-flower [skill-teleport "TELEPORT"]
                 m/snow-block [skill-ice "ICE"]
                 m/crops [skill-plant "PLANT"]
                 m/chest [skill-capture "CAPTURE"]}]
      (when-let [[skill skill-name] (table (.getType block))]
        (set-skill player skill)
        (loc/play-effect (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
        (c/broadcast (.getDisplayName player) " changed egg-skill to " skill-name)))))

(defn damages-entity-event [evt egg target]
  (when-let [shooter (.getShooter egg)]
    (assert (instance? Player shooter) shooter)
    (condp = (skill-of shooter)
      skill-ice
      (c/freeze-for-20-sec target)

      skill-capture
      (when (= 0 (rand-int 4))
        (capture shooter target))

      nil
      (.sendMessage shooter "You don't have an egg-skill yet.")

      (prn 'egg-damages-entity-event 'must-not-happen shooter (skill-of shooter)))))

(defn hit-event [evt egg]
  (when-let [skill (skill-of (.getShooter egg))]
    (skill egg)))

(defn throw-event [evt]
  (let [egg (.getEgg evt)
        shooter (.getShooter egg)
        skill (skill-of shooter)]
    (when skill
      (.setHatching evt false))))
