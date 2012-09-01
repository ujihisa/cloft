(ns cloft.skill
  (:require [cloft.cloft :as c])
  (:require [cloft.material :as m])
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

(defprotocol ArrowSkill
  (arrow-damage-entity [_ evt arrow target])
  (arrow-hit [_ evt arrow])
  (arrow-shoot [_ evt shooter])
  (arrow-reflectable? [_]))

(def arrow-skill-teleport
  (reify
    clojure.lang.Named
    (getName [_] "TELEPORT")

    Learn
    (block [_] m/yellow-flower)
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
    (arrow-shoot [_ evt shooter]
      nil)
    (arrow-reflectable? [_] false)))

(def arrow-skill-shotgun
  (reify
    clojure.lang.Named
    (getName [_] "SHOTGUN")

    Learn
    (block [_] m/cactus)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      nil)
    (arrow-hit [_ evt arrow]
      (.remove arrow))
    (arrow-shoot [_ evt shooter]
      (dotimes [_ 80]
        (let [rand1 (fn [] (* 0.8 (- (rand) 0.5)))
              arrow (.launchProjectile shooter Arrow)]
          (.setVelocity arrow (.getVelocity (.getProjectile evt)))
          (c/add-velocity arrow (rand1) (rand1) (rand1)))))
    (arrow-reflectable? [_] true)))
