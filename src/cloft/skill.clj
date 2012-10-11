(ns cloft.skill
  (:use [cloft.cloft :only [later]])
  (:require [cloft.cloft :as c])
  (:require [cloft.block :as block])
  (:require [cloft.loc :as loc])
  (:require [cloft.material :as m])
  (:require [cloft.sound :as s])
  (:import [org.bukkit Location Effect TreeType])
  (:import [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity IronGolem Item LightningStrike LivingEntity
            MagmaCube Minecart Monster MushroomCow NPC Painting Pig PigZombie
            Player PoweredMinecart Projectile Sheep Silverfish Skeleton Slime
            SmallFireball Snowball Snowman Spider Squid StorageMinecart
            ThrownPotion TNTPrimed Vehicle Villager Villager$Profession
            WaterMob Weather Wolf Zombie Ocelot]))

(defprotocol Learn
  (block [_]))

(defprotocol Refer
  (id [_]))

(defprotocol ArrowSkill
  (arrow-damage-entity [_ evt arrow target])
  (arrow-hit [_ evt arrow])
  (arrow-shoot [_ evt arrow shooter])
  (arrow-reflectable? [_]))

(def arrow-teleport
  (reify
    clojure.lang.Named
    (getName [_] "TELEPORT")
    Learn
    (block [_] m/yellow-flower)
    Refer
    (id [_] #'arrow-teleport)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      (.setCancelled evt true))
    (arrow-hit [_ evt arrow]
      (let [location (.getLocation arrow)
            world (.getWorld location)
            shooter (.getShooter arrow)]
        (.setFallDistance shooter 0.0)
        (c/teleport-without-angle shooter location))
      (.remove arrow))
    (arrow-shoot [_ evt arrow shooter]
      nil)
    (arrow-reflectable? [_] false)))

(def arrow-shotgun
  (reify
    clojure.lang.Named
    (getName [_] "SHOTGUN")
    Learn
    (block [_] m/cactus)
    Refer
    (id [_] #'arrow-shotgun)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      nil)
    (arrow-hit [_ evt arrow]
      (.remove arrow))
    (arrow-shoot [_ evt arrow shooter]
      (dotimes [_ 80]
        (let [rand1 (fn [] (* 0.8 (- (rand) 0.5)))
              new-arrow (.launchProjectile shooter Arrow)]
          (.setVelocity new-arrow (.getVelocity arrow))
          (c/add-velocity new-arrow (rand1) (rand1) (rand1)))))
    (arrow-reflectable? [_] true)))

(def arrow-strong
  (reify
    clojure.lang.Named
    (getName [_] "STRONG")
    Learn
    (block [_] m/glowstone)
    Refer
    (id [_] #'arrow-strong)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      nil)
    (arrow-hit [_ evt arrow]
      nil)
    (arrow-shoot [_ evt arrow shooter]
      (.setVelocity arrow (.multiply (.getVelocity arrow) 2)))
    (arrow-reflectable? [_] true)))

(def arrow-exp
  #_("dummy skill. grep the name.")
  (reify
    clojure.lang.Named
    (getName [_] "EXP")
    Learn
    (block [_] m/powered-rail)
    Refer
    (id [_] #'arrow-exp)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      (.damage (.getShooter arrow) 2))
    (arrow-hit [_ evt arrow]
      nil)
    (arrow-shoot [_ evt arrow shooter]
      nil)
    (arrow-reflectable? [_] true)))

(def arrow-fire
  (reify
    clojure.lang.Named
    (getName [_] "FIRE")
    Learn
    (block [_] m/red-rose)
    Refer
    (id [_] #'arrow-fire)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      (.setFireTicks target 400))
    (arrow-hit [_ evt arrow]
      (doseq [target (.getNearbyEntities arrow 1 1 1)
              :when (and (instance? LivingEntity target)
                         (not= (.getShooter arrow) target))]
        (.setFireTicks target 200)))
    (arrow-shoot [_ evt arrow shooter]
      nil)
    (arrow-reflectable? [_] true)))

(def arrow-tree
  (reify
    clojure.lang.Named
    (getName [_] "TREE")
    Learn
    (block [_] m/sapling)
    Refer
    (id [_] #'arrow-tree)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      (.setCancelled evt true))
    (arrow-hit [_ evt arrow]
      (let [location (.getLocation arrow)
            world (.getWorld location)
            trees (remove #{TreeType/JUNGLE TreeType/BROWN_MUSHROOM
                            TreeType/RED_MUSHROOM}
                          (TreeType/values))]
        (.generateTree world location (rand-nth trees)))
      (.remove arrow))
    (arrow-shoot [_ evt arrow shooter]
      nil)
    (arrow-reflectable? [_] true)))

(def arrow-torch
  (reify
    clojure.lang.Named
    (getName [_] "TORCH")
    Learn
    (block [_] m/torch)
    Refer
    (id [_] #'arrow-torch)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      nil)
    (arrow-hit [_ evt arrow]
      (let [location (.getLocation arrow)]
        (.setType (.getBlock location) m/torch))
      (.remove arrow))
    (arrow-shoot [_ evt arrow shooter]
      nil)
    (arrow-reflectable? [_] true)))

(def arrow-skill (atom {}))

(def arrow-skills
  [arrow-teleport arrow-shotgun arrow-strong arrow-exp arrow-fire arrow-tree
   arrow-torch])

(defn arrow-skillchange [player block0 block-against]
  (when (block/blazon? m/stone (.getBlock (.add (.getLocation block0) 0 -1 0)))
    (when-let [skill (first (filter #(= (.getType block0) (block %))
                                    arrow-skills))]
      (let [loc (.getLocation block0)]
        (loc/play-effect loc Effect/MOBSPAWNER_FLAMES nil)
        (loc/play-sound loc s/ambience-cave 0.8 1.2))
      (c/broadcast (format "%s changed arrow-skill to %s"
                           (.getDisplayName player)
                           (name skill)))
      (swap! arrow-skill assoc (.getDisplayName player) skill)
      (block/blazon-change-randomely m/cobblestone (.getBlock (.add (.getLocation block0) 0 -1 0))))))

(defn arrow-skill-of [player]
  (get @arrow-skill (.getDisplayName player)))

(defn to-hashmap []
  {:arrow-skill
   (into {} (for [[pname skill] @arrow-skill]
              [pname (id skill)]))})

(defn from-hashmap [hashmap]
  (swap! arrow-skill
         (constantly (into {} (for [[pname skill-var] (:arrow-skill hashmap)]
                                [pname @skill-var])))))

#_(table-legacy {
                 m/tnt [arrow-skill-explosion "EXPLOSION"]
                 m/piston-sticky-base [arrow-skill-pull "PULL"]
                 m/workbench [arrow-skill-ore "ORE"]
                 m/trap-door ['digg "DIGG"]
                 m/ladder ['trap "TRAP"]
                 m/rails ['cart "CART"]
                 m/bookshelf ['mobchange "MOBCHANGE"]
                 m/piston-base ['super-knockback "SUPER-KNOCKBACK"]
                 m/jack-o-lantern [arrow-skill-pumpkin "PUMPKIN"]
                 m/pumpkin [arrow-skill-pumpkin "PUMPKIN"]
                 m/diamond-block [arrow-skill-diamond "CRAZY DIAMOND"]
                 #_(m/fire [arrow-skill-flame "FLAME"])
                 m/brown-mushroom [arrow-skill-quake "QUAKE"]
                 m/red-mushroom ['arrow-skill-poison "POISON"]
                 m/water [arrow-skill-water "WATER"]
                 m/lava [arrow-skill-lava "LAVA"]
                 m/log [arrow-skill-woodbreak "WOODBREAK"]})
