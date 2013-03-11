(ns tough-explosive.core
  (:require [swank.swank]
            [cloft.sound :as s]
            [cloft.loc :as loc]
            [cloft.material :as m]
            [cloft.cloft :as c])
  (:import [org.bukkit Bukkit DyeColor Material Color Location Effect]
           [org.bukkit.material Wool Dye]
           [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity IronGolem Item LightningStrike
            LivingEntity MagmaCube Minecart Monster MushroomCow NPC Painting
            Pig PigZombie Player PoweredMinecart Projectile Sheep Silverfish
            Skeleton Slime SmallFireball Snowball Snowman Spider Squid
            StorageMinecart ThrownPotion TNTPrimed Vehicle Villager
            Villager$Profession WaterMob Weather Wolf Zombie Ocelot
            Bat]
           [org.bukkit.event.entity EntityDamageByEntityEvent
            EntityDamageEvent$DamageCause CreatureSpawnEvent$SpawnReason]
           [org.bukkit.potion Potion PotionEffect PotionEffectType]
           [org.bukkit.inventory ItemStack CraftingInventory]
           [org.bukkit.util Vector]
           [org.bukkit.block Biome]
           [org.bukkit.event.block Action]
           [org.bukkit.enchantments Enchantment]
           [org.dynmap DynmapCommonAPI]
           [org.dynmap.markers MarkerSet])
  (:gen-class))

(defmacro later [sec & exps]
  `(.scheduleSyncDelayedTask
     (Bukkit/getScheduler)
     (.getPlugin (Bukkit/getPluginManager) "tough-explosive")
     (fn [] ~@exps)
     (int (* 20 ~sec))))

(defn entity-damage-event [evt]
  (.setDamage evt (int (/ (.getDamage evt) 5)))
  (when (= EntityDamageEvent$DamageCause/ENTITY_ATTACK (.getCause evt))
    (later 0 (.setVelocity (.getEntity evt) (Vector. 0 2 0)))))

(defn projectile-hit-event [evt]
  (let [entity (.getEntity evt)]
    (when (instance? Egg entity)
      (loc/explode (.getLocation entity) 10 false))))

(defn player-interact-event [evt]
  #_(let [player (.getPlayer evt)]
    (when (= Action/RIGHT_CLICK_AIR (.getAction evt))
      (when (= m/air (.getType (.getItemInHand player)))
        (.launchProjectile player Egg)))))

(defn entity-explode-event [evt]
  (let [entity (.getEntity evt)]
    (when (and
            (= 0 (rand-int 3))
            (instance? Creeper entity))
      (later 1 (loc/spawn (.getLocation entity) Creeper)))))

(def block-exploding (atom #{}))
(defn block-damage-event [evt]
  (let [block (.getBlock evt)]
    (if (= m/obsidian (.getType block))
      (when-not (@block-exploding block)
        (swap! block-exploding conj block))
      (case (rand-int 4)
        0 (loc/explode (.getLocation block) 0 false)
        1 (loc/explode (.getLocation block) 2 false)
        2 (do
            (.setType block m/air)
            (loc/spawn (.getLocation block) Creeper))
        3 (loc/spawn (.getLocation block) TNTPrimed)))))

(def sneak-players (atom {}))
(defn player-toggle-sneak-event [evt]
  (when (.isSneaking evt)
    (let [player (.getPlayer evt)]
      (swap! sneak-players update-in [player] (fnil inc 0))
      (future
        (Thread/sleep 2000)
        (swap! sneak-players update-in [player] dec))
      (when (= 3 (@sneak-players player))
        (.setFallDistance player 0.0)
        (.setVelocity player (doto (.getVelocity player) (.setY 2.4)))) )))

(def danger-chunks (atom #{}))

(defn periodically1sec []
  (doseq [block @block-exploding]
    (if (= 0 (rand-int 10))
      (swap! block-exploding disj block)
      (do
        (loc/explode (.add (.getLocation block) 0 -1 0) 2 false)
        (loc/explode (.add (.getLocation block) 0 1 0) 2 false)))))

(defn periodically1tick []
  )

(defonce swank* nil)
(defn on-enable [plugin*]
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin* #'periodically1sec 0 20)
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin* #'periodically1tick 0 1)
  (when-not swank*
    (def swank* (swank.swank/start-repl 4006))))
