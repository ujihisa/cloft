(ns cloft.core
  (:require [cloft.cloft :as c])
  (:use [cloft.cloft :only [later]])
  (:require [cloft.material :as m])
  (:require [cloft.sound :as s])
  (:require [cloft.scheduler :as cloft-scheduler])
  (:require [cloft.chimera-cow :as chimera-cow])
  (:require [cloft.arrow :as arrow])
  (:require [cloft.recipe])
  (:require [cloft.chest])
  (:require [cloft.player :as player])
  (:require [cloft.loc :as loc])
  (:require [cloft.block :as block])
  (:require [cloft.item :as item])
  (:require [cloft.transport :as transport])
  (:require [cloft.egg :as egg])
  (:require [cloft.skill :as skill])
  (:require [swank.swank])
  (:import [org.bukkit Bukkit DyeColor])
  (:import [org.bukkit.material Wool Dye])
  (:import [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity IronGolem Item LightningStrike LivingEntity
            MagmaCube Minecart Monster MushroomCow NPC Painting Pig PigZombie
            Player PoweredMinecart Projectile Sheep Silverfish Skeleton Slime
            SmallFireball Snowball Snowman Spider Squid StorageMinecart
            ThrownPotion TNTPrimed Vehicle Villager Villager$Profession
            WaterMob Weather Wolf Zombie Ocelot])
  (:import [org.bukkit.event.entity EntityDamageByEntityEvent
            EntityDamageEvent$DamageCause CreatureSpawnEvent$SpawnReason])
  (:import [org.bukkit.potion Potion PotionEffect PotionEffectType])
  (:import [org.bukkit.inventory ItemStack])
  (:import [org.bukkit.util Vector])
  (:import [org.bukkit Location Effect])
  (:import [org.bukkit.block Biome])
  (:import [org.bukkit.event.block Action])
  (:require [cloft.lingr :as lingr])
  (:require [cloft.zhelpers :as mq]))

(defn coor-local-to-world [player origin-block dx hx rx]
  """returns a bukkit Vector"""
  (defn direction-of [loc]
    (let [height (Vector. 0.0 1.0 0.0)
          xz-normalized-vector #(.normalize (Vector. (.getX %) 0.0 (.getZ %)))
          depth (xz-normalized-vector (.getDirection loc))
          right-hand (.crossProduct (.clone depth) height)]
     [depth height right-hand]))

  (let [[d h r] (direction-of (.getLocation player))]
    (.add (.toVector (.getLocation origin-block))
          (.add (.add (.multiply d dx) (.multiply h hx)) (.multiply r rx)))))

(defn player-super-jump [evt player]
  (let [name (.getDisplayName player)]
    (when (= m/feather (.getType (.getItemInHand player)))
      (let [amount (.getAmount (.getItemInHand player))
            x (if (.isSprinting player) (* amount 2) amount)
            x2 (/ (java.lang.Math/log x) 2)]
        (.setFallDistance player 0.0)
        (c/consume-itemstack (.getInventory player) m/feather)
        (c/add-velocity player 0 x2 0)
        (loc/play-sound (.getLocation player) s/chicken-hurt 1.0 (+ 0.9 (rand)))))))

(defn kaiouken [player]
  (if (< 0 (.getFireTicks player))
    (.sendMessage player "(界王拳失敗)")
    (do
      (.sendMessage player "界王拳3倍!")
      (.addPotionEffect player (PotionEffect. PotionEffectType/HUNGER 500 10))
      (.addPotionEffect player (PotionEffect. PotionEffectType/FIRE_RESISTANCE 500 3))
      (.addPotionEffect player (PotionEffect. PotionEffectType/INCREASE_DAMAGE 500 1))
      (.addPotionEffect player (PotionEffect. PotionEffectType/DAMAGE_RESISTANCE 500 1))
      (.addPotionEffect player (PotionEffect. PotionEffectType/SPEED 500 1))
      (.addPotionEffect player (PotionEffect. PotionEffectType/JUMP 500 1))
      (.addPotionEffect player (PotionEffect. PotionEffectType/FAST_DIGGING 500 1))
      (.setFireTicks player 500))))

(defn food-level-change-event [evt]
  (defn o157 [player]
    (when (= 0 (rand-int 2))
      (let [msg (format "%s got O157! Don't eat a yukhoe or a raw liver!"
                        (.getDisplayName player))]
        (c/broadcast msg)
        (lingr/say-in-mcujm msg)
        (.addPotionEffect player (PotionEffect. PotionEffectType/POISON 100 1))
        (.addPotionEffect player (PotionEffect. PotionEffectType/BLINDNESS 500 1))
        (.addPotionEffect player (PotionEffect. PotionEffectType/CONFUSION 500 1)))))
  (let [player (.getEntity evt)
        eating? (< (.getFoodLevel player) (.getFoodLevel evt))]
    (when eating?
      (when-let [itemstack (.getItemInHand player)]
        """for some reason case didn't work"""
        (condp = (.getType itemstack)
          m/apple (kaiouken player)
          m/raw-beef (o157 player)
          nil)))))

(defn entity-combust-event [evt]
  (.setCancelled evt true))

(defn player-move-event [evt]
  (let [player (.getPlayer evt)]
    #_(when-let [cart (.getVehicle player)]
      (when (instance? Minecart cart)
        (if (#{m/stone} (.getType (.getBlock (.getTo evt))))
          (.setDerailedVelocityMod cart (Vector. 0.90 0.90 0.90))
          (if (#{m/stone} (.getType (.getBlock (.getFrom evt))))
            (do
              #_(.setCancelled evt true)
              (.setVelocity cart (.multiply (.getVelocity cart) -1)))
            (.setDerailedVelocityMod cart (Vector. 0.1 0.5 0.1))
))))
    (when (and
            (.hasPotionEffect player PotionEffectType/SPEED)
            (.isSprinting player)
            (= 0 (rand-int 2)))
      (loc/play-effect (.add (.getLocation player) 0 -1 0) Effect/MOBSPAWNER_FLAMES nil))))

(defn arrow-skill-explosion [entity]
  (loc/explode (.getLocation entity) 0 false)
  (let [block (block/of-arrow entity)]
    (.breakNaturally block (ItemStack. m/diamond-pickaxe)))
  (.remove entity))

(defn arrow-skill-torch [entity]
  (let [location (.getLocation entity)]
    (.setType (.getBlock location) m/torch)))

(defn arrow-skill-pull [entity]
  (let [block (block/of-arrow entity)]
    (if (c/removable-block? block)
      (let [shooter-loc (.getLocation (.getShooter entity))]
        (.setType (.getBlock shooter-loc) (.getType block))
        (when (= m/mob-spawner (.getType block))
          (.setSpawnedType (.getState (.getBlock shooter-loc)) (.getSpawnedType (.getState block))))
        (.setType block m/air)
        (.teleport (.getShooter entity) (.add shooter-loc 0 1 0)))
      (.sendMessage (.getShooter entity) "PULL failed")))
  (.remove entity))

(defn arrow-skill-flame [entity]
  (doseq [x [-1 0 1] y [-1 0 1] z [-1 0 1]
          :let [block (.getBlock (.add (.clone (.getLocation entity)) x y z))]
          :when (= m/air (.getType block))]
    (.setType block m/fire)))

(defn arrow-skill-ore [entity]
  (let [block (block/of-arrow entity)]
    (when (= (.getType block) m/stone)
      (let [block-to-choices [m/coal-ore
                              m/coal-ore
                              m/cobblestone
                              m/cobblestone
                              m/gravel
                              m/iron-ore
                              m/lapis-ore
                              m/gold-ore
                              m/redstone-ore]]
        (.setType block (rand-nth block-to-choices))))))

(defn arrow-skill-pumpkin [entity]
  "This needs to be later to check if the arrow (entity) has hit an entity or
  not. If the arrow (entity) is dead, it has hit."
  (later
    (when-not (.isDead entity)
      (let [block (.getBlock (.getLocation entity))]
        (if (and
              (= 0 (rand-int 3))
              (= m/air (.getType block)))
          (do
            (.setType block (rand-nth [m/pumpkin m/jack-o-lantern]))
            (.remove entity))
          (.sendMessage (.getShooter entity) "PUMPKIN failed"))))))

(defn arrow-skill-diamond [entity]
  (let [block (.getBlock (.getLocation entity))]
    (condp = (.getType block)
      m/crops
      (.setData block 7)
      nil))
  (let [block (block/of-arrow entity)]
    (condp = (.getType block)
      m/cobblestone
      (.setType block m/stone)
      m/wool
      (do
        (.setType block m/air)
        (if (= 0 (rand-int 2))
          (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. m/wool))
          (loc/spawn (.getLocation entity) Sheep)))
      nil))
  (.remove entity)
  (let [loc (.getLocation entity)]
    (.dropItem (.getWorld loc) loc (ItemStack. m/arrow))))

(defn arrow-skill-quake [entity]
  (let [targets (.getNearbyEntities entity 5 3 5)]
    (future
      (doseq [_ [1 2 3]
              target targets
              :when (instance? LivingEntity target)]
        (Thread/sleep (rand-int 300))
        (later
          (loc/play-effect (.getLocation target) Effect/ZOMBIE_CHEW_WOODEN_DOOR nil)
          (c/add-velocity target (- (rand) 0.5) 0.9 (- (rand) 0.5)))
        (Thread/sleep 1500))
      (later (.remove entity)))))

(defn arrow-skill-liquid [material duration entity]
  (let [block (.getBlock (.getLocation entity))]
    (if (= m/air (.getType block))
      (do
        (.setType block material)
        (future-call #(do
                        (Thread/sleep duration)
                        (when (.isLiquid block)
                          (.setType block m/air)))))
      (.sendMessage (.getShooter entity) "failed")))
  (future-call #(do
                  (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. m/arrow))
                  (.remove entity))))

(defn arrow-skill-water [entity]
  (arrow-skill-liquid m/water 5000 entity))

(defn arrow-skill-lava [entity]
  (arrow-skill-liquid m/lava 500 entity))

(defn arrow-skill-woodbreak [entity]
  (let [block (block/of-arrow entity)
        table {m/wooden-door (repeat 6 (ItemStack. m/wood))
               m/fence (repeat 6 (ItemStack. m/stick))
               m/wall-sign (cons (ItemStack. m/stick)
                                        (repeat 6 (ItemStack. m/wood)))
               m/sign-post (cons (ItemStack. m/stick)
                                        (repeat 6 (ItemStack. m/wood)))
               m/fence-gate (concat (repeat 2 (ItemStack. m/wood))
                                           (repeat 4 (ItemStack. m/stick)))
               m/trap-door (repeat 6 (ItemStack. m/wood))}
        items (table (.getType block))
        block2 (.getBlock (.getLocation entity))
        items2 (table (.getType block2))]
    (if items
      (do
        (.setType block m/air)
        (doseq [item items]
          (.dropItemNaturally (.getWorld block) (.getLocation block) item)))
      (if items2
        (do
          (.setType block2 m/air)
          (doseq [item items2]
            (.dropItemNaturally (.getWorld block2) (.getLocation block2) item)))
        (.sendMessage (.getShooter entity) "Woodbreak failed."))))
  (.remove entity))

(defn arrow-skill-of [player]
  (get @skill/arrow-skill (.getDisplayName player)))

(def pickaxe-skill (atom {}))
(defn pickaxe-skill-of [player]
  (get @pickaxe-skill (.getDisplayName player)))

(defn skill2name [skill]
  (cond
    (fn? skill) (second (re-find #"\$.*?[_-]skill[_-](.*?)@" (str skill)))
    (nil? skill) nil
    :else (str skill)))

(def reaction-skill (atom {}))
(defn reaction-skill-of [player]
  (when-let [[skill num-rest] (get @reaction-skill (.getDisplayName player))]
    (if (= 0 num-rest)
      (do
        (c/broadcast (format "%s lost reactio-skill %s" (.getDisplayName player) (skill2name skill)))
        (swap! reaction-skill assoc (.getDisplayName player) nil)
        nil)
      (do
        (swap! reaction-skill assoc (.getDisplayName player) [skill (dec num-rest)])
        skill))))
(defn reaction-skill-of-without-consume [player]
  (first (get @reaction-skill (.getDisplayName player))))

(defn arrow-velocity-vertical? [arrow]
  (let [v (.getVelocity arrow)]
    (and (> 0.1 (Math/abs (.getX v)))
         (> 0.1 (Math/abs (.getZ v))))))

(defn thunder-mobs-around [player amount]
  (doseq [x (filter
              #(instance? Monster %)
              (.getNearbyEntities player 20 20 20))]
    (Thread/sleep (rand-int 1000))
    (.strikeLightningEffect (.getWorld x) (.getLocation x))
    (.damage x amount)))

(def last-vertical-shots (atom {}))

(defn enough-previous-shots-by-players? [triggered-by threshold]
  (let [locs (vals @last-vertical-shots)]
    ;(prn enough-previous-shots-by-players?)
    ;(prn locs)
    (<= threshold
      (count (filter
               #(> 10.0 ; radius of 10.0
                   (.distance (.getLocation triggered-by) %))
               locs)))))

(defn check-and-thunder [triggered-by]
  (when (enough-previous-shots-by-players? triggered-by 3)
    (future-call #(thunder-mobs-around triggered-by 20))))
    ; possiblly we need to flush last-vertical-shots, not clear.
    ; i.e. 3 shooters p1, p2, p3 shoot arrows into mid air consecutively, how often thuders(tn)?
    ; A.
    ; p1, p2, p3, p1, p2, p3,
    ;         t1          t2
    ; B.
    ; p1, p2, p3, p1, p2, p3,
    ;         t1, t2, t3, t4

(defn blaze2-launch-fireball [blaze2 projectile]
  (assert (= "world" (.getName (.getWorld blaze2))) blaze2)
  (.remove projectile)
  (if (= 0 (rand-int 5))
    (let [loc (.getLocation blaze2)]
      (.playEffect (.getWorld loc) loc Effect/MOBSPAWNER_FLAMES nil)
      (loc/spawn
           (.add loc 0 2 0)
           (rand-nth [Skeleton Zombie Spider MagmaCube MagmaCube Silverfish
                         Enderman Villager Creeper])))
    (.launchProjectile blaze2 Arrow)))

(defn blaze2-arrow-hit [target]
  (.setFireTicks target 200))

(defn blaze2-get-damaged [evt blaze2]
  (assert (= "world" (.getName (.getWorld blaze2))) blaze2)
  (.setDamage evt (max (int (/ (.getDamage evt) 2)) 9)))

(defn ghast2-get-damaged [evt ghast2 attacker]
  (assert (= "world" (.getName (.getWorld ghast2))) ghast2)
  (cond
    (nil? attacker) (.setCancelled evt true)

    (instance? Projectile attacker)
    (do
      (.remove attacker)
      (.setCancelled evt true))

    :else
    (.setDamage evt (min (int (/ (.getDamage evt) 2)) 4))))

(defn blaze2-murder-event [evt blaze2 player]
  (assert (= "world" (.getName (.getWorld blaze2))) blaze2)
  (doseq [[x y z] [[0 0 0] [-1 0 0] [0 -1 0] [0 0 -1] [1 0 0] [0 1 0] [0 0 1]]
          :let [loc (.add (.clone (.getLocation blaze2)) x y z)]
          :when (= m/air (.getType (.getBlock loc)))
          :let [new-block-type
                (rand-nth [m/nether-brick m/netherrack
                           m/soul-sand m/glowstone
                           m/glowstone m/glowstone
                           m/air m/air])]]
    (.setType (.getBlock loc) new-block-type))
  (.setDroppedExp evt 40)
  (c/broadcast (format "%s beated a blaze2!" (.getDisplayName player)))
  (lingr/say-in-mcujm (format "%s beated a blaze2!" (.getDisplayName player))))

(defn ghast2-murder-event [evt ghast2 player]
  (assert (= "world" (.getName (.getWorld ghast2))) ghast2)
  (.setDroppedExp evt 80)
  (let [msg (format "%s beated a ghast2!" (.getDisplayName player))]
    (lingr/say-in-mcujm msg)))

(defn projectile-launch-event [evt]
  (let [projectile (.getEntity evt)
        shooter (.getShooter projectile)]
    (condp instance? projectile
      Fireball (when (and
                       (instance? Blaze shooter)
                       (= "world" (.getName (.getWorld projectile))))
                 (blaze2-launch-fireball shooter projectile))
      SmallFireball nil
      nil)))

(defn creature-spawn-event [evt]
  (let [creature (.getEntity evt)]
    (condp = (.getSpawnReason evt)
      CreatureSpawnEvent$SpawnReason/NATURAL
      (do
        (when (and
                (= 0 (rand-int 5))
                (some #(instance? % creature) [Zombie Skeleton Spider Creeper Enderman]))
          (let [spider (loc/spawn (.getLocation creature) Spider)]
            (later (.setPassenger spider creature))))
        (when (and
                (= 0 (rand-int 10))
                (= "world" (.getName (.getWorld creature)))
                (some #(instance? % creature) [Zombie Skeleton Spider Creeper Enderman]))
          (loc/spawn (.getLocation creature) Blaze)))

      CreatureSpawnEvent$SpawnReason/DEFAULT
      (prn 'spawn-default creature)

      CreatureSpawnEvent$SpawnReason/JOCKEY
      nil

      nil)))

(defn popcorn [item p]
  "args: item entity (maybe dead), success probability 0..100"
  "after delay."
  (future
    (Thread/sleep (rand-int 5000))
    (later (when-not (.isDead item)
      (if (> p (rand-int 100))
        (do
          (loc/play-effect (.getLocation item) Effect/ZOMBIE_CHEW_WOODEN_DOOR nil)
          (c/add-velocity item (- (rand) 0.5) 0.9 (- (rand) 0.5))
          (loc/drop-item (.getLocation item) (.getItemStack item)))
        (do
          (loc/play-effect (.getLocation item) Effect/CLICK1 nil)
          (.remove item)))))))

(def lifting? (ref false))
(def popcorning (ref nil))

(defn item-spawn-event [evt]
  (when @lifting?
    (.setCancelled evt true))
  (if @popcorning
    (let [item (.getEntity evt)
          itemstack (.getItemStack item)
          itemtype (.getType itemstack)]
      (cond
        (= m/emerald itemtype) (.setCancelled evt true)
        (#{m/chest m/ender-chest} itemtype) nil
        :else (popcorn item @popcorning)))
    (let [item (.getEntity evt)
          table {m/raw-beef [m/rotten-flesh m/cooked-beef]
                 m/raw-chicken [m/rotten-flesh m/cooked-chicken]
                 m/raw-fish [m/raw-fish m/cooked-fish]
                 m/pork [m/rotten-flesh m/grilled-pork]
                 #_(m/apple [m/apple m/golden-apple])
                 m/rotten-flesh [nil m/coal]}
          itemstack (.getItemStack item)]
      (when (table (.getType itemstack))
        (future-call #(let [pair (table (.getType itemstack))]
                        (Thread/sleep 5000)
                        (when-not (.isDead item)
                          (if (#{m/furnace m/burning-furnace}
                                  (.getType (.getBlock (.add (.getLocation item) 0 -1 0))))
                            (do
                              (.dropItem (.getWorld item) (.getLocation item) (ItemStack. (last pair) (.getAmount itemstack)))
                              (.remove item))
                            (when-let [type-to (first pair)]
                              (Thread/sleep 25000)
                              (when-not (.isDead item)
                                (.dropItem (.getWorld item) (.getLocation item) (ItemStack. type-to (.getAmount itemstack)))
                                (.remove item)))))))))))

(defn entity-shoot-bow-event [evt]
  (let [shooter (.getEntity evt)]
    (when (instance? Player shooter)
      (when (.isSneaking shooter)
        (.setVelocity (.getProjectile evt) (.multiply (.getVelocity (.getProjectile evt)) 2)))
      (when (arrow-velocity-vertical? (.getProjectile evt))
        (prn last-vertical-shots)
        (swap! last-vertical-shots assoc (.getDisplayName shooter) (.getLocation shooter))
        (prn last-vertical-shots)
        (future-call #(let [shooter-name (.getDisplayName shooter)]
                        (check-and-thunder shooter)
                        (Thread/sleep 1000)
                        (swap! last-vertical-shots dissoc shooter-name))))
      (when-let [skill (arrow-skill-of shooter)]
        (skill/arrow-shoot skill evt (.getProjectile evt) shooter)))))

(def takumi-watched? (atom false))

(defn entity-target-event [evt]
  (let [entity (.getEntity evt)]
    (when (instance? Creeper entity)
      (when-let [target (.getTarget evt)]
        (.setFireTicks entity 40)
        (when (and
                (not @takumi-watched?)
                (instance? Player target))
          (swap! takumi-watched? (constantly true))
          (future
            (Thread/sleep 5000)
            (swap! takumi-watched? (constantly false)))
          (c/broadcast "Takumi is watching " (.getDisplayName target)))))))

(defn entity-explosion-prime-event [evt]
  nil)

(defn reaction-skill-ice [you by]
  (c/freeze-for-20-sec by)
  (lingr/say-in-mcujm (str "counter attack with ice by " (.getDisplayName you) " to " (c/entity2name by))))

(defn reaction-skill-knockback [you by]
  (let [direction (.multiply (.normalize (.toVector (.subtract (.getLocation by) (.getLocation you)))) 2)]
    (c/add-velocity by (.getX direction) (.getY direction) (.getZ direction))))

(defn reaction-skill-fire [you by]
  (.setFireTicks by 100))

(defn reaction-skill-golem [you by]
  (let [golem (loc/spawn (.getLocation by) IronGolem)]
    (.setTarget golem by)
    (future-call #(do
                    (Thread/sleep 10000)
                    (.remove golem))))
  (.sendMessage you "a golem helps you!"))

(defn reaction-skill-wolf [you by]
  (let [wolf (loc/spawn (.getLocation by) Wolf)]
    (.setTamed wolf true)
    (.setOwner wolf you)
    (.setTarget wolf by)
    (future-call #(do
                    (Thread/sleep 10000)
                    (.remove wolf))))
  (.sendMessage you "a wolf helps you!"))

(defn find-place [from range]
  (let [candidates
        (for [x range y range z range :when (> y 5)]
          (.add (.clone from) x y z))
        good-candidates
        (filter
          #(and
             (not= m/air
                   (.getType (.getBlock (.add (.clone %) 0 -1 0))))
             (= m/air (.getType (.getBlock %)))
             (= m/air
                (.getType (.getBlock (.add (.clone %) 0 1 0)))))
          candidates)]
    (rand-nth good-candidates)))

(defn reaction-skill-teleport [you by]
  (.sendMessage you
                (str "You got damage by " (c/entity2name by) " and escaped."))
  (.teleport you (find-place (.getLocation you) (range -10 10))))

(defn reaction-skill-poison [you by]
  (.addPotionEffect by (PotionEffect. PotionEffectType/POISON 200 2)))

(defn reaction-skillchange [player block block-against]
  (when (block/blazon? m/log block-against)
    (let [table {m/red-rose [reaction-skill-fire "FIRE"]
                 m/yellow-flower [reaction-skill-teleport "TELEPORT"]
                 m/cobblestone [reaction-skill-knockback "KNOCKBACK"]
                 m/dirt [reaction-skill-wolf "WOLF"]
                 m/iron-block [reaction-skill-golem "GOLEM"]
                 m/snow-block [reaction-skill-ice "ICE"]
                 m/red-mushroom [reaction-skill-poison "POISON"]}]
      (when-let [skill-name (table (.getType block))]
        (if (= 0 (.getLevel player))
          (.sendMessage player "Your level is 0. You can't set reaction skill yet.")
          (let [l (.getLevel player)]
            (.playEffect (.getWorld block) (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
            (c/broadcast (.getDisplayName player) " changed reaction-skill to " (last skill-name))
            (.sendMessage player (format "You can use the reaction skill for %d times" l))
            (swap! reaction-skill assoc (.getDisplayName player) [(first skill-name) l])))))))

(defn pickaxe-skillchange [player block block-against]
  (when (block/blazon? m/iron-ore (.getBlock (.add (.getLocation block) 0 -1 0)))
    (let [table {m/yellow-flower ['pickaxe-skill-teleport "TELEPORT"]
                 m/red-rose ['pickaxe-skill-fire "FIRE"]
                 m/workbench ['pickaxe-skill-ore "ORE"]
                 m/stone ['pickaxe-skill-stone "STONE"]
                 m/sand ['pickaxe-skill-fall "FALL"]}]
      (when-let [skill-name (table (.getType block))]
        (loc/play-effect (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
        (c/broadcast (.getDisplayName player) " changed pickaxe-skill to " (last skill-name))
        (swap! pickaxe-skill assoc (.getDisplayName player) (first skill-name))))
    (when (#{m/chest m/ender-chest} (.getType block))
      (swap! pickaxe-skill dissoc (.getDisplayName player))
      (loc/play-effect (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
      (.sendMessage player "ざわ・・・"))))

(defn summon-x
  ([pos world creature] (summon-x pos world creature 1))
  ([pos world creature after]
   (cloft-scheduler/settimer after #(.spawn world (.toLocation pos world) creature))))

(defn summon-giant [player block]
  (.damage player (/ (.getHealth player) 2))
  (.setFoodLevel player 0)
  (let [world (.getWorld player)
        spawn-at  (coor-local-to-world player block 10.0 0.0 0.0)]
    (.strikeLightningEffect world (.toLocation spawn-at world))
    (summon-x spawn-at world Giant)
    (c/broadcast (.getDisplayName player) " has summoned a Giant!")))

(defn summon-residents-of-nether [player block]
  (let [world (.getWorld player)
        loc (.toVector (.getLocation player))
        pos1 (coor-local-to-world player block 15.0 1.0 -5.0)
        pos2 (coor-local-to-world player block 15.0 1.0 0.0)
        pos3 (coor-local-to-world player block 15.0 1.0 5.0)
        place-fire (fn [v i]
                      (cloft-scheduler/settimer
                        i
                        #(when (= m/air (.getType v))
                           (.playEffect (.getWorld v) (.getLocation v) Effect/BLAZE_SHOOT nil)
                           (.setType v m/fire))))]
    (letfn [(explode-at ([pos world delay]
               (cloft-scheduler/settimer 1  #(when-not (.createExplosion world (.toLocation pos world) 0.0 true)
                                               (explode-at pos world 1)))))
            (summon-set-of-evils-at [pos loc world]
              (cloft-scheduler/settimer
                1
                #(do
                   (block/place-in-line-with-offset world (.clone loc) (.clone pos) place-fire 2)
                   (explode-at (.clone pos) world 60)
                   (summon-x pos world Blaze 65)
                   (summon-x loc world PigZombie 65)
                   (let [ghast-pos (.add (.clone pos) (Vector. 0.0 7.0 0.0))]
                     (explode-at ghast-pos world 1)
                     (summon-x ghast-pos world Ghast 65)))))]
            (summon-set-of-evils-at pos1 loc world)
            (summon-set-of-evils-at pos2 loc world)
            (summon-set-of-evils-at pos3 loc world)
            (summon-x (coor-local-to-world player block -5.0 0.5 0.0) world Creeper 80)
            (c/broadcast (.getDisplayName player) " has summoned Blazes, PigZombies and Ghasts!"))))

(def active-fusion-wall (atom {}))
(defn active-fusion-wall-of [player]
  (get @active-fusion-wall (.getDisplayName player)))

(defn alchemy-fusion-wall [player block]
  (let [world (.getWorld player)
        loc (.toVector (.getLocation player))
        bottom (coor-local-to-world player block 15.0 0.0 0.0)
        top (coor-local-to-world player block 15.0 6.0 0.0)]
    (letfn [(place-cobblestones [v i]
              (cloft-scheduler/settimer i
                                       #(when (= m/air (.getType v))
                                          (.setType v m/cobblestone))))]
      (.strikeLightningEffect world (.toLocation bottom world))
      (block/place-in-line world bottom top place-cobblestones)
      (if-let [prev (active-fusion-wall-of player)]
        (let [[eb et] prev]
          (block/place-in-line world eb bottom place-cobblestones)
          (block/place-in-line world et top place-cobblestones))
        (prn "nothing to connect."))
      (swap! active-fusion-wall assoc (.getDisplayName player) [bottom top]))))

(defn fusion-floor [player block]
  (let [world (.getWorld player)
        start-left (coor-local-to-world player block 0.0 0.0 -1.0)
        start-center (.toVector (.getLocation player))
        start-right (coor-local-to-world player block 0.0 0.0 1.0)
        distance (min (+ 10.0 (* 2 (.getLevel player))) 60.0)
        end-left (coor-local-to-world player block distance 0.0 -1.0)
        end-center (coor-local-to-world player block distance 0.0 0.0)
        end-right (coor-local-to-world player block distance 0.0 1.0)
        block-floor (fn [v i]
                      (cloft-scheduler/settimer
                        i
                        #(when (boolean ((block/category :enterable) (.getType v)))
                           (when (= 0 (rand-int 6))
                             (.strikeLightningEffect world (.getLocation v)))
                           (.setType v m/cobblestone))))]
    (block/place-in-line-with-offset world start-left end-left block-floor 2)
    (block/place-in-line-with-offset world start-center end-center block-floor 2)
    (block/place-in-line-with-offset world start-right end-right block-floor 2)))

(defn make-redstone-for-livings [player block]
  (let [world (.getWorld player)]
    (doseq [e (filter #(instance? LivingEntity %) (.getNearbyEntities player 10 10 10))]
           (let [loc (.getLocation e)]
             (.remove e)
             (.strikeLightningEffect world loc)
             (.dropItem world loc (ItemStack. m/redstone))))))

(defn erupt-volcano [player block]
  (let [world (.getWorld player)
        crator-vector (coor-local-to-world player block 40.0 20.0 0.0)
        crator-location (.toLocation crator-vector world)]
    (.strikeLightningEffect world crator-location)
    (.setType (.getBlockAt world crator-location) m/lava)
    (block/place-in-circle
      world 10 14
      crator-location
      (fn [v i]
          (.setType v m/cobblestone)))))

(defn close-air-support [player block]
  (let [world (.getWorld player)
        xz (coor-local-to-world player block 0.0 0.0 0.0)
        center-vector (.setY (.clone xz) 255)
        center-location (.toLocation center-vector world)]
    (doseq [v (block/blocks-in-radiaus-xz world center-location 20 70)]
      (when (= 1 (rand-int 30))
        (cloft-scheduler/settimer
          (rand-int 300)
          #(let [tnt (.spawn world (.getLocation v) TNTPrimed)
                 uy (Vector. 0.0 -10.0 0.0)
                 y (.multiply uy (rand))]
             (.setVelocity tnt y)))))))

(defn earthen-pipe [player block]
  (let [world (.getWorld player)
        center-vector (coor-local-to-world player block 10.0 0.0 0.0)
        center-location (.toLocation center-vector world)
        uy (Vector. 0 1 0)]
    (loop [h 0 inner 5.0 outer 7.0]
      (block/place-in-circle
        world inner outer
        (.toLocation (.add (.clone center-vector) (.multiply (.clone uy) h)) world)
        (fn [v i]
          (.setType v m/wool)
          (.setData v (Byte. (byte 5)))))
      (if (< h 20)
        (recur (inc h) inner outer)
        (when (< h 24)
          "making lip"
          (recur (inc h) inner 9))))))

(defn invoke-alchemy [player block block-against]
  (when (block/blazon? m/netherrack block-against)
    "MEMO: to be changed to STONE BRICK"
    "TODO: consistant naming"
    (let [table {m/cobblestone alchemy-fusion-wall
                 m/sand fusion-floor
                 m/dirt summon-giant
                 m/log make-redstone-for-livings
                 m/glowstone summon-residents-of-nether}
          table2 {m/tnt close-air-support
                  m/netherrack erupt-volcano
                  m/red-mushroom earthen-pipe}]
      (when-let [alchemy (table (.getType block))]
        (alchemy player block)))))

(defn block-damage-event [evt]
  (let [player (.getPlayer evt)
        block (.getBlock evt)]
    (when (item/pickaxes (.getType (.getItemInHand player)))
      (condp = (pickaxe-skill-of player)
        'pickaxe-skill-stone
        (if (= m/stone (.getType block))
          (.setInstaBreak evt true)
          (when (not= 0 (rand-int 1000))
            (.setCancelled evt true)))

        'pickaxe-skill-fall
        (let [btype (.getType block)]
          (when (and
                  (.isBlock btype)
                  (not (contains? item/unobtainable btype))
                  (not (instance? org.bukkit.block.ContainerBlock (.getState block))))
            (loc/fall-block (.getLocation block) btype (.getData block))
            (.setType block m/air)
            (let [item (.getItemInHand player)
                  max-durability (item/pickaxe-durabilities (.getType item))]
              (if (< max-durability (.getDurability item))
                (c/consume-item player)
                (item/modify-durability item #(+ (int (/ max-durability 10)) %))))))

        nil))))

(defn block-piston-extend-event [evt]
  "pushes the entity strongly"
  (let [direction (.getDirection evt)
        block (.getBlock
                (.add (.getLocation (.getBlock evt)) (.getModX direction) (.getModY direction) (.getModZ direction)))
        entities (c/entities-nearby-from (.getLocation block) 10)
        entities-pushed (filter #(= block (.getBlock (.getLocation %))) entities)]
    (doseq [e entities-pushed]
      (.teleport e (.add (.getLocation e) (.getModX direction) (.getModY direction) (.getModZ direction)))
      (c/add-velocity e (* (.getModX direction) 4) (* (.getModY direction) 1.5) (* (.getModZ direction) 4)))))

(defn vector-from-to [ent-from ent-to]
  (.toVector (.subtract (.getLocation ent-to) (.getLocation ent-from))))

(def countdowning? (ref false))
(def player-block-placed (atom {}))

(defn lookup-player-block-placed [block player]
  """returns nil or a tuple of (block, player, xyz)"""
  (defn block-linear [b1 b2]
    "returns :x, :y, :z or false"
    (let [l1 (.getLocation b1)
          l2 (.getLocation b2)]
      (cond
        (and
          (= (.getY l1) (.getY l2))
          (= (.getZ l1) (.getZ l2)))
        #(.getX %)
        (and
          (= (.getZ l1) (.getZ l2))
          (= (.getX l1) (.getX l2)))
        #(.getY %)
        (and
          (= (.getX l1) (.getX l2))
          (= (.getY l1) (.getY l2)))
        #(.getZ %))))
  (first (filter
           (fn [[_ _ xyz]] xyz)
           (map (fn [[b p]]
                  [b p (and
                         (= (.getType b) (.getType block))
                         (not= p player)
                         (block-linear b block))])
                @player-block-placed))))

(defn block-place-event [evt]
  (let [block (.getBlock evt)
        player (.getPlayer evt)
        block-against (.getBlockAgainst evt)]
    (assert (instance? Player player))
    (if-let [[another-block another-player xyz-getter] (lookup-player-block-placed block player)]
      (do
        (swap! player-block-placed empty)
        (let [block1 (min-key xyz-getter block another-block)
              block2 (if (= block1 block) another-block block)
              unit-vec (.normalize
                         (.toVector (.subtract
                                      (.getLocation block2)
                                      (.getLocation block1))))]
          (doseq [diff (range 1 (- (xyz-getter block2) (xyz-getter block1)))
                  :when (< diff 200)]
            (let [b (.getBlock (.add (.clone (.getLocation block1))
                                     (.multiply (.clone unit-vec) diff)))]
              (when ((block/category :enterable) (.getType b))
                (.setType b (.getType another-block))
                (.setData b (.getData another-block))))))
        (.sendMessage another-player "ok (second)")
        (.sendMessage player "ok (first)"))
      (when @countdowning?
        (future
          (Thread/sleep 3000)
          (swap! player-block-placed dissoc block))
        (swap! player-block-placed assoc block player)))
    (skill/arrow-skillchange player block block-against)
    (pickaxe-skillchange player block block-against)
    (reaction-skillchange player block block-against)
    (egg/change-skill player block block-against)
    (invoke-alchemy player block block-against)
    #_(transport/teleport-machine player block block-against)))

(defn welcome-message [player]
  #_(.sendMessage player "[TIPS] 川で砂金をとろう! クワと皿を忘れずに。")
  #_(.sendMessage player "[TIPS] 3人が同時に真上に矢を撃つと敵がEmeraldに")
  #_(.sendMessage player "[TIPS] りんごを食べて界王拳!")
  #_(.sendMessage player "[TIPS] HP Maxのときに金の剣で攻撃するとビーム")
  #_(.sendMessage player "[TIPS] 糸で何か乗せてるときは、糸なくても右クリックで降ろせます")
  #_(.sendMessage player "[TIPS] 生牛肉は危険です")
  #_(.sendMessage player "[TIPS] shiftでプレイヤからも降りれます")
  #_(.sendMessage player "[TIPS] exp5以上のなにか殺すとたまにEmeraldもらえます")
  #_(.sendMessage player "[TIPS] arrow-skill-treeで生える木の種類がランダムに")
  #_(.sendMessage player "[TIPS] しゃがんだまま剣でガードすると近くの敵に自動照準")
  #_(.sendMessage player "[TIPS] しゃがんだまま弓を構えると近くの敵に自動照準")
  #_(.sendMessage player "[TIPS] arrow-skill-woodbreakがちょっと便利に")
  #_(.sendMessage player "[TIPS] ラピュタ近くの地上の村、実はその下に地下帝国が...")
  #_(.sendMessage player "[TIPS] 剣を焼くと分解できる。もしそれがenchantされてると...?")
  #_(.sendMessage player "[TIPS] stone plateを持って他人を右クリックするとスカウター")
  #_(.sendMessage player "[TIPS] TNTの上に置かれたチェストを開くと、即座に...!")
  #_(.sendMessage player "[NEWS] Enderman右クリックでもアイテム。たまに怒られるよ")
  #_(.sendMessage player "[NEWS] Zombie Jockeyや匠Jockeyが出没するように")
  #_(.sendMessage player "[NEWS] pickaxe-skill紋章上チェストをpickaxeで破壊するギャンブル")
  #_(.sendMessage player "[NEWS] 紋章上チェスト確率はblaze rodで確認可能。エメラルドで確変!")
  (.sendMessage player "[NEWS] pickaxe-skill-fallで任意のブロックを落下可能")
  (.sendMessage player "[NEWS] はさみで羊毛ブロックを切って糸にできる")
  #_(.sendMessage player "[NEWS] 金剣ビームはBlaze2に逆効果")
  (.sendMessage player "[NEWS] エンダーチェストで高確率popcornが可能に!")
  (.sendMessage player "[NEWS] chestのegg-skillでポケモンできる!")
  (.sendMessage player "[NEWS] 蜘蛛右クリックであなたもライダーに")
  (.sendMessage player "[NEWS] 匠、雪玉爆発")
  (.sendMessage player "[NOTE] スキルシステム大改造中。arrow-skillはいまは申し訳ないけれど一部しか使えません")
  (later (.sendMessage player (clojure.string/join ", " (map name skill/arrow-skills))))
  #_(.sendMessage player "[NEWS] "))

(defn player-login-event [evt]
  (let [player (.getPlayer evt)]
    (future (Thread/sleep 1000)
      (let [ip (.. player getAddress getAddress getHostAddress)]
        (.setOp player (or
                         (.startsWith ip "10.0")
                         (= "113.151.154.229" ip)
                         (= "127.0.0.1" ip))))
      (loc/play-effect (.getLocation player) Effect/RECORD_PLAY (rand-nth item/records))
      (welcome-message player))
    (lingr/say-in-mcujm (format "%s logged in" (.getDisplayName player)))))

(defn paperlot [player]
  (letfn [(unlucky [player]
            (.sendMessage player "unlucky!")
            (future-call
              #(do
                 "wait less than 5min"
                 (Thread/sleep (rand-int 300000))
                 (doseq [x [-2 2] z [-2 2]]
                   (let [loc (.add (.getLocation player) x 2 z)]
                     (.sendMessage player "!!!")
                     (when (= m/air (.getType (.getBlock loc)))
                       (.spawn (.getWorld loc) loc Creeper)))))))]
    ((rand-nth [unlucky]) player)))

(defn async-player-chat-event [evt]
  (let [player (.getPlayer evt)
        pname (.getDisplayName player)
        msg (.getMessage evt)]
    (cond
      (= 1 (count msg)) nil

      (.startsWith msg "-")
      (do
        (.setCancelled evt true)
        (let [sound
              (read-string
                (str "org.bukkit.Sound/"
                     (clojure.string/upper-case (clojure.string/replace (apply str (rest msg)) #"[ -]" "_"))))]
          (loc/play-sound (.getLocation player) (eval sound) 1.0 1.0)))

      (= "benri" msg)
      (do
        (c/broadcast (format "%s: 便利" pname))
        (.setCancelled evt true))

      (= "countdown" msg)
      (do
        (.setCancelled evt true)
        (future
          (c/broadcast pname ": " 3 " " (.getType (.getItemInHand player)))
          (dosync
            (ref-set countdowning? true))
          (Thread/sleep 1000)
          (c/broadcast 2)
          (Thread/sleep 1000)
          (c/broadcast 1)
          (Thread/sleep 2000)
          (dosync
            (ref-set countdowning? false))))

      (= "where am I?" msg)
      (let [loc (.getLocation player)
            msg (format "%s (%d %d %d)"
                        pname
                        (int (.getX loc))
                        (int (.getY loc))
                        (int (.getZ loc)))]
        (.setCancelled evt true)
        (lingr/say "computer_science" msg)
        (c/broadcast msg))

      :else (lingr/say "computer_science" (str (player/name2icon pname) msg)))))

(defn player-rightclick-player [player target]
  (if (and (.getItemInHand player)
           (= m/stone-plate (.getType (.getItemInHand player))))
    (do
      (.sendMessage target "Stone plate helmet!")
      (when-let [helmet (.getHelmet (.getInventory target))]
        (loc/drop-item (.getLocation target) helmet))
      (.setHelmet (.getInventory target) (ItemStack. m/stone-plate))
      (c/consume-item player))
    (do
      (.setFoodLevel target (dec (.getFoodLevel target)))
      (.setGameMode target org.bukkit.GameMode/SURVIVAL))))

(defn entity-interact-physical-event [evt entity]
  (transport/teleport-up entity (.getBlock evt)))

(defn entity-interact-event [evt]
  (let [entity (.getEntity evt)]
    (entity-interact-physical-event evt entity)))

(def special-snowball-set (atom #{}))

(defn lift [center-loc ydiff]
  (loc/play-effect center-loc Effect/CLICK1 nil)
  (loc/play-effect center-loc Effect/CLICK2 nil)
  (dosync (ref-set lifting? true))
  (let [tuples (for [x (range -2 3) y (range -1 4) z (range -2 3)]
                 (let [block (.getBlock (.add (.clone center-loc) x y z))]
                   [(.getType block) (.getData block)]))
        new-coords (for [x (range -2 3) y (range -1 4) z (range -2 3)]
                     [x (+ y ydiff) z])
        zipped (map #(list %1 %2) tuples new-coords)]
    (doseq [[[btype bdata] [x y z]] zipped]
      "I don't know why but this line is vital")
    (doseq [[[btype bdata] [x y z]] zipped]
      (let [block (.getBlock (.add (.clone center-loc) x y z))]
        (.setType block m/air)))
    (doseq [[[btype bdata] [x y z]] zipped]
      (let [block (.getBlock (.add (.clone center-loc) x y z))]
        (.setType block btype)
        (.setData block bdata)))
    (doseq [x (range -2 3)  z (range -2 3)]
      (let [block (.getBlock (.add (.clone center-loc) x (- 0 ydiff) z))]
        (.setType block m/air))))
  (doseq [player (Bukkit/getOnlinePlayers)
          :when (.isInAABB
                  (.toVector (.getLocation player))
                  (.toVector (.add (.clone center-loc) -3 -3 -3))
                  (.toVector (.add (.clone center-loc) 3 3 3)))
          :let [loc (.getLocation player)]]
    (.add loc 0 ydiff 0)
    (.teleport player loc)
    (.sendMessage player "elevator!"))
  (dosync (ref-set lifting? false)))

(defn lift-up [center-loc]
  (lift center-loc 1))

(defn lift-down [center-loc]
  (lift center-loc -1))

(comment (defn elevator [player door]
  (let [loc (.getLocation (.getBlock (.getLocation player)))]
    (when (and
            (every?
              #(= m/cobblestone %)
              (for [x [-1 0 1] z [-1 0 1]]
                (.getType (.getBlock (.add (.clone loc) x -1 z)))))
            (every?
              #(= m/cobblestone %)
              (for [x [-1 0 1] z [-1 0 1]]
                (.getType (.getBlock (.add (.clone loc) x 2 z))))))
      (.teleport player (.add (.getLocation player) 0 10 0))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x -1 z)) m/air))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x 2 z)) m/air))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x 9 z)) m/cobblestone))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x 12 z)) m/cobblestone))
      (.setType (.getBlock (.getLocation door)) m/air)
      (.setType (.getBlock (.add (.getLocation door) 0 10 0)) m/iron-door)))))

(def plowed-sands (atom #{}))

(defn minecart-accelerate [cart]
  (let [dire (.getDirection (.getLocation cart))]
    (let [x (- (* (.getX dire) 0.707) (* (.getZ dire) 0.707))
          z (+ (* (.getX dire) 0.707) (* (.getZ dire) 0.707))]
      (.setVelocity cart (Vector. (* (.getZ dire) -2) 0.0 (* (.getX dire) 2))))))

(defn player-left-click-event [evt player]
  (let [item (.getItemInHand player)]
    (cond
      (instance? Minecart (.getVehicle player))
      (do
        (.setCancelled evt true)
        (minecart-accelerate (.getVehicle player)))

      (and
        (= (.. player (getItemInHand) (getType)) m/gold-sword)
        (= (.getHealth player) (.getMaxHealth player)))
      (if (< 33 (.getDurability item))
        (c/consume-item player)
        (do
          (when (= 0 (rand-int 10))
            (item/modify-durability item inc))
          (if (empty? (.getEnchantments item))
            (let [snowball (.launchProjectile player Snowball)]
              (loc/play-sound (.getLocation player) s/zombie-metal 0.5 1.0)
              (swap! special-snowball-set conj snowball)
              (.setVelocity snowball (.multiply (.getVelocity snowball) 3)))
            (let [arrow (.launchProjectile player Arrow)]
              (.setVelocity arrow (.multiply (.getVelocity arrow) 1.3))
              (when-let [skill (arrow-skill-of player)]
                (skill/arrow-shoot skill nil arrow player))
              #_(when (= 0 (rand-int 1000))
                (let [msg (format "%s's gold sword lost enchant" (.getDisplayName player))]
                  (lingr/say-in-mcujm msg)
                  (c/broadcast msg))
                (doseq [[enchant level] (.getEnchantments item)]
                  (.removeEnchantment item enchant)))))))

      (and
        (= m/feather (.getType item))
        (< 0.01 (.getFallDistance player)))
      (do
        (.setVelocity player (doto
                               (.getVelocity player)
                               (.setY 0)))
        (.setFallDistance player 0.0)
        (when (= 0 (rand-int 3))
          (loc/play-sound (.getLocation player) s/chicken-idle 0.8 1.5)
          (c/consume-item player)))

      (and (.getClickedBlock evt)
           (= m/stone-button (.getType (.getClickedBlock evt)))
           (block/blazon? m/emerald-block (.getBlock (.add (.getLocation player) 0 -1 0))))
      (lift-up (.getLocation player)))))

(defn y->pitch [y]
  (* 90 (Math/sin (* y -0.5 Math/PI))))

(defn xz->yaw [x z]
  (mod (* 90 (/ (Math/atan2 (* -1 x) z) (/ Math/PI 2))) 360))

(defn autofocus [player]
  (let [ploc (.getLocation player)
        monsters (filter #(instance? Monster %) (.getNearbyEntities player 50 10 50))]
    (when (< 0 (count monsters))
      (let [target (apply min-key (cons #(.distance ploc (.getLocation %))
                                        monsters))]
        (let [direction (.normalize (.toVector (.subtract (.getLocation target) ploc))) ]
          #_(.sendMessage player (str direction))
          (.setPitch ploc (y->pitch (.getY direction)))
          (.setYaw ploc (xz->yaw (.getX (.normalize direction)) (.getZ (.normalize direction))))
          (.teleport player ploc))))))

(defn pan-gold-wait [player block]
  (let [item (.getItemInHand player)]
    (.playEffect (.getWorld block) (.getLocation block) Effect/STEP_SOUND m/torch)
    (if (> (.getDurability item) (item/hoe-durabilities (.getType item)))
      (.remove (.getInventory player) item)
      (do
        (item/modify-durability item #(+ 2 %))
        (future
          (swap! plowed-sands conj block)
          (Thread/sleep (+ 1000 (* 5 (item/hoe-durabilities (.getType item)))))
          (swap! plowed-sands disj block)
          (when (#{m/sand m/gravel} (.getType block))
            (.playEffect (.getWorld block) (.getLocation block) Effect/STEP_SOUND m/sand)
            (when (= 0 (rand-int 2))
              (.setType block (rand-nth [m/sandstone m/air m/clay])))))))))

(defn night? [world]
  (< 13500 (.getTime world)))

(defn burning? [player]
  (assert (instance? Player player) player)
  (and (< 0 (.getFireTicks player))
       (not (.hasPotionEffect player PotionEffectType/FIRE_RESISTANCE))))

(defn chest-popcorn-probability [block player]
  (assert (#{m/chest m/ender-chest} (.getType block)) block)
  (defn on-spider? [player]
    (instance? Spider (.getVehicle player)))
  (let [world (.getWorld block)
        base0 (if (= m/chest (.getType block)) 24 39)
        base (int (* base0
                     (if (night? world) 1.3 1)
                     (if (.hasStorm world) 1.3 1)
                     (if (.isThundering world) 1.3 1)
                     (if (on-spider? player) 1.3 1)
                     (if (burning? player) 2.0 1)))
        inventory (if (= m/chest (.getType block))
                    (.getBlockInventory (.getState block))
                    (.getEnderChest player))
        contents (filter identity (.getContents inventory))
        emeralds (filter #(= m/emerald (.getType %)) contents)
        total-emeralds (apply + (map #(.getAmount %) emeralds))
        emerald-blocks (filter #(= m/emerald-block (.getType %)) contents)
        total-emerald-blocks (apply + (map #(.getAmount %) emerald-blocks))
        emerald-effect-num (- total-emeralds (* total-emerald-blocks 5))]
    (min (if (= m/chest (.getType block)) 90 95)
         (+ base (* emerald-effect-num 5)))))

(defn player-right-click-event [evt player]
  (defn else []
    """just for DRY"""
    (cond
      (and
        (player/zombie? player)
        (= m/milk-bucket (.getMaterial evt)))
      (do
        (player/rebirth-from-zombie player)
        (when (= 0 (rand-int 3))
          (.setType (.getItemInHand player) m/bucket)))

      (and
        (.getAllowFlight player)
        (= m/coal (.getMaterial evt)))
      (do
        (.setVelocity player (.multiply (.getDirection (.getLocation player)) 4))
        (c/consume-item player))

      (and
        (.isSneaking player)
        ((conj item/swords m/bow) (.getType (.getItemInHand player))))
      (autofocus player)

      (= m/feather (.getMaterial evt))
      (player-super-jump evt player)))
  (if-let [block (.getClickedBlock evt)]
    (cond
      (and (= m/stone-button (.getType block))
           (block/blazon? m/emerald-block (.getBlock (.add (.getLocation player) 0 -1 0))))
      (lift-down (.getLocation player))

      (and (= m/shears (.getType (.getItemInHand player)))
           (= m/wool (.getType block)))
      (do
        (.setType block m/air)
        (item/modify-durability (.getItemInHand player) inc)
        (dotimes [_ 3]
          (loc/drop-item (.getLocation block) (ItemStack. m/string 1))))

      (= m/blaze-rod (.. player (getItemInHand) (getType)))
      (if (and (block/blazon? m/iron-ore (.getBlock (.add (.getLocation block) 0 -1 0)))
               (#{m/chest m/ender-chest} (.getType block)))
        (let [probability (chest-popcorn-probability block player)]
          (.setCancelled evt true)
          (.sendMessage player (format "current probability: %.2f" (/ probability 100.0))))
        (do
          (.sendMessage player (format "%s: %1.3f" (.getType block) (.getTemperature block)))
          (.sendMessage player (format "biome: %s" (.getBiome block)))))

      (and
        (= m/chest (.getType block))
        (= m/tnt (.getType (.getBlock (.add (.getLocation block) 0 -1 0)))))
      (do
        (.setType (.getBlock (.add (.getLocation block) 0 -1 0)) m/air)
        (loc/spawn (.add (.getLocation block) 0 1 0) TNTPrimed))

      (= m/cake-block (.getType block))
      (if-let [death-point (player/death-location-of player)]
        (do
          (.load (.getChunk death-point))
          (c/broadcast (str (.getDisplayName player) " is teleporting to the last death place..."))
          (.teleport player death-point))
        (.sendMessage player "You didn't die yet."))

      (and
        (= 0 (rand-int 15))
        (= m/bowl (.getType (.getItemInHand player)))
        (@plowed-sands block))
      (let [item-type (if (= 0 (rand-int 50)) m/gold-ingot m/gold-nugget)]
        (loc/drop-item (.getLocation block) (ItemStack. item-type)))

      (and
        (item/hoe-durabilities (.. player (getItemInHand) (getType)))
        (and (= Biome/RIVER (.getBiome block))
             (not (@plowed-sands block))
             (#{m/sand m/gravel} (.getType block))
             (> 64.0 (.getY (.getLocation block)))
             (.isLiquid (.getBlock (.add (.getLocation block) 0 1 0)))))
      (pan-gold-wait player block)

      :else
      (else))
    (else)))

(defn player-interact-event [evt]
  (let [player (.getPlayer evt)
        action (.getAction evt)]
    (cond
      (= action Action/PHYSICAL)
      (transport/teleport-up player (.getClickedBlock evt))

      (or (= action Action/LEFT_CLICK_AIR)
          (= action Action/LEFT_CLICK_BLOCK))
      (player-left-click-event evt player)

      (or
          (= action Action/RIGHT_CLICK_AIR)
          (= action Action/RIGHT_CLICK_BLOCK))
      (player-right-click-event evt player))))

(defn player-item-held-event [evt]
  (let [player (.getPlayer evt)]
    (when (instance? Minecart (.getVehicle player))
      (let [cart (.getVehicle player)
            diff (mod (- (.getNewSlot evt) (.getPreviousSlot evt)) 9)]
        (when-let [anglediff ({1 15 8 -15} diff)]
          (let [l (.getLocation cart)]
            (.setYaw l (+ (.getYaw l) anglediff))
            (.teleport cart l)))))))

(defn player-drop-item-event [evt]
  (let [item (.getItemDrop evt)
        itemstack (.getItemStack item)
        table-equip
        {m/wood-sword [m/stick m/wood m/wood]
         m/stone-sword [m/stick m/cobblestone m/cobblestone]
         m/iron-sword [m/stick m/iron-ingot m/iron-ingot]
         m/gold-sword [m/stick m/gold-ingot m/gold-ingot]
         m/diamond-sword [m/stick m/diamond m/diamond]}
        player (.getPlayer evt)]
    (when (.isSprinting player)
      (.setVelocity item (.add (.multiply (.getVelocity item) 2.0) (Vector. 0.0 0.5 0.0))))
    (cond
      (table-equip (.getType itemstack))
      (future
        (let [parts (table-equip (.getType itemstack))]
          (Thread/sleep 8000)
          (later
            (when (and
                    (not (.isDead item))
                    (#{m/furnace m/burning-furnace}
                        (.getType (.getBlock (.add (.getLocation item) 0 -1 0)))))
              (doseq [p parts]
                (loc/drop-item
                     (.getLocation item)
                     (ItemStack. (if (not= 0 (rand-int 10)) p m/coal) (.getAmount itemstack))))
              (when-not (empty? (.getEnchantments itemstack))
                (let [exp (loc/spawn (.getLocation item) ExperienceOrb)]
                  (.setExperience exp (rand-nth (range 10 20)))))
              (.remove item)))))

      (and
        (item/pickaxes (.getType itemstack))
        (= 'pickaxe-skill-teleport (pickaxe-skill-of player)))
      (future
        (Thread/sleep 2000)
        (later
          (when-not (.isDead item)
            (c/teleport-without-angle player (.getLocation item))))))))

(defn player-entity-with-string-event [evt player target]
  (c/consume-item player)
  (.setPassenger player target)
  (.setCancelled evt true)
  (.setFlySpeed player 0.15)
  (condp instance? target
    Pig (.setAllowFlight player true)
    Chicken (.setAllowFlight player true)
    Squid (do
            (lingr/say-in-mcujm (str (.getDisplayName player) " is flying with squid!"))
            (.setAllowFlight player true)
            (.setFlySpeed player 0.30))
    Player
    (future
      (Thread/sleep 10000)
      (when (= player (.getPassenger player))
        (later (.setPassenger player nil))))
    nil))

(defn player-rightclick-chicken [player target]
  (when (= 0 (rand-int 10))
    (.setFoodLevel player (dec (.getFoodLevel player))))
  (loc/drop-item
       (.getLocation target)
       (ItemStack. (rand-nth [m/feather m/feather
                              m/sand]))))

(defn player-rightclick-cow [player target]
  (let [itemstack (.getItemInHand player)]
    (if (and
          itemstack
          (= m/cooked-beef (.getType itemstack)))
      (chimera-cow/birth player target)
      (.dropItemNaturally (.getWorld target) (.getLocation target) (ItemStack. m/coal)))))

(defn player-rightclick-poweredminecart [player cart]
  (when (= m/coal (.getType (.getItemInHand player)))
    (.setMaxSpeed cart 5.0)
    (let [v (.getVelocity cart)
          x (.getX v)
          z (.getY v)
          r2 (max (+ (* x x) (* z z)) 0.1)
          new-x (* 2 (/ x r2))
          new-z (* 2 (/ z r2))]
      (future
        (Thread/sleep 100)
        (later (.setVelocity cart (Vector. new-x (.getY v) new-z)))))))

(defn player-rightclick-villager [player villager]
  (let [default #(loc/drop-item (.getLocation villager) (ItemStack. m/cake))]
    (if-let [item (.getItemInHand player)]
      (condp = (.getType item)
        m/brown-mushroom (do
                           (.setProfession villager Villager$Profession/LIBRARIAN)
                           (c/consume-item player))
        m/red-mushroom (do
                         (.setProfession villager Villager$Profession/PRIEST)
                         (c/consume-item player))
        m/yellow-flower (do
                          (.setProfession villager Villager$Profession/BLACKSMITH)
                          (c/consume-item player))
        m/red-rose (do
                     (.setProfession villager Villager$Profession/BUTCHER)
                     (c/consume-item player))
        m/redstone (do
                     (.setProfession villager Villager$Profession/FARMER)
                     (c/consume-item player))
        (default))
      (default))))

(defn player-interact-entity-event [evt]
  (let [player (.getPlayer evt)
        target (.getRightClicked evt)]
    (cond
      (when-let [passenger (.getPassenger player)]
        (= passenger target))
      (do
        (.setAllowFlight player false)
        (.eject player))

      (= m/string (.getType (.getItemInHand player)))
      (player-entity-with-string-event evt player target)

      :else
      (condp instance? target
        PoweredMinecart (player-rightclick-poweredminecart player target)

        PigZombie
        (when (= m/wheat (.getType (.getItemInHand player)))
          (c/swap-entity target Pig)
          (c/consume-item player))

        Pig
        (if (= m/rotten-flesh (.getType (.getItemInHand player)))
          (do
            (c/swap-entity target PigZombie)
            (c/consume-item player))
          (loc/drop-item (.getLocation target)
               (.toItemStack (doto (Dye.) (.setColor DyeColor/BROWN)) 1)))

        Zombie
        (if (= m/rotten-flesh (.getType (.getItemInHand player)))
          (do
            (loc/spawn (.getLocation target) Giant)
            (c/broadcast "Giant!")
            (c/consume-item player)
            (.remove target))
          (loc/drop-item (.getLocation target) (ItemStack. m/rotten-flesh)))

        Villager (player-rightclick-villager player target)

        Squid
        (let [msg (clojure.string/join "" (map char [65394 65398 65398 65436
                                                     65394 65394 65411 65438
                                                     65405]))
              msg2 (format "%s: %s" (.getDisplayName player) msg)]
          (lingr/say-in-mcujm msg2)
          (c/broadcast msg2)
          (.setFoodLevel player 0))

        Player (player-rightclick-player player target)

        Sheep
        (loc/drop-item (.getLocation target) (.toItemStack (Wool. (rand-nth (DyeColor/values))) 1))

        Chicken (player-rightclick-chicken player target)
        Cow (player-rightclick-cow player target)

        Creeper
        (loc/drop-item (.getLocation target) (ItemStack.  m/sulphur))

        Skeleton (loc/drop-item (.getLocation target) (ItemStack. m/arrow))

        Spider
        (do
          (when (= player (.getTarget target))
            (.setTarget target nil))
          (.setPassenger target player))
        #_(loc/drop-item
          (.getLocation target)
          (ItemStack. (rand-nth [m/spider-eye m/dirt
                                 m/sand m/string])))

        IronGolem
        (loc/drop-item (.getLocation target) (ItemStack. (rand-nth
                                                           [m/yellow-flower
                                                            m/red-rose])))

        Snowman
        (loc/drop-item (.getLocation target) (ItemStack. (rand-nth
                                                           [m/dirt
                                                            m/snow-ball
                                                            m/snow-ball
                                                            m/snow-ball
                                                            m/snow-ball
                                                            m/snow-ball
                                                            m/bucket])))

        Enderman
        (if (= 0 (rand-int 50))
          (.setPassenger target player)
          (loc/drop-item (.getLocation target)
             (ItemStack. (rand-nth [m/stick m/ender-stone
                                    m/ender-stone
                                    m/ender-stone
                                    m/ender-stone
                                    m/ender-stone
                                    m/ender-pearl]))))
nil))))

(defn player-egg-throw-event [evt]
  (egg/throw-event evt))

(defn player-level-change-event [evt]
  (let [player (.getPlayer evt)]
    (when (< (.getOldLevel evt) (.getNewLevel evt))
      (loc/play-sound (.getLocation player) s/level-up 0.5 1.0)
      (c/broadcast (format
                     "Level up! %s is Lv%s"
                     (.getDisplayName player)
                     (.getNewLevel evt))))))

(defn chain-entity [entity shooter]
  (let [block (.getBlock (.getLocation entity))]
    (when-not (.isLiquid block)
      (let [msg (str (.getDisplayName shooter) " chained " (c/entity2name entity))]
        (.sendMessage shooter msg)
        (lingr/say-in-mcujm msg))
      (.setType block m/web)
      (future
        (Thread/sleep 10000)
        (when (= m/web (.getType block))
          (later (.setType block m/air)))))))

(def chicken-attacking (atom 0))
(defn chicken-touch-player [chicken player]
  (when (not= @chicken-attacking 0)
    (.teleport chicken (.getLocation player))
    (.damage player (rand-int 3) chicken)))

(defn periodically-entity-touch-player-event []
  (doseq [player (Bukkit/getOnlinePlayers)
          chicken (take 3 (filter #(instance? Chicken %) (.getNearbyEntities player 2 2 2)))]
      (chicken-touch-player chicken player)))

(defn periodically-flyers []
  (doseq [player (Bukkit/getOnlinePlayers)
          :when (.getAllowFlight player)]
    (if (.getPassenger player)
      (let [randvelodiff #(/ (- (rand) 0.5) 2.0)]
        (when (and (not= "ujm" (.getDisplayName player))
                  (= 0 (rand-int 2)))
          (c/add-velocity player (randvelodiff) (randvelodiff) (randvelodiff))))
      (do
        (.setAllowFlight player false)
        (.setFallDistance player 0.0)))))

(defn periodically []
  (periodically-flyers)
  (periodically-entity-touch-player-event)
  (chimera-cow/periodically)
  (player/periodically-zombie-player)
  nil)

(defn player-respawn-event [evt]
  (let [player (.getPlayer evt)]
    (future
      (.setHealth player (/ (.getMaxHealth player) 3))
      (.setFoodLevel player 5))))

(defn spawn-block-generater [entity]
  (let [loc (.getLocation entity)]
    (when
      (and
        (= m/diamond-block (.getType (.getBlock (.add (.clone loc) 0 -1 0))))
        (every? identity
                (for [x [-1 0 1] z [-1 0 1]]
                  (= m/gold-block (.getType (.getBlock (.add (.clone loc) x -2 z)))))))
      (let [block (.getBlock (.add (.clone loc) 0 -1 0))]
        (.setType block m/mob-spawner)
        (.setSpawnedType (.getState block) (.getType entity)))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x -2 z)) m/mossy-cobblestone))
      (later (.remove entity))
      #_(.setCancelled evt true))))

(defn pig-murder-event [entity]
  (when-let [killer (.getKiller entity)]
    (when (instance? Player killer)
      (.sendMessage killer "PIG: Pig Is God"))
    (.setFireTicks killer 1000)))

(defn entity-murder-event [evt entity killer]
  (if (instance? Player entity)
    (player/death-event evt entity)
    (let [location (.getLocation entity)]
    (when (instance? Player killer)
      (condp instance? entity
        Pig (pig-murder-event entity)
        PigZombie nil
        Zombie ((rand-nth
                  [#(do
                      (loc/explode location 0 false)
                      (when (= m/air (.getType (.getBlock location)))
                        (.setType (.getBlock location) m/gravel)))
                   #(loc/spawn location Villager)
                   #(loc/spawn location Silverfish)
                   #(loc/drop-item location (ItemStack. m/iron-sword))]))
        Giant (.setDroppedExp evt 200)
        Creeper (.setDroppedExp evt 10)
        Blaze (when (= "world" (.getName (.getWorld entity)))
                (blaze2-murder-event evt entity killer))
        Ghast (when (= "world" (.getName (.getWorld entity)))
                (ghast2-murder-event evt entity killer))
        CaveSpider (when (= 0 (rand-int 3))
                     (loc/drop-item location (ItemStack. m/gold-sword)))
        nil)
      (when (chimera-cow/is? entity)
        (chimera-cow/murder-event evt entity killer))
      (.setDroppedExp evt (int (* (.getDroppedExp evt) (/ 15 (.getHealth killer)))))
      (when (= skill/arrow-exp (arrow-skill-of killer))
        (.setDroppedExp evt (int (* (.getDroppedExp evt) 3))))
      (when (and
              (< 5 (.getDroppedExp evt))
              (= 0 (rand-int 10)))
        (loc/drop-item location (ItemStack. m/emerald (rand-nth [1 2]))))
      (player/record-and-report killer entity evt)))))

(defn entity-orthothanasia-event [evt entity]
  (when (instance? Player entity)
    (player/death-event evt entity)))

(defn entity-death-event [evt]
  (let [entity (.getEntity evt)]
    (if-let [killer (.getKiller entity)]
      (entity-murder-event evt entity killer)
      (entity-orthothanasia-event evt entity))))

(defn creeper-explosion-1 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0)
  (doseq [e (filter #(instance? LivingEntity %) (.getNearbyEntities entity 5 5 5))]
    (let [v (.multiply (.toVector (.subtract (.getLocation e) (.getLocation entity))) 2.0)
          x (- 5 (.getX v))
          z (- 5 (.getZ v))]
      (when (instance? Player e)
        (.sendMessage e "Air Explosion"))
      (.setVelocity e (Vector. x 1.5 z))))
  (comment (let [another (.spawn (.getWorld entity) (.getLocation entity) Creeper)]
             (.setVelocity another (Vector. 0 1 0)))))

(defn creeper-explosion-2 [evt entity]
  (.setCancelled evt true)
  (let [loc (.getLocation entity)]
    (.setType (.getBlock loc) m/pumpkin)
    (c/broadcast "break the bomb before it explodes!")
    (future
      (Thread/sleep 7000)
      (when (= (.getType (.getBlock loc)) m/pumpkin)
        (c/broadcast "zawa...")
        (Thread/sleep 1000)
        (when (= (.getType (.getBlock loc)) m/pumpkin)
          (later (.setType (.getBlock loc) m/air))
          (let [tnt (loc/spawn loc TNTPrimed)]
            (Thread/sleep 1000)
            (later
              (.remove tnt)
              (c/broadcast "big explosion!")
              (loc/explode loc 6 true))))))))

(defn creeper-explosion-3 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0))

(defn creeper-explosion-4 [evt entity]
  (defn nearest-player [entity]
    (apply min-key (cons #(.distance (.getLocation entity) (.getLocation %))
                         (Bukkit/getOnlinePlayers))))
  (.setCancelled evt true)
  (loc/play-sound (.getLocation entity) s/explode-old 1.0 1.3)
  (prn (nearest-player entity))
  (let [target (nearest-player entity)
        rand1 (fn [] (* 0.8 (- (rand) 0.5)))
        direction (.normalize
                    (.toVector
                      (.subtract
                        (.getLocation target)
                        (.getLocation entity))))]
    (dotimes [_ 80]
      (let [snowball (.launchProjectile entity Snowball)]
        (.setVelocity
          snowball
          (Vector. (+ (rand1) (.getX direction))
                   (+ (rand1) (.getY direction))
                   (+ (rand1) (.getZ direction))))))))

(def creeper-explosion-idx (atom 0))
(defn current-creeper-explosion []
  (get [creeper-explosion-4
        (constantly nil)
        creeper-explosion-1
        creeper-explosion-2
        creeper-explosion-3
        ] (rem @creeper-explosion-idx 5)))

(defn entity-explode-event [evt]
  (if-let [entity (.getEntity evt)]
    (let [ename (c/entity2name entity)]
      (condp instance? entity
        Creeper
        (do
          ((current-creeper-explosion) evt entity)
          (swap! creeper-explosion-idx inc))

        Fireball
        (when (instance? Cow (.getShooter entity))
          (.setCancelled evt true))

        SmallFireball
        nil

        Fireball
        nil

        (let [players-nearby (filter #(instance? Player %) (.getNearbyEntities entity 5 5 5))]
          (when (and
                  ename
                  (not-empty players-nearby)
                  (not (instance? EnderDragon entity)))
            (lingr/say-in-mcujm
               (format
                 "%s is exploding near %s"
                 ename
                 (clojure.string/join ", " (map #(.getDisplayName %) players-nearby))))))))
    (prn 'explosion-without-entity)))

(defn digg-entity [target shooter]
  (loop [depth -1]
    (when (> depth -3)
      (let [loc (.add (.clone (.getLocation target)) 0 depth 0)
            block (.getBlock loc)]
        (when (#{m/grass m/dirt m/stone
                 m/gravel m/sand m/sandstone
                 m/cobblestone m/soul-sand
                 m/netherrack m/air} (.getType block))
          (.breakNaturally block (ItemStack. m/diamond-pickaxe))
          (let [block-loc (.getLocation block)]
            (.setYaw block-loc (.getYaw loc))
            (.setPitch block-loc (.getPitch loc))
            (.teleport target (.add block-loc 0.5 1 0.5)))
          (recur (dec depth)))))))

(defn scouter [evt attacker target]
  (when-let [helmet (.getHelmet (.getInventory attacker))]
    (when (= m/stone-plate (.getType helmet))
      (let [damage (if (.isCancelled evt)
                     0
                     (.getDamage evt))]
        (.sendMessage attacker (format "damage: %s, HP: %s -> %s (%s)"
                                       damage
                                       (.getHealth target)
                                       (- (.getHealth target) damage)
                                       (c/entity2name target)))))))

(defn arrow-damages-entity-event-internal [evt arrow target]
  (when-let [shooter (.getShooter arrow)]
    (condp instance? shooter
      Blaze
      (do
        "arrow from blaze = always it's by blaze2"
        (blaze2-arrow-hit target))

      Player
      (do
        (skill/arrow-damage-entity (arrow-skill-of shooter) evt arrow target)
        (cond
          (.contains (.getInventory shooter) m/web)
          (do
            (chain-entity target shooter)
            (c/consume-itemstack (.getInventory shooter) m/web))
          #_(
          (= arrow-skill-explosion (arrow-skill-of shooter))
          (.damage target 10 shooter)

          (= egg/skill-ice (egg/skill-of shooter))
          (c/freeze-for-20-sec target)

          (= 'trap (arrow-skill-of shooter))
          ((rand-nth [chain-entity
                      (comp c/freeze-for-20-sec first list)
                      digg-entity])
             target shooter)

          (= 'digg (arrow-skill-of shooter))
          (digg-entity target shooter)

          (= arrow-skill-pumpkin (arrow-skill-of shooter))
          (condp instance? target
            Player
            (let [helmet (.getHelmet (.getInventory target))]
              (.setHelmet (.getInventory target) (ItemStack. m/pumpkin))
              (when helmet
                (.dropItemNaturally (.getWorld target) (.getLocation target) helmet)))

            LivingEntity
            (let [klass (.getEntityClass (.getType target))
                  health (.getHealth target)
                  loc (.getLocation target)
                  block (.getBlock loc)
                  block-type (.getType block)]
              (.remove target)
              (.remove arrow)
              (.setType block m/pumpkin)
              (future
                (Thread/sleep 3000)
                (let [newmob (loc/spawn loc klass)]
                  (if (= m/pumpkin (.getType block))
                    (do
                      (.setHealth newmob health)
                      (.setType block block-type))
                    (if-let [player (first (filter #(instance? Player %)
                                                   (.getNearbyEntities newmob 3 3 3)))]
                      (.damage newmob (.getMaxHealth newmob) player)
                      (.damage newmob (.getMaxHealth newmob)))))))

            nil)

          (= arrow-skill-diamond (arrow-skill-of shooter))
          (cond
            (some #(instance? % target) [Zombie Skeleton])
            (do
              (loc/spawn (.getLocation target) Villager)
              (.remove target))

            :else
            (do
              (.setHealth target (.getMaxHealth target))
              (.damage target 1 shooter)
              (.setHealth target (.getMaxHealth target))
              (when (instance? Player target)
                (.setFoodLevel target 20))
              (.setCancelled evt true)
              (c/broadcast
                 "Crazy diamond recovers "
                 (if (instance? Player target)
                   (.getDisplayName target)
                   (c/entity2name target)))))

          (= 'arrow-skill-poison (arrow-skill-of shooter))
          (reaction-skill-poison nil target)

          (= 'super-knockback (arrow-skill-of shooter))
          (let [direction (.subtract (.getLocation shooter)
                                     (.getLocation target))
                vector (.multiply (.normalize (.toVector direction)) 3)]
            (c/add-velocity shooter (.getX vector) (+ (.getY vector) 1.0) (.getY vector))
            (future-call
              (let [vector (.multiply vector -1)]
                (Thread/sleep 1)
                (c/add-velocity target (.getX vector) (+ (.getY vector) 1.0) (.getY vector)))))

          (= 'mobchange (arrow-skill-of shooter))
          (do
            (let [change-to (rand-nth [Blaze Boat CaveSpider Chicken Chicken
                                       Chicken Cow Cow Cow Creeper Enderman
                                       Ghast Giant MagmaCube Minecart
                                       MushroomCow Pig Pig Pig PigZombie
                                       PoweredMinecart Sheep Sheep Sheep
                                       Silverfish Skeleton Slime Snowman Spider
                                       Squid Squid Squid StorageMinecart
                                       TNTPrimed Villager Wolf Ocelot Zombie])]
              (loc/spawn (.getLocation target) change-to))
            (.remove target))

          (= arrow-skill-pull (arrow-skill-of shooter))
          (.teleport target shooter)

          (= 'cart (arrow-skill-of shooter))
          (let [cart (loc/spawn (.getLocation target) Minecart)]
            (.setPassenger cart target))))
      (scouter evt shooter target))

      nil)))

(defn arrow-damages-entity-event [evt arrow target]
  (if (and
        (not= 0 (rand-int 10))
        (instance? Player target)
        (or (not (instance? Player (.getShooter arrow)))
            #_(use skill/arrow-reflectable? instead)
            (skill/arrow-reflectable? (arrow-skill-of (.getShooter arrow))))
        (when-let [chestplate (.getChestplate (.getInventory target))]
          (and
            (= m/leather-chestplate (.getType chestplate))
            (not-empty (.getEnchantments chestplate)))))
    (do
      (c/broadcast (.getDisplayName target) "'s enchanted leather chestplate reflects arrows!")
      (arrow/reflect evt arrow target))
    (arrow-damages-entity-event-internal evt arrow target)))

(comment (let [cart (.spawn (.getWorld target) (.getLocation target) Minecart)]
           (future-call #(let [b (.getBlock (.getLocation target))]
                           (.setType b m/rails)))
           (.setPassenger cart target)
           (c/add-velocity cart 0 5 0)))

(defn player-attacks-spider-event [evt player spider]
  (let [cave-spider (loc/spawn (.getLocation spider) CaveSpider)]
    (.sendMessage player "The spider turned into a cave spider!")
    (when-let [passenger (.getPassenger spider)]
      (later (.setPassenger cave-spider passenger)))
    (later
      (.setVelocity
        cave-spider
        (.normalize (.toVector (.subtract (.getLocation cave-spider) (.getLocation player))))))
    (.setTarget cave-spider player))
  (.remove spider))

(defn player-attacks-pig-event [evt player pig]
  (when (and (= 0 (rand-int 2))
             (not (.isDead pig)))
    (let [another-pig (loc/spawn (.getLocation pig) PigZombie)]
      (.setTarget another-pig player)
      (future
        (Thread/sleep 3000)
        (when-not (.isDead another-pig)
          (later (.remove another-pig)))))))

(defn player-attacks-chicken-event [_ player chicken]
  (when (not= 0 (rand-int 3))
    (let [location (.getLocation player)
          world (.getWorld location)]
      (swap! chicken-attacking inc)
      (future
        (Thread/sleep 20000)
        (swap! chicken-attacking dec))
      (doseq [x (range -4 5) z (range -4 5)
              :let [chicken (loc/spawn (.add (.clone location) x 3 z) Chicken)]]
        (future
          (Thread/sleep (+ 10000 (rand-int 8000)))
          (when-not (.isDead chicken)
            (later
              (when (not (.isLoaded (.getChunk (.getLocation chicken))))
                #_(prn 'isLoaded-not chicken))
              (.remove chicken))))))))

(defn fish-damages-entity-event [evt fish target]
  (if-let [shooter (.getShooter fish)]
    (let [table {Cow m/raw-beef
                 Pig m/pork
                 Chicken m/raw-chicken
                 Zombie m/leather-chestplate
                 Skeleton m/bow
                 Creeper m/sulphur
                 CaveSpider m/iron-ingot
                 Spider m/redstone
                 Sheep m/bed
                 Villager m/apple
                 Silverfish m/iron-sword
                 IronGolem m/fishing-rod
                 Squid m/raw-fish
                 Blaze m/glowstone-dust
                 MagmaCube m/flint
                 Giant m/dirt}]
      (if-let [m (last (first (filter #(instance? (first %) target) table)))]
        (.dropItem (.getWorld target) (.getLocation target) (ItemStack. m 1))
        (cond
          (instance? Player target)
          (do
            (when-let [item (.getItemInHand target)]
              (.damage target 1)
              (.setItemInHand target (ItemStack. m/air))
              (.setItemInHand shooter item)
              (lingr/say-in-mcujm (format "%s fished %s"
                                     (.getDisplayName shooter)
                                     (.getDisplayName target)))))

          :else
          (.teleport target shooter))))))

(defn entity-damage-by-drawning-event [evt target]
  (cond
    (player/zombie? target)
    (do
      (.setCancelled evt true)
      (player/rebirth-from-zombie target))

    (and
      (instance? Player target)
      (.getHelmet (.getInventory target))
      (= m/glass (.getType (.getHelmet (.getInventory target)))))
    (.setCancelled evt true)))

(defn blaze2-get-damaged-by-special-snowball [snowball blaze2 shooter]
  (assert (instance? Player shooter) shooter)
  (assert (instance? Blaze blaze2) blaze2)
  (assert (= "world" (.getName (.getWorld blaze2))) blaze2)
  (.sendMessage shooter "blaze2 likes your beam! HP max!")
  (.setHealth blaze2 (.getMaxHealth blaze2))
  (.setTarget blaze2 shooter)
  (let [v (.normalize (.toVector (.subtract (.getLocation shooter) (.getLocation blaze2))))]
    (later (c/add-velocity blaze2 (.getX v) (.getY v) (.getZ v)))))

(defn special-snowball-damage [snowball target shooter]
  (cond
    (and
      (instance? Blaze target)
      (= "world" (.getName (.getWorld target))))
    (blaze2-get-damaged-by-special-snowball snowball target shooter)

    :else
    (do
      (.setFireTicks target 200)
      (.damage target 1 shooter))))

(defn entity-damage-intent-event [evt target]
  (let [attacker (when (instance? EntityDamageByEntityEvent evt)
                   (.getDamager evt))]
    (cond
      (and (instance? Blaze target)
           (= "world" (.getName (.getWorld target))))
      (blaze2-get-damaged evt target)

      (and (instance? Ghast target)
           (= "world" (.getName (.getWorld target))))
      (ghast2-get-damaged evt target attacker)

      :else nil)
    (when (and
            (instance? Villager target)
            (instance? EntityDamageByEntityEvent evt)
            (instance? Player attacker))
      (.damage attacker (.getDamage evt)))
    (when (instance? Fish attacker)
      (fish-damages-entity-event evt attacker target))
    (when (instance? Snowball attacker)
      (if-let [shooter (.getShooter attacker)]
        (cond
          (@special-snowball-set attacker)
          (special-snowball-damage attacker target shooter)

          (instance? Snowman shooter)
          (special-snowball-damage attacker target shooter)

          (instance? Player shooter)
          (let [direction (.subtract (.getLocation target) (.getLocation (.getShooter attacker)))
                vector (.normalize (.toVector direction))]
            (.setCancelled evt true)
            (later (c/add-velocity target (.getX vector) (+ (.getY vector) 1.0) (.getZ vector))))

          (instance? Creeper shooter)
          (let [direction (.subtract (.getLocation target) (.getLocation (.getShooter attacker)))
                vector (.multiply (.normalize (.toVector direction)) 0.3)]
            (.setCancelled evt true)
            (later (c/add-velocity target (.getX vector) (+ (.getY vector) 0.3) (.getZ vector)))))))
    (when (instance? Enderman attacker)
      (when (instance? Player target)
        (if (= target (.getPassenger attacker))
          (.setCancelled evt true)
          (when (= 0 (rand-int 5))
            (.sendMessage target "Enderman picked you! (sneaking to get off)")
            (.setPassenger attacker target)))))
    (when (instance? Arrow attacker)
      (arrow-damages-entity-event evt attacker target))
    (when (instance? Egg attacker)
      (egg/damages-entity-event evt attacker target))
    (when (instance? Player attacker)
      (when-not (instance? Player target)
        (spawn-block-generater target))
      (when-let [item (.getItemInHand attacker)]
        (when (item/pickaxes (.getType item))
          (when (= 'pickaxe-skill-fire (pickaxe-skill-of attacker))
            (.setFireTicks target 200))))
      (when (and (instance? Spider target)
                 (not (instance? CaveSpider target)))
        (player-attacks-spider-event evt attacker target))
      (when (instance? Pig target)
        (player-attacks-pig-event evt attacker target))
      (when (instance? Chicken target)
        (player-attacks-chicken-event evt attacker target))
      (scouter evt attacker target))
    (when (and (instance? Player target) (instance? EntityDamageByEntityEvent evt))
      (when (instance? Fireball attacker)
        (when-let [shooter (.getShooter attacker)]
          (when (chimera-cow/is? shooter)
            (chimera-cow/fireball-hit-player evt target shooter attacker))))
      (when-not (instance? Egg attacker)
        (when-let [skill (reaction-skill-of target)]
          (let [actual-attacker
                (if (instance? Projectile attacker)
                  (.getShooter attacker)
                  attacker)]
            (when (and (not= actual-attacker target)
                       (not (instance? Wolf actual-attacker))
                       (not (instance? TNTPrimed actual-attacker))
                       (not (and
                              (instance? Player actual-attacker)
                              (= arrow-skill-diamond (arrow-skill-of actual-attacker)))))
              (skill target actual-attacker)))))
      (when (and (instance? Zombie attacker) (not (instance? PigZombie attacker)))
        (if (player/zombie? target)
          (.setCancelled evt true)
          (player/zombieze target)))
      (when (player/zombie? attacker)
        (player/zombieze target)
        (.sendMessage attacker "You made a friend")))
    (when (chimera-cow/is? target)
      (chimera-cow/damage-event evt target attacker))))

(defn entity-damage-event [evt]
  (let [target (.getEntity evt)]
    (cond
      (= EntityDamageEvent$DamageCause/DROWNING (.getCause evt))
      (entity-damage-by-drawning-event evt target)

      (= EntityDamageEvent$DamageCause/ENTITY_EXPLOSION (.getCause evt))
      (do
        #_(prn 'entity-explosion (.getEntity evt))
        (if (= 0 (rem @creeper-explosion-idx 3))
          (.setDamage evt (min (.getDamage evt) 19))
          (.setDamage evt 0)))

      (= EntityDamageEvent$DamageCause/FIRE_TICK (.getCause evt))
      (do
        #_(when (@fire-damage-exempt target)
          (.setCancelled evt true))
        (when (instance? Creeper target)
          (.setCancelled evt true)))

      (= EntityDamageEvent$DamageCause/FALL (.getCause evt))
      (cond
        (chimera-cow/is? target)
        (chimera-cow/fall-damage-event evt target)

        (when-let [vehicle (.getVehicle target)]
          (and vehicle (instance? Boat vehicle)))
        (.setCancelled evt true)

        :else
        (let [loc (.add (.getLocation target) 0 -1 0)]
          (doseq [fence [m/fence m/nether-fence]]
            (when (= fence (.getType (.getBlock loc)))
              (when (every? #(not= fence %)
                            (map (fn [[x z]]
                                   (.getType (.getBlock (.add (.clone loc) x 0 z))))
                                 [[-1 0] [1 0] [0 -1] [0 1]]))
                (when (instance? Player target)
                  (let [msg (str "Oh trap! " (.getDisplayName target) " was on a needle.")]
                    (lingr/say-in-mcujm msg)
                    (.sendMessage target msg)))
                (.damage target 100))))))
      :else
      (entity-damage-intent-event evt target))))

(def unpopcornable (atom {}))
(defn block-break-event [evt]
  (let [block (.getBlock evt)]
    (when-let [player (.getPlayer evt)]
      (when-let [item (.getItemInHand player)]
        (when (and
                (= m/ender-chest (.getType item))
                (= 0 (rand-int 5)))
          (let [loc (.getLocation block)
                enderman (loc/spawn loc Enderman)]
            (.teleport enderman (find-place loc (range -30 30)))
            (loc/play-sound loc s/enderman-teleport 1.0 1.0)
            (.setTarget enderman player)
            (.sendMessage player "Enderman spawned!")))
        (condp get (.getType item)
          #{m/air}
          (do
            (.sendMessage player "Your hand hurts!")
            (.damage player (rand-int 5)))

          item/pickaxes
          (let [btype (.getType block)]
            (condp get btype
              #{m/stone}
              (when (= 'pickaxe-skill-ore (pickaxe-skill-of player))
                (letfn [(f [blocktype]
                          (.setType block blocktype)
                          (.setCancelled evt true)
                          (loc/play-effect (.getLocation block) Effect/MOBSPAWNER_FLAMES nil))]
                  (cond
                    (= 0 (rand-int 10)) (f m/coal-ore)
                    (= 0 (rand-int 20)) (f m/iron-ore)
                    (= 0 (rand-int 30)) (f m/redstone-ore)
                    (= 0 (rand-int 40)) (f m/lapis-ore)
                    (= 0 (rand-int 50)) (f m/gold-ore)
                    (= 0 (rand-int 100)) (f m/emerald-ore)
                    (= 0 (rand-int 1000)) (f m/diamond-ore)
                    (= 0 (rand-int 300)) (f m/glowstone)
                    (= 0 (rand-int 1000)) (f m/lapis-block)
                    (= 0 (rand-int 1500)) (f m/iron-block)
                    (= 0 (rand-int 2000)) (f m/gold-block)
                    (= 0 (rand-int 50000)) (f m/diamond-block)
                    :else nil)))

              #{m/chest m/ender-chest}
              (when (block/blazon? m/iron-ore (.getBlock (.add (.clone (.getLocation block)) 0 -1 0)))
                (if (@unpopcornable player)
                  (do
                    (.sendMessage player "wait for a min...")
                    (loc/spawn (.add (.clone (.getLocation block)) 0 1 0)
                         (rand-nth [Chicken Pig Villager Ocelot]))
                    (.setCancelled evt true))
                  (do
                    (swap! unpopcornable assoc player true)
                    (future
                      (Thread/sleep 180000)
                      (swap! unpopcornable assoc player false))
                    (dosync
                      (ref-set popcorning (chest-popcorn-probability block player))
                      (cloft.chest/break-and-scatter block player)
                      (ref-set popcorning nil))
                    (let [msg (format "%s popcorned with %s!"
                                      (.getDisplayName player)
                                      btype)]
                      (lingr/say-in-mcujm msg)
                      (c/broadcast msg)))))
              nil))
          nil)))))

(defn block-grow-event [evt]
  (let [newstate (.getNewState evt)]
    (condp = (.getType newstate)
      m/pumpkin
      (when (= 0 (rand-int 2))
        (loc/spawn (.getLocation newstate) Squid)
        (.setCancelled evt true))

      m/melon-block
      (when (= 0 (rand-int 2))
        (loc/spawn (.getLocation newstate) Squid)
        (.setCancelled evt true))

      nil)))

(defn arrow-hit-event [evt entity]
  (let [shooter (.getShooter entity)]
    (condp instance? shooter
      Player
      (do
        (when (when-let [inhand (.getItemInHand shooter)]
                (= m/gold-sword (.getType inhand)))
          (.remove entity))
        (if-let [skill (arrow-skill-of shooter)]
          (skill/arrow-hit skill evt entity)
          (.sendMessage shooter "You don't have a skill yet.")))

      Skeleton
      (when (= 0 (rand-int 2))
        (loc/explode (.getLocation entity) 1 false)
        (.remove entity))

      Cow
      (chimera-cow/arrow-hit evt)

      Blaze
      nil

      (prn 'arrow-hit 'nil-shooter evt entity))))

(defn snowball-hit-event [evt snowball]
  (cond
    (@special-snowball-set snowball)
    (do
      (swap! special-snowball-set disj snowball)
      (comment (let [block (.getBlock (.getLocation snowball))]
                 (when (= m/air (.getType block))
                   (.setType block m/snow)))))
    (instance? Snowman (.getShooter snowball))
    nil

    (nil? (.getShooter snowball))
    nil

    (instance? Player (.getShooter snowball))
    (do
      (let [shooter (.getShooter snowball)]
        (.setFoodLevel shooter (dec (.getFoodLevel shooter))))
      (loc/explode (.getLocation snowball) 0 false)
      (.remove snowball))

    (instance? Creeper (.getShooter snowball))
    (do
      #_(loc/explode (.getLocation snowball) 0 false)
      (.remove snowball))

    :else
    (prn 'snowball-hit-event 'must-not-happen snowball)))

(defn projectile-hit-event [evt]
  (let [entity (.getEntity evt)]
        (condp instance? entity
          #_(Fish (fish-hit-event evt entity))
          Arrow (arrow-hit-event evt entity)
          Snowball (snowball-hit-event evt entity)
          Egg (egg/hit-event evt entity)
          #_(instance? Snowball entity) #_(.strikeLightning (.getWorld entity) (.getLocation entity))
          nil)))

(defn block-dispense-event [evt]
  (when (= m/seeds (.getType (.getItem evt)))
    (let [dispenser (.getBlock evt)
          item (.dropItem (.getWorld dispenser) (.add (.getLocation dispenser) (.multiply (.getVelocity evt) 4)) (.getItem evt))]
      (.playEffect (.getWorld dispenser) (.getLocation dispenser) Effect/MOBSPAWNER_FLAMES nil)
      (.playEffect (.getWorld dispenser) (.getLocation dispenser) Effect/CLICK1 nil)
      (.setVelocity item (.getVelocity evt))
      (future-call #(do
                      (Thread/sleep 1000)
                      (let [soil (.getBlock (.add (.getLocation item) 0 -1 0))
                            air (.getBlock (.getLocation item))]
                        (when (and
                                (= m/soil (.getType soil))
                                (= m/air (.getType air)))
                          (.setType air m/crops)
                          (.remove item))))))
    (.setCancelled evt true)))

(defn player-bed-enter-event [evt]
  (let [player (.getPlayer evt)]
    (c/broadcast (.getDisplayName player) " is sleeping.")
    (future
      (Thread/sleep 3000)
      (later
        (when (.isSleeping player)
          (let [all-players (Bukkit/getOnlinePlayers)
                bed-players (filter (memfn isSleeping) all-players)]
            (when (< (count all-players) (inc (* (count bed-players) 2)))
              (.setHealth player 20)
              (.setTime (.getWorld player) 0)
              (loc/play-sound (.getLocation player) s/cat-hit 0.8 1.0)
              (c/broadcast "good morning everyone!"))))))))

(defn player-bucket-empty-event [evt]
  (later
    (skill/arrow-skillchange (.getPlayer evt) (.getBlock (.add (.getLocation (.getBlockClicked evt)) 0 1 0)) nil)))

(defn player-toggle-sneak-event [evt]
  (let [player (.getPlayer evt)]
    "recovery spa"
    (let [loc (.add (.getLocation player) 0 1 0)]
      (when (= m/stationary-water (.getType (.getBlock loc)))
        (when ( block/blazon? m/stone (.getBlock loc))
          (when (= 0 (rand-int 10))
            (.setType (.getBlock loc) m/air))
          (loc/play-sound (.getLocation player) s/orb-pickup 0.8 1.5)
          (c/broadcast (.getDisplayName player) ": recovery spa!")
          (.setHealth player 20)
          (.setFoodLevel player 20)
          (.teleport player loc)
          (c/add-velocity player 0 0.6 0))))
    (transport/cauldron-teleport player)
    (when-let [vehicle (.getVehicle player)]
      (condp instance? vehicle
        Boat (.setVelocity vehicle (Vector. 0 0 0))
        Minecart nil
        Pig nil
        Player (.leaveVehicle player)
        Enderman (.leaveVehicle player)
        nil))))

(defn just-for-now
  ([] (just-for-now (c/ujm)))
  ([player]
   (def b (.spawn (.getWorld player) (.getLocation player) Boat))
   (.setPassenger b player)
   (.setWorkOnLand b true)
   (.setOccupiedDeceleration b 0.5)
   (.setMaxSpeed b 2.0)))

(defn just-for-now2 []
  (let [chicken (.spawn (.getWorld (c/ujm)) (.getLocation (c/ujm)) Chicken)]
    (.setPassenger (c/ujm) chicken)
    (.setAllowFlight (c/ujm) true)))

(defn just-for-now3 []
  (let [gs (ItemStack. m/gold-sword)]
    (.addEnchantment gs org.bukkit.enchantments.Enchantment/DAMAGE_ALL 5)
    (.setItemInHand (c/ujm) gs)))

(defn just-for-now4 []
  (let [gp (ItemStack. m/gold-pickaxe)]
    (.addEnchantment gp org.bukkit.enchantments.Enchantment/DIG_SPEED 4)
    (.addEnchantment gp org.bukkit.enchantments.Enchantment/DURABILITY 3)
    (.setItemInHand (c/ujm) gp)))

(def just-for-now5-state (ref true))
(defn just-for-now5 []
  (if @just-for-now5-state
    (dosync
      (.sendMessage (c/ujm) "to false")
      (doseq [player (Bukkit/getOnlinePlayers)]
        (.hidePlayer player (c/ujm)))
      (ref-set just-for-now5-state false))
    (dosync
      (.sendMessage (c/ujm) "to true")
      (doseq [player (Bukkit/getOnlinePlayers)]
        (.showPlayer player (c/ujm)))
      (ref-set just-for-now5-state true))))

(defn just-for-now6 []
  (let [items [(ItemStack. m/bow 1) (ItemStack. m/glass 64) (ItemStack. m/dirt 64) (ItemStack. m/torch 64) (ItemStack. m/string 64) (ItemStack. m/diamond-axe 1) (ItemStack. m/diamond-spade 1) (ItemStack. m/water-bucket 1) (ItemStack. m/gold-sword 1) (ItemStack. m/bread 64) (ItemStack. m/diamond-hoe 1) (ItemStack. m/water-bucket 1) (ItemStack. m/blaze-rod 1) (ItemStack. m/stone 64) (ItemStack. m/egg 16) (ItemStack. m/workbench 3) (ItemStack. m/bed 1) (ItemStack. m/redstone 64) (ItemStack. m/glowstone 64) (ItemStack. m/potion 1) (ItemStack. m/potion 1) (ItemStack. m/sand 64) (ItemStack. m/golden-apple 64) (ItemStack. m/iron-ingot 64) (ItemStack. m/arrow 64) (ItemStack. m/coal 64) (ItemStack. m/cobblestone 64) (ItemStack. m/dirt 64) (ItemStack. m/wood 64) (ItemStack. m/log 64) (ItemStack. m/stone-plate 64) (ItemStack. m/milk-bucket 1) (ItemStack. m/cake 1) (ItemStack. m/egg 1) (ItemStack. m/diamond-sword 1) (ItemStack. m/ink-sack 64)]]
    (doseq [i (range 0 (count items))] (.setItem (.getInventory (c/ujm)) i (get items i)))))

(defn vehicle-block-collision-event [evt]
  (let [vehicle (.getVehicle evt)]
    (when-let [passenger (.getPassenger vehicle)]
      (when (instance? Boat vehicle)
        (when (.getWorkOnLand vehicle)
          (let [block (.getBlock evt)]
            (.teleport vehicle (.add (.getLocation vehicle) 0 1 0))))))))

#_(def fire-damage-exempt (atom {}))

(defn vehicle-damage-event [evt]
  (defn near-fire? [loc]
    (not-empty (filter
                 #(= m/fire %)
                 (for [x (range -1 2) z (range -1 2)]
                   (.getType (.getBlock (.add (.clone loc) x 0 z)))))))
  (let [vehicle (.getVehicle evt)]
    (when (and
            (instance? Minecart vehicle)
            (nil? (.getAttacker evt))
            (near-fire? (.getLocation vehicle)))
      (.setCancelled evt true)
      #_(when-let [passenger (.getPassenger vehicle)]
        (when-not (@fire-damage-exempt passenger)
          (swap! fire-damage-exempt assoc passenger true)
          (future
            (loop []
              (Thread/sleep 2000)
              (prn 'tick passenger)
              (if (.isInsideVehicle passenger)
                (recur)
                (swap! fire-damage-exempt assoc false)))))
        (later (.setFireTicks passenger 2000))))))

(defn vehicle-destroy-event [evt]
  (let [vehicle (.getVehicle evt)]
    (when-let [passenger (.getPassenger vehicle)]
      (.setCancelled evt true))))

#_(defn vehicle-entity-collision-event [evt]
  (prn 'vehicle-entity-collision-event))

(defn vehicle-exit-event [evt]
  (let [entity (.getExited evt)
        vehicle (.getVehicle evt)]
    (when (and
            (instance? Player entity)
            (instance? Minecart vehicle))
      (let [center (.add (.getLocation vehicle) 0 -1 0)]
        (when-let [iron-loc (first
                              (for [x (range -1 2)
                                    z (range -1 2)
                                    :let [loc (.add (.clone center) x 0 z)]
                                    :when (= m/iron-block
                                             (.getType (.getBlock loc)))]
                                loc))]
          (future (c/teleport-without-angle entity (.add iron-loc 0 5 0))))))))

#_(defn painting-break-by-entity-event [evt]
  (prn 'pbreak (.getRemover evt) (.getPainting evt))
  (when (instance? Arrow (.getCause evt))
    (.setCancelled evt true)))

(comment (defn enderman-pickup-event* [evt]
  (prn 'epe)))

(comment (defn enderman-pickup-event []
  (c/auto-proxy [Listener] []
                (onEndermanPickup [evt] (enderman-pickup-event* evt)))))

(defn good-bye [klass]
  (count (seq (map #(.remove %)
                   (filter #(instance? klass %)
                           (.getLivingEntities c/world))))))

(def good-bye-creeper (partial good-bye Creeper))

(def pre-stalk (ref nil))

(defn stalk-on [player-name]
  (let [player (Bukkit/getPlayer player-name)]
    (.hidePlayer player (c/ujm))
    (dosync
      (ref-set pre-stalk (.getLocation (c/ujm))))
    (.teleport (c/ujm) (.getLocation player))))

(defn stalk-off [player-name]
  (let [player (Bukkit/getPlayer player-name)]
    (.teleport (c/ujm) @pre-stalk)
    (.showPlayer player (c/ujm))))

(defn block-can-build-event [evt]
  (block/can-build-event evt))

(defn chunk-populate-event [evt]
  #_(let [chk (.getChunk evt)]
    (prn 'chunk (.getX chk) (.getZ chk) (.isLoaded chk))
    (doseq [x (range 0 16) y (range 0 80) z (range 0 16)
            :let [block (.getBlock chk x y z)]]
      (when-let [btype ({m/grass m/dirt
                         m/dirt m/glass
                         m/stone m/air
                         m/coal-ore m/air} (.getType block))]
        (.setType block btype)))))

(defonce swank* nil)
(defn on-enable [plugin]
  (when-not swank*
    (def swank* (swank.swank/start-repl 4005)))
  (c/init-plugin plugin)
  (cloft.recipe/on-enable)
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin #'periodically 50 50)
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin #'cloft-scheduler/on-beat 0 20)
  #_(lingr/say-in-mcujm "cloft plugin running...")
  (future
    (let [ctx (mq/context 1)
          subscriber (mq/socket ctx mq/sub)]
      (mq/bind subscriber "tcp://*:1235")
      (mq/subscribe subscriber "")
      (while true
        (let [contents (read-string (mq/recv-str subscriber))
              players (Bukkit/getOnlinePlayers)]
          #_(prn 'received contents)
          (condp #(.startsWith %2 %1) (:body contents)
            "/list"
            (let [msg (if (empty? players)
                        "(no players)"
                        (clojure.string/join "\n" (map #(player/player-inspect % (= (:body contents) "/list -l")) players)))]
              (lingr/say "computer_science" msg)
              (c/broadcast msg))
            "/chicken"
            (future
              (doseq [p (Bukkit/getOnlinePlayers)]
                (when-not (.isDead p)
                  (loc/spawn (.add (.getLocation p) 0 2 0) Chicken))))
            (doseq [player players]
              (.sendMessage player (format "%s: %s" (:user contents) (:body contents))))))))))
