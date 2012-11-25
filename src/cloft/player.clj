(ns cloft.player
  (:require [cloft.cloft :as c]
            [cloft.lingr :as lingr]
            [cloft.skill :as skill]
            [cloft.loc :as loc])
  (:import [org.bukkit.entity Player]
           [org.bukkit Bukkit Material]
           [org.bukkit.inventory ItemStack]))

(defn name2icon [name]
  (str name ": "))

"""zombie related"""

(def zombie-players (atom #{}))

(defn zombie? [entity]
  (and
    (instance? Player entity)
    (boolean (get @zombie-players entity))))

(defn zombieze [entity]
  (swap! zombie-players conj entity)
  (.setMaximumAir entity 1)
  (.setRemainingAir entity 1)
  (.sendMessage entity "You turned into a zombie.")
  (lingr/say-in-mcujm (str (name2icon (.getDisplayName entity)) "turned into a zombie."))
  (when-let [helmet (.getHelmet (.getInventory entity))]
    (loc/drop-item (.getLocation entity) helmet))
  (.setHelmet (.getInventory entity)
              (let [is (ItemStack. Material/SKULL_ITEM)
                    d (.getData is)]
                (.setData d 2)
                (.setData is d)
                (.toItemStack d 1))))

(defn rebirth-from-zombie [target]
  (.setMaximumAir target 300)
  (.setRemainingAir target 300)
  (.setHealth target (.getMaxHealth target))
  (swap! zombie-players disj target)
  (c/broadcast (.getDisplayName target) " rebirthed as a human.")
  (when-let [helmet (.getHelmet (.getInventory target))]
    (when (and
            (= Material/SKULL_ITEM (.getType helmet))
            (= 2 (.getData (.getData helmet))))
      (.setHelmet (.getInventory target) nil))))

(defn periodically-zombie-player []
  (doseq [zplayer (filter zombie? (Bukkit/getOnlinePlayers))]
    (when (= 15 (.getLightLevel (.getBlock (.getLocation zplayer))))
      (.setFireTicks zplayer 100))
    (.setFoodLevel zplayer (dec (.getFoodLevel zplayer)))))

"""death realated"""

(def death-locations (atom {}))

(defn death-event [evt player]
  (swap! death-locations assoc (.getDisplayName player) (.getLocation player))
  (lingr/say-in-mcujm (str (name2icon (.getDisplayName player)) (.getDeathMessage evt))))

(defn death-location-of [player]
  (get @death-locations (.getDisplayName player)))

"""murder-record
memo: Do we need double dispatch for entity-murder-event handling ?"""

(def murder-record (atom {}))

(defn record-and-report [killer entity evt]
  (let [name (.getDisplayName killer)
        target-name (c/entity2name entity)]
    (swap! murder-record
           #(let [old-map (or (% name) {})]
              (assoc % name (assoc old-map target-name (inc (or (old-map target-name) 0))))))
    (c/broadcast name " killed " target-name " (exp: " (.getDroppedExp evt) ")")))

"""dummies"""
(defn reaction-skill-of-without-consume [player] nil)

(defn player-inspect [player verbose?]
  (format
    "%s (%s)"
    (.getDisplayName player)
    (clojure.string/join
      ", "
      (map (partial clojure.string/join ": ")
           (filter second
                   (merge (when verbose?
                            {'MR (@murder-record (.getDisplayName player))})
                          {'HP (.getHealth player)
                           'MP (.getFoodLevel player)
                           'AS (skill/id (skill/arrow-skill-of player))
                           'RS (comment (reaction-skill-of-without-consume player))}))))))
