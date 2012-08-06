(ns cloft.core
  (:require [cloft.cloft :as c])
  (:require [cloft.scheduler :as cloft-scheduler])
  (:require [cloft.chimera-cow :as chimera-cow])
  (:require [cloft.arrow :as arrow])
  (:require [cloft.recipe])
  (:require [cloft.player :as player])
  (:require [cloft.block])
  (:require [cloft.coordinate :as coor])
  (:require [cloft.transport :as transport])
  (:require [swank.swank])
  (:import [org.bukkit Bukkit Material])
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
            EntityDamageEvent$DamageCause])
  (:import [org.bukkit.potion Potion PotionEffect PotionEffectType])
  (:import [org.bukkit.inventory ItemStack])
  (:import [org.bukkit.util Vector])
  (:import [org.bukkit Location Effect])
  (:import [org.bukkit.event.block Action])
  (:require [cloft.zhelpers :as mq]))

(def world (Bukkit/getWorld "world"))

(defn player-super-jump [evt player]
  (let [name (.getDisplayName player)]
    (when (= (.getType (.getItemInHand player)) Material/FEATHER)
      (let [amount (.getAmount (.getItemInHand player))
            x (if (.isSprinting player) (* amount 2) amount)
            x2 (/ (java.lang.Math/log x) 2)]
        (.setFallDistance player 0.0)
        (c/consume-itemstack (.getInventory player) Material/FEATHER)
        (c/add-velocity player 0 x2 0)))))

(defn kaiouken [player]
  (.sendMessage player "界王拳3倍!")
  (.addPotionEffect player (PotionEffect. PotionEffectType/HUNGER 500 10))
  (.addPotionEffect player (PotionEffect. PotionEffectType/FIRE_RESISTANCE 500 3))
  (.addPotionEffect player (PotionEffect. PotionEffectType/INCREASE_DAMAGE 500 1))
  (.addPotionEffect player (PotionEffect. PotionEffectType/DAMAGE_RESISTANCE 500 1))
  (.addPotionEffect player (PotionEffect. PotionEffectType/SPEED 500 1))
  (.addPotionEffect player (PotionEffect. PotionEffectType/JUMP 500 1))
  (.addPotionEffect player (PotionEffect. PotionEffectType/FAST_DIGGING 500 1))
  (.setFireTicks player 500))

(defn food-level-change-event [evt]
  (let [player (.getEntity evt)]
    (when-let [itemstack (.getItemInHand player)]
      (when (= Material/APPLE (.getType itemstack))
        (kaiouken player)))))

(defn entity-combust-event [evt]
  (.setCancelled evt true))

(def bossbattle-player nil)
(defn player-move-event [evt]
  (let [player (.getPlayer evt)]
    #_(when-let [cart (.getVehicle player)]
      (when (instance? Minecart cart)
        (if (#{Material/STONE} (.getType (.getBlock (.getTo evt))))
          (.setDerailedVelocityMod cart (Vector. 0.90 0.90 0.90))
          (if (#{Material/STONE} (.getType (.getBlock (.getFrom evt))))
            (do
              #_(.setCancelled evt true)
              (.setVelocity cart (.multiply (.getVelocity cart) -1)))
            (.setDerailedVelocityMod cart (Vector. 0.1 0.5 0.1))
))))
    (when (and
            (.hasPotionEffect player PotionEffectType/SPEED)
            (.isSprinting player)
            (= 0 (rand-int 2)))
      (.playEffect (.getWorld player) (.add (.getLocation player) 0 -1 0) Effect/MOBSPAWNER_FLAMES nil))
    (comment(let [before-y (.getY (.getFrom evt))
          after-y (.getY (.getTo evt))]
      (when (< (- after-y before-y) 0)
        (let [tmp (.getTo evt)]
          (.setY tmp (+ 1 (.getY (.getTo evt))))
          (prn tmp)
          (.setTo evt tmp)))))
    (comment (let [new-velo (.getDirection (.getLocation player))]
      (.setY new-velo 0)
      (.setVelocity player new-velo)))
    (comment(when (> -0.1 (.getY (.getVelocity player)))
      (.setVelocity player (.setY (.clone (.getVelocity player)) -0.1))))
    #_(let [name (.getDisplayName player)]
        (sanctuary/on-player-move-event [player])
      (when (and (= (.getWorld player) world) (< (.distance (org.bukkit.Location. world 70 66 -58) (.getLocation player)) 1))
        (if (c/jumping? evt)
          (when (not= @bossbattle-player player)
            (c/broadcast player " entered the boss' room!")
            (dosync
              (ref-set bossbattle-player player)))
          (do
            (.sendMessage player "You can't leave")
            (.setTo evt (.add (.getFrom evt) 0 0.5 0))))))
    (comment (when (walking? evt)
      (let [l (.getLocation player)
            b-up (.getBlock l)
            b-down (.getBlock (.add l 0 -1 0))]
        (when (and
                (= (.getType (.getItemInHand player)) Material/RAILS)
                (= (.getType b-up) Material/AIR)
                (contains? #{Material/STONE Material/COBBLESTONE
                             Material/SAND Material/GRAVEL
                             Material/GRASS Material/DIRT}
                           (.getType b-down)))
          (.setType b-up Material/RAILS)
          (c/consume-item player)))))))

(defn block-of-arrow [entity]
  (let [location (.getLocation entity)
        velocity (.getVelocity entity)
        direction (.multiply (.clone velocity) (double (/ 1 (.length velocity))))]
    (.getBlock (.add (.clone location) direction))))

(defn arrow-skill-explosion [entity]
  (.createExplosion (.getWorld entity) (.getLocation entity) 0)
  (let [block (block-of-arrow entity)]
    (.breakNaturally block (ItemStack. Material/DIAMOND_PICKAXE)))
  (.remove entity))

(defn arrow-skill-torch [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.setType (.getBlockAt world location) Material/TORCH)))

(defn arrow-skill-pull [entity]
  (let [block (block-of-arrow entity)]
    (if (c/removable-block? block)
      (let [shooter-loc (.getLocation (.getShooter entity))]
        (.setType (.getBlock shooter-loc) (.getType block))
        (when (= Material/MOB_SPAWNER (.getType block))
          (.setSpawnedType (.getState (.getBlock shooter-loc)) (.getSpawnedType (.getState block))))
        (.setType block Material/AIR)
        (.teleport (.getShooter entity) (.add shooter-loc 0 1 0)))
      (.sendMessage (.getShooter entity) "PULL failed")))
  (.remove entity))

(defn arrow-skill-teleport [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)
        shooter (.getShooter entity)]
    (.setFallDistance shooter 0.0)
    (c/teleport-without-angle shooter location)))

(defn arrow-skill-fire [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (doseq [target (filter
                     #(and (instance? LivingEntity %) (not= (.getShooter entity) %))
                     (.getNearbyEntities entity 1 1 1))]
      (.setFireTicks target 200))))

(defn arrow-skill-flame [entity]
  (doseq [x [-1 0 1] y [-1 0 1] z [-1 0 1]
          :let [block (.getBlock (.add (.clone (.getLocation entity)) x y z))]
          :when (= Material/AIR (.getType block))]
    (.setType block Material/FIRE)))

(defn arrow-skill-tree [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.generateTree world location org.bukkit.TreeType/BIRCH)))

(defn arrow-skill-ore [entity]
  (let [block (block-of-arrow entity)]
    (when (= (.getType block) Material/STONE)
      (let [block-to-choices [Material/COAL_ORE
                              Material/COAL_ORE
                              Material/COBBLESTONE
                              Material/COBBLESTONE
                              Material/GRAVEL
                              Material/IRON_ORE
                              Material/LAPIS_ORE
                              Material/GOLD_ORE
                              Material/REDSTONE_ORE]]
        (.setType block (rand-nth block-to-choices))))))

(defn arrow-skill-shotgun [entity]
  (.remove entity))

(defn arrow-skill-ice [entity]
  (if (.isLiquid (.getBlock (.getLocation entity)))
    (.setType (.getBlock (.getLocation entity)) Material/ICE)
    (let [block (block-of-arrow entity)
          loc (.getLocation block)
          loc-above (.add (.clone loc) 0 1 0)]
      (when
        (and
          (= Material/AIR (.getType (.getBlock loc-above)))
          (not= Material/AIR (.getType (.getBlock loc))))
        (.setType (.getBlock loc-above) Material/SNOW))
      (.dropItem (.getWorld loc-above) loc-above (ItemStack. Material/ARROW))))
  (.remove entity))

(defn arrow-skill-pumpkin [entity]
  (future-call
    #(do
       (Thread/sleep 10)
       (when-not (.isDead entity)
         (let [block (.getBlock (.getLocation entity))]
           (if (and
                   (= 0 (rand-int 3))
                   (= Material/AIR (.getType block)))
             (do
               (.setType block (rand-nth [Material/PUMPKIN Material/JACK_O_LANTERN]))
               (.remove entity))
             (.sendMessage (.getShooter entity) "PUMPKIN failed")))))))

(defn arrow-skill-plant [entity]
  (let [inventory (.getInventory (.getShooter entity))]
    (.remove entity)
    (doseq [x (range -3 4) z (range -3 4)]
      (let [loc (.add (.getLocation entity) x 0 z)]
        (when (and (.contains inventory Material/SEEDS)
                   (= Material/AIR (.getType (.getBlock loc)))
                   (= Material/SOIL (.getType (.getBlock (.add (.clone loc) 0 -1 0)))))
          (c/consume-itemstack inventory Material/SEEDS)
          (c/consume-itemstack inventory Material/SEEDS)
          (.setType (.getBlock loc) Material/CROPS))))))

(defn arrow-skill-diamond [entity]
  (let [block (.getBlock (.getLocation entity))]
    (condp = (.getType block)
      Material/CROPS
      (.setData block 7)
      nil))
  (let [block (block-of-arrow entity)]
    (condp = (.getType block)
      Material/COBBLESTONE
      (.setType block Material/STONE)
      Material/WOOL
      (do
        (.setType block Material/AIR)
        (if (= 0 (rand-int 2))
          (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/WOOL))
          (.spawn (.getWorld entity) (.getLocation entity) Sheep)))
      nil))
  (.remove entity)
  (let [loc (.getLocation entity)]
    (.dropItem (.getWorld loc) loc (ItemStack. Material/ARROW))))

(defn something-like-quake [entity klass f]
  (let [targets (.getNearbyEntities entity 5 3 5)]
    (future-call
      #(do
         (doseq [_ [1 2 3]]
           (doseq [target targets :when (instance? klass target)]
             (Thread/sleep (rand-int 300))
             (.playEffect (.getWorld target) (.getLocation target) Effect/ZOMBIE_CHEW_WOODEN_DOOR nil)
             (c/add-velocity target (- (rand) 0.5) 0.9 (- (rand) 0.5))
             (f target))
           (Thread/sleep 1500))
         (.remove entity)))))

(defn arrow-skill-quake [entity]
  (something-like-quake
    entity
    LivingEntity
    (fn [_] nil)))

(defn arrow-skill-popcorn [entity]
  (something-like-quake
    entity
    Item
    (fn [item]
      (when (= 0 (rand-int 5))
        (.dropItem (.getWorld item) (.getLocation item) (.getItemStack item)))
      (when (= 0 (rand-int 5))
        (.remove item)))))

(defn arrow-skill-liquid [material duration entity]
  (let [block (.getBlock (.getLocation entity))]
    (if (= Material/AIR (.getType block))
      (do
        (.setType block material)
        (future-call #(do
                        (Thread/sleep duration)
                        (when (.isLiquid block)
                          (.setType block Material/AIR)))))
      (.sendMessage (.getShooter entity) "failed")))
  (future-call #(do
                  (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/ARROW))
                  (.remove entity))))

(defn arrow-skill-water [entity]
  (arrow-skill-liquid Material/WATER 5000 entity))

(defn arrow-skill-lava [entity]
  (arrow-skill-liquid Material/LAVA 500 entity))

(defn arrow-skill-woodbreak [entity]
  (let [block (block-of-arrow entity)
        table {Material/WOODEN_DOOR (repeat 6 (ItemStack. Material/WOOD))
               Material/FENCE (repeat 6 (ItemStack. Material/STICK))
               Material/WALL_SIGN (cons (ItemStack. Material/STICK)
                                        (repeat 6 (ItemStack. Material/WOOD)))
               Material/SIGN_POST (cons (ItemStack. Material/STICK)
                                        (repeat 6 (ItemStack. Material/WOOD)))}
        items (table (.getType block))
        block2 (.getBlock (.getLocation entity))
        items2 (table (.getType block2))]
    (if items
      (do
        (.setType block Material/AIR)
        (doseq [item items]
          (.dropItemNaturally (.getWorld block) (.getLocation block) item)))
      (if items2
        (do
          (.setType block2 Material/AIR)
          (doseq [item items2]
            (.dropItemNaturally (.getWorld block2) (.getLocation block2) item)))
        (.sendMessage (.getShooter entity) "Woodbreak failed."))))
  (.remove entity))

(def arrow-skill (atom {}))
(defn arrow-skill-of [player]
  (get @arrow-skill (.getDisplayName player)))

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

(def bowgun-players (atom #{"ujm"}))
(defn add-bowgun-player [name]
  (swap! bowgun-players conj name))

(defn arrow-velocity-vertical? [arrow]
  (let [v (.getVelocity arrow)]
    ;(prn 'arrow-velocity-vertical? v)
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

(defn fly-with-check [projectile fn]
  (cloft-scheduler/settimer
    1
    #(when (fn projectile)
       (fly-with-check projectile fn))))

(defn blaze2-launch-fireball [blaze2 projectile]
  (when (not= "world" (.getName (.getWorld blaze2))) (prn 'omg-assertion-failed))
  (.remove projectile)
  (if (= 0 (rand-int 5))
    (let [loc (.getLocation blaze2)]
      (.playEffect (.getWorld loc) loc Effect/MOBSPAWNER_FLAMES nil)
      (.spawn (.getWorld blaze2)
              (.add loc 0 2 0)
              (rand-nth [Skeleton Zombie Spider MagmaCube MagmaCube Silverfish
                         Enderman Villager Creeper])))
    (.launchProjectile blaze2 Arrow)))

(defn blaze2-arrow-hit [target]
  (.setFireTicks target 200))

(defn blaze2-get-damaged [evt blaze2]
  (when (not= "world" (.getName (.getWorld blaze2))) (prn 'omg-assertion-failed))
  (.setDamage evt (max (int (/ (.getDamage evt) 2)) 9)))

(defn blaze2-murder-event [evt blaze2 player]
  (when (not= "world" (.getName (.getWorld blaze2))) (prn 'omg-assertion-failed))
  (doseq [[x y z] [[0 0 0] [-1 0 0] [0 -1 0] [0 0 -1] [1 0 0] [0 1 0] [0 0 1]]]
    (let [loc (.getLocation blaze2)]
      (when (and
              (= Material/AIR (.getType (.getBlock loc)))
              (not= 0 (rand-int 3)))
        (.setType
          (.getBlock (.add (.clone loc) x y z))
          (rand-nth [Material/NETHER_BRICK Material/NETHERRACK Material/SOUL_SAND
                     Material/GLOWSTONE Material/GLOWSTONE Material/GLOWSTONE])))))
  (.setDroppedExp evt 200)
  (c/broadcast (format "%s beated a blaze2!" (.getDisplayName player)))
  (c/lingr (format "%s beated a blaze2!" (.getDisplayName player))))


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
    (when (and
            (= org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason/NATURAL (.getSpawnReason evt))
            (= 0 (rand-int 10))
            (= "world" (.getName (.getWorld creature)))
            (some #(instance? % creature) [Zombie Skeleton]))
      (.spawn (.getWorld creature) (.getLocation creature) Blaze)
      (.setCancelled evt true))))

(defn item-spawn-event [evt]
  (let [item (.getEntity evt)
        table {Material/RAW_BEEF [Material/ROTTEN_FLESH Material/COOKED_BEEF]
               Material/RAW_CHICKEN [Material/ROTTEN_FLESH Material/COOKED_CHICKEN]
               Material/RAW_FISH [Material/RAW_FISH Material/COOKED_FISH]
               Material/PORK [Material/ROTTEN_FLESH Material/GRILLED_PORK]
               #_(Material/APPLE [Material/APPLE Material/GOLDEN_APPLE])
               Material/ROTTEN_FLESH [Material/ROTTEN_FLESH Material/COAL]}
        itemstack (.getItemStack item)]
    (when (table (.getType itemstack))
      (future-call #(let [pair (table (.getType itemstack))]
                      (Thread/sleep 5000)
                      (when-not (.isDead item)
                        (if (#{Material/FURNACE Material/BURNING_FURNACE}
                                      (.getType (.getBlock (.add (.getLocation item) 0 -1 0))))
                          (do
                            (.dropItem (.getWorld item) (.getLocation item) (ItemStack. (last pair) (.getAmount itemstack)))
                            (.remove item))
                          (do
                            (Thread/sleep 25000)
                            (when-not (.isDead item)
                              (.dropItem (.getWorld item) (.getLocation item) (ItemStack. (first pair) (.getAmount itemstack)))
                              (.remove item))))))))))

(defn entity-shoot-bow-event [evt]
  (let [shooter (.getEntity evt)]
    (when (instance? Player shooter)
      (when (or (.isSneaking shooter)
                (= 'strong (arrow-skill-of shooter)))
        (.setVelocity (.getProjectile evt) (.multiply (.getVelocity (.getProjectile evt)) 3)))
      (comment (.setCancelled evt true))
      (comment (.setVelocity shooter (.multiply (.getVelocity (.getProjectile evt)) 2)))
      (comment (when (and
              (get @bowgun-players (.getDisplayName shooter))
              (not= arrow-skill-teleport (arrow-skill-of shooter)))
        (future-call #(do
                        (Thread/sleep 100) (.shootArrow (.getEntity evt))
                        (Thread/sleep 300) (.shootArrow (.getEntity evt))
                        (Thread/sleep 500) (.shootArrow (.getEntity evt))))))
      (when (= 'sniping (arrow-skill-of shooter))
        (let [arrow (.getProjectile evt)
              direction (.getDirection (.getLocation shooter))]
         (prn
          "shooter location " (.getLocation shooter)
          "shooter direction " direction
          "arrow loc: "(.getLocation arrow)
          "arrow v: "(.getVelocity arrow)
          "arrow v/|v|: " (.multiply (.getVelocity arrow) (/ 1 (.length (.getVelocity arrow)))))))
      (when (arrow-velocity-vertical? (.getProjectile evt))
        (prn last-vertical-shots)
        (swap! last-vertical-shots assoc (.getDisplayName shooter) (.getLocation shooter))
        (prn last-vertical-shots)
        (future-call #(let [shooter-name (.getDisplayName shooter)]
                        (check-and-thunder shooter)
                        (Thread/sleep 1000)
                        (swap! last-vertical-shots dissoc shooter-name))))
      #_(when (= 'arrow-skill-tntmissile (arrow-skill-of shooter))
        (let [inventory (.getInventory shooter)
              arrow (.getProjectile evt)]
          (if (.contains inventory Material/TNT)
            (fly-with-check
              arrow
              #(let [velocity (.getVelocity %1)
                     location (.getLocation %1)]
                 (if (> (.getY (.toVector location))
                          (+ 5.0 (.getY (.toVector (.getLocation shooter)))))
                   (if (.contains inventory Material/TNT)
                     (let [primed-tnt (.spawn world location TNTPrimed) ]
                       (.setVelocity primed-tnt velocity)
                       (c/consume-itemstack inventory Material/TNT)
                       (.remove arrow)
                       false)
                     ((c/broadcast (.getDisplayName shooter) " has no TNT.")
                     false))
                   true)))
            (c/broadcast (.getDisplayName shooter) " has no TNT."))))
      (when (= arrow-skill-shotgun (arrow-skill-of shooter))
        (doseq [_ (range 1 80)]
          (let [rand1 (fn [] (* 0.8 (- (rand) 0.5)))
                arrow (.launchProjectile shooter Arrow)]
            (.setVelocity arrow (.getVelocity (.getProjectile evt)))
            (c/add-velocity arrow (rand1) (rand1) (rand1))))))))

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

(defn freeze-for-20-sec [target]
  (when-not (.isDead target)
    (let [loc (.getLocation (.getBlock (.getLocation target)))]
      (doseq [y [0 1]]
        (doseq [[x z] [[-1 0] [1 0] [0 -1] [0 1]]]
          (let [block (.getBlock (.add (.clone loc) x y z))]
            (when (#{Material/AIR Material/SNOW} (.getType block))
              (.setType block Material/GLASS)))))
      (doseq [y [-1 2]]
        (let [block (.getBlock (.add (.clone loc) 0 y 0))]
          (when (#{Material/AIR Material/SNOW} (.getType block))
            (.setType block Material/GLASS))))
      (future-call #(do
                      (Thread/sleep 1000)
                      (when-not (.isDead target)
                        (.teleport target (.add (.clone loc) 0.5 0.0 0.5)))))
      (future-call #(do
                      (Thread/sleep 20000)
                      (doseq [y [0 1]]
                        (doseq [[x z] [[-1 0] [1 0] [0 -1] [0 1]]]
                          (let [block (.getBlock (.add (.clone loc) x y z))]
                            (when (= (.getType block) Material/GLASS)
                              (.setType block Material/AIR)))))
                      (doseq [y [-1 2]]
                        (let [block (.getBlock (.add (.clone loc) 0 y 0))]
                          (when (= (.getType block) Material/GLASS)
                            (.setType block Material/AIR)))))))))

(defn reaction-skill-ice [you by]
  (freeze-for-20-sec by)
  (c/lingr (str "counter attack with ice by " (.getDisplayName you) " to " (c/entity2name by))))

(defn reaction-skill-knockback [you by]
  (let [direction (.multiply (.normalize (.toVector (.subtract (.getLocation by) (.getLocation you)))) 2)]
    (c/add-velocity by (.getX direction) (.getY direction) (.getZ direction))))

(defn reaction-skill-fire [you by]
  (.setFireTicks by 100))

(defn reaction-skill-golem [you by]
  (let [golem (.spawn (.getWorld by) (.getLocation by) IronGolem)]
    (.setTarget golem by)
    (future-call #(do
                    (Thread/sleep 10000)
                    (.remove golem))))
  (.sendMessage you "a golem helps you!"))

(defn reaction-skill-wolf [you by]
  (let [wolf (.spawn (.getWorld by) (.getLocation by) Wolf)]
    (.setTamed wolf true)
    (.setOwner wolf you)
    (.setTarget wolf by)
    (future-call #(do
                    (Thread/sleep 10000)
                    (.remove wolf))))
  (.sendMessage you "a wolf helps you!"))

(defn reaction-skill-teleport [you by]
  (letfn [(find-place [from range]
            (let [candidates
                  (for [x range y range z range :when (> y 5)]
                    (.add (.clone (.getLocation from)) x y z))
                  good-candidates
                  (filter
                    #(and
                       (not= Material/AIR
                             (.getType (.getBlock (.add (.clone %) 0 -1 0))))
                       (= Material/AIR (.getType (.getBlock %)))
                       (= Material/AIR
                          (.getType (.getBlock (.add (.clone %) 0 1 0)))))
                    candidates)]
              (rand-nth good-candidates)))]
    (.sendMessage you
      (str "You got damage by " (c/entity2name by) " and escaped."))
    (.teleport you (find-place you (range -10 10)))))

(defn reaction-skill-poison [you by]
  (.addPotionEffect by (PotionEffect. PotionEffectType/POISON 200 2)))


(defn blazon? [block-type block-against]
  (and (every? #(= % block-type)
               (map #(.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                    [0 0 -1 1] [-1 1 0 0]))
       (every? #(not= % block-type)
               (map #(.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                    [-1 1 0 0] [-1 1 0 0]))))

(defn reaction-skillchange [player block block-against]
  (when (blazon? Material/LOG block-against)
    (let [table {Material/RED_ROSE [reaction-skill-fire "FIRE"]
                 Material/YELLOW_FLOWER [reaction-skill-teleport "TELEPORT"]
                 Material/COBBLESTONE [reaction-skill-knockback "KNOCKBACK"]
                 Material/DIRT [reaction-skill-wolf "WOLF"]
                 Material/IRON_BLOCK [reaction-skill-golem "GOLEM"]
                 Material/SNOW_BLOCK [reaction-skill-ice "ICE"]
                 Material/RED_MUSHROOM [reaction-skill-poison "POISON"]}]
      (when-let [skill-name (table (.getType block))]
        (if (= 0 (.getLevel player))
          (.sendMessage player "Your level is 0. You can't set reaction skill yet.")
          (let [l (.getLevel player)]
            (.playEffect (.getWorld block) (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
            (c/broadcast (.getDisplayName player) " changed reaction-skill to " (last skill-name))
            (.sendMessage player (format "You can use the reaction skill for %d times" l))
            (swap! reaction-skill assoc (.getDisplayName player) [(first skill-name) l])))))))

(defn arrow-skillchange [player block block-against]
  (when (blazon? Material/STONE (.getBlock (.add (.getLocation block) 0 -1 0)))
    (let [table {Material/GLOWSTONE ['strong "STRONG"]
                 Material/TNT [arrow-skill-explosion "EXPLOSION"]
                 Material/TORCH [arrow-skill-torch "TORCH"]
                 Material/REDSTONE_TORCH_ON [arrow-skill-pull "PULL"]
                 Material/YELLOW_FLOWER [arrow-skill-teleport "TELEPORT"]
                 Material/RED_ROSE [arrow-skill-fire "FIRE"]
                 Material/SAPLING [arrow-skill-tree "TREE"]
                 Material/WORKBENCH [arrow-skill-ore "ORE"]
                 Material/TRAP_DOOR ['digg "DIGG"]
                 Material/LADDER ['trap "TRAP"]
                 Material/CACTUS [arrow-skill-shotgun "SHOTGUN"]
                 Material/RAILS ['cart "CART"]
                 Material/BOOKSHELF ['mobchange "MOBCHANGE"]
                 Material/SANDSTONE ['arrow-skill-tntmissile "TNTMissle"]
                 #_( Material/STONE ['sniping "SNIPING"])
                 Material/SNOW_BLOCK [arrow-skill-ice "ICE"]
                 Material/POWERED_RAIL ['exp "EXP"]
                 Material/PISTON_BASE ['super-knockback "SUPER-KNOCKBACK"]
                 Material/JACK_O_LANTERN [arrow-skill-pumpkin "PUMPKIN"]
                 Material/PUMPKIN [arrow-skill-pumpkin "PUMPKIN"]
                 Material/CROPS [arrow-skill-plant "PLANT"]
                 Material/DIAMOND_BLOCK [arrow-skill-diamond "CRAZY DIAMOND"]
                 #_( Material/FIRE [arrow-skill-flame "FLAME"])
                 Material/BROWN_MUSHROOM [arrow-skill-quake "QUAKE"]
                 Material/RED_MUSHROOM ['arrow-skill-poison "POISON"]
                 Material/FENCE_GATE [arrow-skill-popcorn "POPCORN"]
                 Material/WATER [arrow-skill-water "WATER"]
                 Material/LAVA [arrow-skill-lava "LAVA"]
                 Material/LOG [arrow-skill-woodbreak "WOODBREAK"]}]
      (when-let [skill-name (table (.getType block))]
        (.playEffect (.getWorld block) (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
        (c/broadcast (.getDisplayName player) " changed arrow-skill to " (last skill-name))
        (swap! arrow-skill assoc (.getDisplayName player) (first skill-name))))))

(defn pickaxe-skillchange [player block block-against]
  (when (blazon? Material/IRON_ORE (.getBlock (.add (.getLocation block) 0 -1 0)))
    (let [table {Material/YELLOW_FLOWER ['pickaxe-skill-teleport "TELEPORT"]
                 Material/RED_ROSE ['pickaxe-skill-fire "FIRE"]
                 Material/WORKBENCH ['pickaxe-skill-ore "ORE"]
                 Material/STONE ['pickaxe-skill-stone "STONE"]}]
      (when-let [skill-name (table (.getType block))]
        (.playEffect (.getWorld block) (.getLocation block) Effect/MOBSPAWNER_FLAMES nil)
        (c/broadcast (.getDisplayName player) " changed pickaxe-skill to " (last skill-name))
        (swap! pickaxe-skill assoc (.getDisplayName player) (first skill-name))))))

(def max-altitude 255)



(defn summon-x
  ([pos world creature] (summon-x pos world creature 1))
  ([pos world creature after]
   (cloft-scheduler/settimer after #(.spawn world (.toLocation pos world) creature))))

(defn summon-giant [player block]
  (.damage player (/ (.getHealth player) 2))
  (.setFoodLevel player 0)
  (let [world (.getWorld player)
        spawn-at  (coor/local-to-world player block 10.0 0.0 0.0)]
    (.strikeLightningEffect world (.toLocation spawn-at world))
    (summon-x spawn-at world Giant)
    (c/broadcast (.getDisplayName player) " has summoned a Giant!")))

(defn summon-residents-of-nether [player block]
  (let [world (.getWorld player)
        loc (.toVector (.getLocation player))
        pos1 (coor/local-to-world player block 15.0 1.0 -5.0)
        pos2 (coor/local-to-world player block 15.0 1.0 0.0)
        pos3 (coor/local-to-world player block 15.0 1.0 5.0)
        place-fire (fn [v i]
                      (cloft-scheduler/settimer
                        (* 4 i)
                        #(when (= Material/AIR (.getType v))
                           (.playEffect (.getWorld v) (.getLocation v) Effect/BLAZE_SHOOT nil)
                           (.setType v Material/FIRE))))]
    (letfn [(explode-at ([pos world delay]
               (cloft-scheduler/settimer 1  #(when-not (.createExplosion world (.toLocation pos world) 0.0 true)
                                               (explode-at pos world 1)))))
            (summon-set-of-evils-at [pos loc world]
              (cloft-scheduler/settimer
                1
                #(do
                   (cloft.block/place-in-line world (.clone loc) (.clone pos) place-fire 2)
                   (explode-at (.clone pos) world 60)
                   (summon-x pos world Blaze 65)
                   (summon-x loc world PigZombie 65)
                   (let [ghast-pos (.add (.clone pos) (Vector. 0.0 7.0 0.0))]
                     (explode-at ghast-pos world 1)
                     (summon-x ghast-pos world Ghast 65)))))]
            (summon-set-of-evils-at pos1 loc world)
            (summon-set-of-evils-at pos2 loc world)
            (summon-set-of-evils-at pos3 loc world)
            (summon-x (coor/local-to-world player block -5.0 0.5 0.0) world Creeper 80)
            (c/broadcast (.getDisplayName player) " has summoned Blazes, PigZombies and Ghasts!"))))

(def active-fusion-wall(atom {}))
(defn active-fusion-wall-of[player]
  (get @active-fusion-wall (.getDisplayName player)))

(defn alchemy-fusion-wall [player block]
  (let [world (.getWorld player)
        loc (.toVector (.getLocation player))
        bottom (coor/local-to-world player block 15.0 0.0 0.0)
        top (coor/local-to-world player block 15.0 6.0 0.0)]
    (letfn [(place-cobblestones [v i]
              (cloft-scheduler/settimer (* 4 i)
                                       #(when (= Material/AIR (.getType v))
                                          (.setType v Material/COBBLESTONE))))]
      (.strikeLightningEffect world (.toLocation bottom world))
      (cloft.block/place-in-line world bottom top place-cobblestones)
      (if-let [prev (active-fusion-wall-of player)]
        (let [[eb et] prev]
          (cloft.block/place-in-line world eb bottom place-cobblestones)
          (cloft.block/place-in-line world et top place-cobblestones))
        (prn "nothing to connect."))
      (swap! active-fusion-wall assoc (.getDisplayName player) [bottom top]))))


(defn fusion-floor [player block]
  (let [world (.getWorld player)
        start-left (coor/local-to-world player block 0.0 0.0 -1.0)
        start-center (.toVector (.getLocation player))
        start-right (coor/local-to-world player block 0.0 0.0 1.0)
        distance (min (+ 10.0 (* 2 (.getLevel player))) 60.0)
        end-left (coor/local-to-world player block distance 0.0 -1.0)
        end-center (coor/local-to-world player block distance 0.0 0.0)
        end-right (coor/local-to-world player block distance 0.0 1.0)
        block-floor (fn [v i]
                      (cloft-scheduler/settimer
                        (* 4 i)
                        #(when (boolean ((cloft.block/category :enterable) (.getType v)))
                           (when (= 0 (rand-int 6))
                             (.strikeLightningEffect world (.getLocation v)))
                           (.setType v Material/COBBLESTONE))))]
    (cloft.block/place-in-line world start-left end-left block-floor 2)
    (cloft.block/place-in-line world start-center end-center block-floor 2)
    (cloft.block/place-in-line world start-right end-right block-floor 2)))

(defn make-redstone-for-livings [player block]
  (let [world (.getWorld player)]
    (doseq [e (filter #(instance? LivingEntity %) (.getNearbyEntities player 10 10 10))]
           (let [loc (.getLocation e)]
             (.remove e)
             (.strikeLightningEffect world loc)
             (.dropItem world loc (ItemStack. Material/REDSTONE))))))

(defn erupt-volcano [player block]
  (let [world (.getWorld player)
        crator-vector (coor/local-to-world player block 40.0 20.0 0.0)
        crator-location (.toLocation crator-vector world)]
    (.strikeLightningEffect world crator-location)
    (.setType (.getBlockAt world crator-location) Material/LAVA)
    (cloft.block/place-in-circle
      world 10 14
      crator-location
      (fn [v i]
          (.setType v Material/COBBLESTONE)))))

(defn close-air-support [player block]
  (let [world (.getWorld player)
        xz (coor/local-to-world player block 0.0 0.0 0.0)
        center-vector (.setY  (.clone xz) max-altitude)
        center-location (.toLocation center-vector world)]
    (doseq [v (cloft.block/blocks-in-radiaus-xz world center-location 20 70)]
      (when (= (rand-int 30) 1)
        (cloft-scheduler/settimer
          (rand-int 300)
          #(let [tnt (.spawn world (.getLocation v) TNTPrimed)
                 uy (Vector. 0.0 -10.0 0.0)
                 y (.multiply uy (rand))]
             (.setVelocity tnt y)))))))

(defn earthen-pipe [player block]
  (let [world (.getWorld player)
        center-vector (coor/local-to-world player block 10.0 0.0 0.0)
        center-location (.toLocation center-vector world)
        uy (Vector. 0 1 0)]
    (loop [h 0 inner 5.0 outer 7.0]
      (cloft.block/place-in-circle
        world inner outer
        (.toLocation (.add (.clone center-vector) (.multiply (.clone uy) h)) world)
        (fn [v i]
          (.setType v Material/WOOL)
          (.setData v (Byte. (byte 5)))))
      (if (< h 20)
        (recur (inc h) inner outer)
        (when (< h 24)
          "making lip"
          (recur (inc h) inner 9))))))

(defn invoke-alchemy [player block block-against]
  (when (blazon? Material/NETHERRACK block-against)
    "MEMO: to be changed to STONE BRICK"
    "TODO: consistant naming"
    (let [table {Material/COBBLESTONE alchemy-fusion-wall
                 Material/SAND fusion-floor
                 Material/DIRT summon-giant
                 Material/LOG make-redstone-for-livings
                 Material/GLOWSTONE summon-residents-of-nether}
          table2 {Material/TNT close-air-support
                  Material/NETHERRACK erupt-volcano
                  Material/RED_MUSHROOM earthen-pipe}]
      (if-let [alchemy (table (.getType block))]
        (alchemy player block)
        (prn "no effect is defined for " block)))))


(defn block-damage-event [evt]
  (let [player (.getPlayer evt)]
    (when (and
            (c/pickaxes (.getType (.getItemInHand player)))
            (= 'pickaxe-skill-stone (pickaxe-skill-of player)))
      (if (= Material/STONE (.getType (.getBlock evt)))
        (.setInstaBreak evt true)
        (when (not= 0 (rand-int 1000))
          (.setCancelled evt true))))))

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
          (doseq [diff (range 1 (- (xyz-getter block2) (xyz-getter block1)))]
            (when (< diff 200)
              (let [b (.getBlock (.add (.clone (.getLocation block1))
                                       (.multiply (.clone unit-vec) diff)))]
                (when ((cloft.block/category :enterable) (.getType b))
                  (.setType b (.getType another-block))
                  (.setData b (.getData another-block)))))))
        (.sendMessage another-player "ok (second)")
        (.sendMessage player "ok (first)"))
      (do
        (future-call #(do
                        (Thread/sleep 3000)
                        (swap! player-block-placed dissoc block)))
        (swap! player-block-placed assoc block player)))
    (arrow-skillchange player block block-against)
    (pickaxe-skillchange player block block-against)
    (reaction-skillchange player block block-against)
    (invoke-alchemy player block block-against)
    #_(transport/teleport-machine player block block-against)))

(defn player-login-event [evt]
  (let [player (.getPlayer evt)]
    (comment (when (= (.getDisplayName player) "Player")
      (.setDisplayName player "raa0121")))
    (future (Thread/sleep 1000)
      (let [ip (.. player getAddress getAddress getHostAddress)]
        (.setOp player (or
                         (.startsWith ip "10.0")
                         (= "113.151.154.229" ip)
                         (= "0:0:0:0:0:0:0:1" ip))))
      (.playEffect (.getWorld player) (.getLocation player) Effect/RECORD_PLAY (rand-nth c/records))
      #_(.sendMessage player "[TIPS] 川で砂金をとろう! クワと皿を忘れずに。")
      #_(.sendMessage player "[TIPS] りんごを食べて界王拳!")
      (.sendMessage player "[NEWS] 鶏右クリックドロップアイテム変わりました")
      (.sendMessage player "[NEWS] 金の剣のビームや矢は左クリックになりました")
      (.sendMessage player "[NEWS] 糸で何か乗せてるときは、糸なくても右クリックで降ろせます")
      #_(when (= "mozukusoba" (.getDisplayName player))
        (.teleport player (.getLocation (c/ujm)))))
    (c/lingr (str (player/name2icon (.getDisplayName player)) "logged in now."))))

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
                     (when (= Material/AIR (.getType (.getBlock loc)))
                       (.spawn (.getWorld loc) loc Creeper)))))))]
    ((rand-nth [unlucky]) player)))

(defn async-player-chat-event [evt]
  (let [player (.getPlayer evt)
        name (.getDisplayName player)
        msg (.getMessage evt)]
    (cond
      (= 1 (count msg)) nil
      (= "countdown" msg) (do
                            (.setCancelled evt true)
                            (future-call #(do
                                            (c/broadcast name ": " 3 " " (.getType (.getItemInHand player)))
                                            (Thread/sleep 1000)
                                            (c/broadcast 2)
                                            (Thread/sleep 1000)
                                            (c/broadcast 1))))
      :else (c/lingr "computer_science" (str (player/name2icon name) msg)))))

(defn touch-player [target]
  (.setFoodLevel target (dec (.getFoodLevel target)))
  (.setGameMode target org.bukkit.GameMode/SURVIVAL))

(defn entity-interact-physical-event [evt entity]
  (transport/teleport-up entity (.getBlock evt)))

(defn entity-interact-event [evt]
  (let [entity (.getEntity evt)]
    (entity-interact-physical-event evt entity)))

(def special-snowball-set (atom #{}))

(comment (defn elevator [player door]
  (let [loc (.getLocation (.getBlock (.getLocation player)))]
    (when (and
            (every?
              #(= Material/COBBLESTONE %)
              (for [x [-1 0 1] z [-1 0 1]]
                (.getType (.getBlock (.add (.clone loc) x -1 z)))))
            (every?
              #(= Material/COBBLESTONE %)
              (for [x [-1 0 1] z [-1 0 1]]
                (.getType (.getBlock (.add (.clone loc) x 2 z))))))
      (.teleport player (.add (.getLocation player) 0 10 0))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x -1 z)) Material/AIR))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x 2 z)) Material/AIR))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x 9 z)) Material/COBBLESTONE))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x 12 z)) Material/COBBLESTONE))
      (.setType (.getBlock (.getLocation door)) Material/AIR)
      (.setType (.getBlock (.add (.getLocation door) 0 10 0)) Material/IRON_DOOR)))))


(def plowed-sands (atom #{}))

(def hoe-durabilities
  {Material/WOOD_HOE 60
   Material/STONE_HOE 132
   Material/IRON_HOE 251
   Material/GOLD_HOE 33
   Material/DIAMOND_HOE 1562})

(defn minecart-accelerate [cart]
  (let [dire (.getDirection (.getLocation cart))]
    (let [x (- (* (.getX dire) 0.707) (* (.getZ dire) 0.707))
          z (+ (* (.getX dire) 0.707) (* (.getZ dire) 0.707))]
      (.setVelocity cart (Vector. (* (.getZ dire) -2) 0.0 (* (.getX dire) 2))))))

(defn player-left-click-event [evt player]
  (cond
    (instance? Minecart (.getVehicle player))
    (do
      (.setCancelled evt true)
      (minecart-accelerate (.getVehicle player)))

    (and
      (= (.. player (getItemInHand) (getType)) Material/GOLD_SWORD)
      (= (.getHealth player) (.getMaxHealth player)))
    (if (empty? (.getEnchantments (.getItemInHand player)))
      (let [snowball (.launchProjectile player Snowball)]
        (swap! special-snowball-set conj snowball)
        (.setVelocity snowball (.multiply (.getVelocity snowball) 3)))
      (let [arrow (.launchProjectile player Arrow)]
        (.setVelocity arrow (.multiply (.getVelocity arrow) 3))))))

(defn player-right-click-event [evt player]
  (defn else []
    """just for DRY"""
    (cond
      (and
        (player/zombie? player)
        (= (.. evt (getMaterial)) Material/MILK_BUCKET))
      (do
        (player/rebirth-from-zombie player)
        (when (= 0 (rand-int 3))
          (.setType (.getItemInHand player) Material/BUCKET)))

      (and
        (.getAllowFlight player)
        (= (.. evt (getMaterial)) Material/COAL))
      (do
        (.setVelocity player (.multiply (.getDirection (.getLocation player)) 3))
        (c/consume-item player))

      (= (.. evt (getMaterial)) Material/FEATHER)
      (player-super-jump evt player)))
  (if-let [block (.getClickedBlock evt)]
    (cond
      (= Material/BLAZE_ROD (.. player (getItemInHand) (getType)))
      (do
        (.sendMessage player (format "%s: %1.3f" (.getType block) (.getTemperature block)))
        (.sendMessage player (format "biome: %s" (.getBiome block))))

      (= Material/CAKE_BLOCK (.getType block))
      (if-let [death-point (player/death-location-of player)]
        (do
          (.load (.getChunk death-point))
          (c/broadcast (str (.getDisplayName player) " is teleporting to the last death place..."))
          (.teleport player death-point))
        (.sendMessage player "You didn't die yet."))

      (and
        (= 0 (rand-int 15))
        (= Material/BOWL (.getType (.getItemInHand player)))
        (@plowed-sands block))
      (let [item-type (if (= 0 (rand-int 50)) Material/GOLD_INGOT Material/GOLD_NUGGET)]
        (.dropItemNaturally (.getWorld block) (.getLocation block) (ItemStack. item-type)))

      (and
        (hoe-durabilities (.. player (getItemInHand) (getType)))
        (and (= org.bukkit.block.Biome/RIVER (.getBiome block))
             (not (@plowed-sands block))
             (= Material/SAND (.getType block))
             (> 64.0 (.getY (.getLocation block)))
             (.isLiquid (.getBlock (.add (.getLocation block) 0 1 0)))))
      (let [item (.getItemInHand player)]
        (.playEffect (.getWorld block) (.getLocation block) Effect/STEP_SOUND Material/TORCH)
        (if (> (.getDurability item) (hoe-durabilities (.getType item)))
          (.remove (.getInventory player) item)
          (do
            (.setDurability item (+ 2 (.getDurability item)))
            (future-call #(do
                            (swap! plowed-sands conj block)
                            (Thread/sleep (+ 1000 (* 5 (hoe-durabilities (.getType item)))))
                            (swap! plowed-sands disj block)
                            (when (= Material/SAND (.getType block))
                              (.playEffect (.getWorld block) (.getLocation block) Effect/STEP_SOUND Material/SAND)
                              (when (= 0 (rand-int 2))
                                (.setType block (rand-nth [Material/SANDSTONE Material/AIR Material/CLAY])))))))))
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
        {Material/WOOD_SWORD [Material/STICK Material/WOOD Material/WOOD]
         Material/STONE_SWORD [Material/STICK Material/COBBLESTONE Material/COBBLESTONE]
         Material/IRON_SWORD [Material/STICK Material/IRON_INGOT Material/IRON_INGOT]
         Material/GOLD_SWORD [Material/STICK Material/GOLD_INGOT Material/GOLD_INGOT]
         Material/DIAMOND_SWORD [Material/STICK Material/DIAMOND Material/DIAMOND]}
        player (.getPlayer evt)]
    (when (.isSprinting player)
      (.setVelocity item (.add (.multiply (.getVelocity item) 2.0) (Vector. 0.0 0.5 0.0))))
    (cond
      (table-equip (.getType itemstack))
      (future-call #(let [parts (table-equip (.getType itemstack))]
                      (Thread/sleep 8000)
                      (when (and
                              (not (.isDead item))
                              (#{Material/FURNACE Material/BURNING_FURNACE}
                                  (.getType (.getBlock (.add (.getLocation item) 0 -1 0)))))
                        (doseq [p parts]
                          (.dropItem (.getWorld item) (.getLocation item) (ItemStack. (if (not= 0 (rand-int 10)) p Material/COAL) (.getAmount itemstack))))
                        (when (not-empty (.getEnchantments itemstack))
                          (let [exp (.spawn (.getWorld item) (.getLocation item) ExperienceOrb)]
                            (.setExperience exp (rand-nth (range 10 20)))))
                        (.remove item))))
      (and
        (c/pickaxes (.getType itemstack))
        (= 'pickaxe-skill-teleport (pickaxe-skill-of player)))
      (future-call
        #(do
           (Thread/sleep 2000)
           (when-not (.isDead item)
             (c/teleport-without-angle player (.getLocation item))))))))

(defn player-entity-with-string-event [evt player target]
  (c/consume-item player)
  (.setPassenger player target)
  (.setCancelled evt true)
  (condp instance? target
    Pig (.setAllowFlight player true)
    Chicken (.setAllowFlight player true)
    Squid (do
            (c/lingr (str (.getDisplayName player) " is flying with squid!"))
            (.setAllowFlight player true)
            (.setFoodLevel player 20))
    Player
    (future-call #(do
                    (Thread/sleep 10000)
                    (when (= player (.getPassenger player))
                      (.setPassenger player nil))))
    nil))



(defn player-rightclick-cow [player target]
  (let [itemstack (.getItemInHand player)]
    (if (and
          itemstack
          (= Material/COOKED_BEEF (.getType itemstack)))
      (chimera-cow/birth player target)
      (.dropItemNaturally (.getWorld target) (.getLocation target) (ItemStack. Material/COAL)))))

(defn player-interact-entity-event [evt]
  (let [player (.getPlayer evt)
        target (.getRightClicked evt)]
    (letfn [(d
              ([n] (d n 1))
              ([n ^Byte m]
                (.dropItem (.getWorld target)
                (.getLocation target)
                (ItemStack. (int n) (int 1) (short 0) (Byte. m)))))]
      (cond
        (when-let [passenger (.getPassenger player)]
          (= passenger target))
        (do
          (.sendMessage player (format "Thanks, %s!" (c/entity2name target)))
          (.setAllowFlight player false)
          (.eject player))
        (= Material/STRING (.getType (.getItemInHand player)))
        (player-entity-with-string-event evt player target)

        (and (= (.getType (.getItemInHand player)) Material/COAL)
             (instance? PoweredMinecart target))
        (do
          (.setMaxSpeed target 5.0)
          (let [v (.getVelocity target)
                x (.getX v)
                z (.getY v)
                r2 (max (+ (* x x) (* z z)) 0.1)
                new-x (* 2 (/ x r2))
                new-z (* 2 (/ z r2))]
            (future-call #(do
                            (Thread/sleep 100)
                            (.setVelocity target (Vector. new-x (.getY v) new-z))))))
        ; give wheat to zombie pigman -> pig
        (and (instance? PigZombie target)
             (= (.getTypeId (.getItemInHand player)) 296))
        (do
          (c/swap-entity target Pig)
          (c/consume-item player))
        ; give zombeef to pig -> zombie pigman
        (and (instance? Pig target)
             (= (.getTypeId (.getItemInHand player)) 367))
        (do
          (c/swap-entity target PigZombie)
          (c/consume-item player))
        ; right-click sheep -> wool
        (instance? Sheep target) (d 35 (rand-int 16))
        (instance? Chicken target) (d (.getId Material/FEATHER))
        ; right-click pig -> cocoa
        (instance? Pig target) (d 351 3)
        (instance? Cow target)
        (player-rightclick-cow player target)
        ; right-click villager -> cake
        (instance? Villager target)
        (if-let [item (.getItemInHand player)]
          (condp = (.getType item)
            Material/BROWN_MUSHROOM (do
                                      (.setProfession target Villager$Profession/LIBRARIAN)
                                      (c/consume-item player))
            Material/RED_MUSHROOM (do
                                    (.setProfession target Villager$Profession/PRIEST)
                                    (c/consume-item player))
            Material/YELLOW_FLOWER (do
                                     (.setProfession target Villager$Profession/BLACKSMITH)
                                     (c/consume-item player))
            Material/RED_ROSE (do
                                (.setProfession target Villager$Profession/BUTCHER)
                                (c/consume-item player))
            Material/REDSTONE (do
                                (.setProfession target Villager$Profession/FARMER)
                                (c/consume-item player))
            (d 92))
          (d 92))
        ; right-click creeper -> gunpowder
        (instance? Creeper target) (d 289)

        (and (instance? Zombie target) (not (instance? PigZombie target)))
        (if (= Material/ROTTEN_FLESH (.getType (.getItemInHand player)))
          (do
            (when (= 0 (rand-int 20))
              (.spawn (.getWorld target) (.getLocation target) Giant)
              (c/broadcast "Giant!"))
            (c/consume-item player)
            (.remove target))
          ; right-click zombie -> zombeef
          (d 367))

        ; right-click skelton -> arrow
        (instance? Skeleton target) (d 262)
        ; right-click spider -> string
        (instance? Spider target) (d 287)
        ; right-click squid -> chat and hungry
        (instance? Squid target)
        (let [msg (clojure.string/join "" (map char [65394 65398 65398 65436
                                                     65394 65394 65411 65438
                                                     65405]))]
          (c/lingr msg)
          (c/broadcast (.getDisplayName player) ": " msg)
          (.setFoodLevel player 0))
        ; right-click player -> makes it hungry
        (instance? Player target) (touch-player target)))))

(defn player-level-change-event [evt]
  (when (< (.getOldLevel evt) (.getNewLevel evt))
    (c/broadcast "Level up! "(.getDisplayName (.getPlayer evt)) " is Lv" (.getNewLevel evt))))


(comment (def chain (atom {:entity nil :loc nil})))

(defn chain-entity [entity shooter]
  (comment (swap! chain assoc :entity entity :loc (.getLocation entity)))
  (let [block (.getBlock (.getLocation entity))]
    (when-not (.isLiquid block)
      (let [msg (str (.getDisplayName shooter) " chained " (c/entity2name entity))]
        (.sendMessage shooter msg)
        (c/lingr msg))
      (.setType block Material/WEB)
      (future-call #(do
                      (Thread/sleep 10000)
                      (when (= (.getType block) Material/WEB)
                        (.setType block Material/AIR)))))))

(comment (defn rechain-entity []
  (when (:entity @chain)
    (.teleport (:entity @chain) (:loc @chain)))))

(def chicken-attacking (atom 0))
(defn chicken-touch-player [chicken player]
  (when (not= @chicken-attacking 0)
    (.teleport chicken (.getLocation player))
    (.damage player (rand-int 3) chicken)))

(defn periodically-entity-touch-player-event []
  (doseq [player (Bukkit/getOnlinePlayers)]
    (let [entities (.getNearbyEntities player 2 2 2)
          chickens (filter #(instance? Chicken %) entities)]
      (doseq [chicken chickens]
        (chicken-touch-player chicken player)))))

(defn periodically-terminate-nonchicken-flighter []
  (doseq [player (Bukkit/getOnlinePlayers)]
    (when (and (nil? (.getPassenger player)) (not= "ujm" (.getDisplayName player)))
      (.setAllowFlight player false))))


(defn periodically []
  (periodically-terminate-nonchicken-flighter)
  (comment (rechain-entity))
  (periodically-entity-touch-player-event)
  (comment (.setHealth v (inc (.getHealth v))))
  (chimera-cow/periodically)
  (seq (map player/zombie-player-periodically
            (filter player/zombie? (Bukkit/getOnlinePlayers))))
  nil)

(defn pig-death-event [entity]
  (when-let [killer (.getKiller entity)]
    (when (instance? Player killer)
      (.sendMessage killer "PIG: Pig Is God"))
    (.setFireTicks killer 1000)))

(defn player-respawn-event [evt]
  (let [player (.getPlayer evt)]
    (future-call #(do
                    (.setHealth player (/ (.getMaxHealth player) 3))
                    (.setFoodLevel player 5)))))


(defn spawn-block-generater [entity]
  (let [loc (.getLocation entity)]
    (when
      (and
        (= Material/DIAMOND_BLOCK (.getType (.getBlock (.add (.clone loc) 0 -1 0))))
        (every? identity
                (for [x [-1 0 1] z [-1 0 1]]
                  (= Material/GOLD_BLOCK (.getType (.getBlock (.add (.clone loc) x -2 z)))))))
      (let [block (.getBlock (.add (.clone loc) 0 -1 0))]
        (.setType block Material/MOB_SPAWNER)
        (.setSpawnedType (.getState block) (.getType entity)))
      (doseq [x [-1 0 1] z [-1 0 1]]
        (.setType (.getBlock (.add (.clone loc) x -2 z)) Material/MOSSY_COBBLESTONE))
      (future-call #(.remove entity))
      #_(.setCancelled evt true))))

(defn entity-murder-event [evt entity]
  (let [killer (.getKiller entity)]
    (when (instance? Player killer)
      (when (instance? Zombie entity)
        ((rand-nth
           [#(let [loc (.getLocation entity)]
               (.createExplosion (.getWorld entity) loc 0)
               (when (= Material/AIR (.getType (.getBlock loc)))
                 (.setType (.getBlock loc) Material/FIRE)))
            #(.spawn (.getWorld entity) (.getLocation entity) Villager)
            #(.spawn (.getWorld entity) (.getLocation entity) Silverfish)
            #(.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/IRON_SWORD))])))
      (when (and
              (instance? Blaze entity)
              (= "world" (.getName (.getWorld entity))))
        (blaze2-murder-event evt entity killer))
      (when (chimera-cow/is? entity)
        (chimera-cow/murder-event evt entity killer))
      (when (instance? Giant entity)
        (.setDroppedExp evt 1000))
      (when (instance? Creeper entity)
        (.setDroppedExp evt 10))
      (when (and
              (= 0 (rand-int 2))
              (instance? CaveSpider entity))
        (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/GOLD_SWORD)))
      (.setDroppedExp evt (int (* (.getDroppedExp evt) (/ 15 (.getHealth killer)))))
      (when (= 'exp (arrow-skill-of killer))
        (.setDroppedExp evt (int (* (.getDroppedExp evt) 3))))
      (player/record-and-report killer entity evt))))


(defn entity-death-event [evt]
  (let [entity (.getEntity evt)]
    (cond
      (instance? Pig entity) (pig-death-event entity)
      (instance? Player entity) (player/death-event evt entity)
      (and (instance? LivingEntity entity) (.getKiller entity)) (entity-murder-event evt entity))))

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
  (if false #_(sanctuary/is-in? (.getLocation entity))
    (prn 'cancelled)
    (let [loc (.getLocation entity)]
      (.setType (.getBlock loc) Material/PUMPKIN)
      (c/broadcast "break the bomb before it explodes!")
      (future-call #(do
                      (Thread/sleep 7000)
                      (when (= (.getType (.getBlock loc)) Material/PUMPKIN)
                        (c/broadcast "zawa...")
                        (Thread/sleep 1000)
                        (when (= (.getType (.getBlock loc)) Material/PUMPKIN)
                          (.setType (.getBlock loc) Material/AIR)
                          (let [tnt (.spawn (.getWorld loc) loc TNTPrimed)]
                            (Thread/sleep 1000)
                            (.remove tnt)
                            (c/broadcast "big explosion!")
                            (.createExplosion (.getWorld loc) loc 6 true)))))))))

(defn creeper-explosion-3 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0))

(def creeper-explosion-idx (atom 0))
(defn current-creeper-explosion []
  (get [(fn [_ _] nil)
        creeper-explosion-1
        creeper-explosion-2
        creeper-explosion-3
        ] (rem @creeper-explosion-idx 4)))

(defn entity-explode-event [evt]
  (when-let [entity (.getEntity evt)]
    (let [ename (c/entity2name entity)
          entities-nearby (filter #(instance? Player %) (.getNearbyEntities entity 5 5 5))]
      (cond
        #_((sanctuary/is-in? (.getLocation entity))
          (.setCancelled evt true))

        (instance? Creeper entity)
        (do
          ((current-creeper-explosion) evt entity)
          (swap! creeper-explosion-idx inc))

        (instance? Fireball entity)
        (when (instance? Cow (.getShooter entity))
          (.setCancelled evt true))

        (and ename (not-empty entities-nearby) (not (instance? EnderDragon entity)))
        (letfn [(join [xs x]
                  (apply str (interpose x xs)))]
          (c/lingr (str ename " is exploding near " (join (map #(.getDisplayName %) entities-nearby) ", "))))))))


(comment (defn potion-weakness [name]
  (.apply
    (org.bukkit.potion.PotionEffect. org.bukkit.potion.PotionEffectType/WEAKNESS 500 1)
    (Bukkit/getPlayer name))))

(defn digg-entity [target shooter]
  (loop [depth -1]
    (when (> depth -3)
      (let [loc (.add (.clone (.getLocation target)) 0 depth 0)
            block (.getBlock loc)]
        (when (#{Material/GRASS Material/DIRT Material/STONE
                 Material/GRAVEL Material/SAND Material/SANDSTONE
                 Material/COBBLESTONE Material/SOUL_SAND
                 Material/NETHERRACK Material/AIR} (.getType block))
          (.breakNaturally block (ItemStack. Material/DIAMOND_PICKAXE))
          (let [block-loc (.getLocation block)]
            (.setYaw block-loc (.getYaw loc))
            (.setPitch block-loc (.getPitch loc))
            (.teleport target (.add block-loc 0.5 1 0.5)))
          (recur (dec depth)))))))


(defn arrow-damages-entity-event [evt arrow target]
  (if (and
        (not= 0 (rand-int 10))
        (instance? Player target)
        (when-let [chestplate (.getChestplate (.getInventory target))]
          (and
            (= Material/LEATHER_CHESTPLATE (.getType chestplate))
            (not-empty (.getEnchantments chestplate)))))
    (do
      (c/broadcast (.getDisplayName target) "'s enchanted leather chestplate reflects arrows!")
      (arrow/reflect evt arrow target))
    (when-let [shooter (.getShooter arrow)]
      (when (instance? Player shooter)
        (cond
          (.contains (.getInventory shooter) Material/WEB)
          (do
            (chain-entity target shooter)
            (c/consume-itemstack (.getInventory shooter) Material/WEB))
          (= arrow-skill-explosion (arrow-skill-of shooter))
          (.damage target 10 shooter)
          (= arrow-skill-ice (arrow-skill-of shooter))
          (freeze-for-20-sec target)
          (= 'trap (arrow-skill-of shooter))
          ((rand-nth [chain-entity
                      (comp freeze-for-20-sec first list)
                      digg-entity])
             target shooter)
          (= 'digg (arrow-skill-of shooter))
          (digg-entity target shooter)
          (= arrow-skill-pumpkin (arrow-skill-of shooter))
          (condp instance? target
            Player
            (let [helmet (.getHelmet (.getInventory target))]
              (.setHelmet (.getInventory target) (ItemStack. Material/PUMPKIN))
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
              (.setType block Material/PUMPKIN)
              (future-call #(do
                              (Thread/sleep 3000)
                              (let [newmob (.spawn (.getWorld loc) loc klass)]
                                (if (= Material/PUMPKIN (.getType block))
                                  (do
                                    (.setHealth newmob health)
                                    (.setType block block-type))
                                  (.damage newmob (.getMaxHealth newmob)))))))
            nil)
          (= arrow-skill-diamond (arrow-skill-of shooter))
          (cond
            (some #(instance? % target) [Zombie Skeleton])
            (do
              (.spawn (.getWorld target) (.getLocation target) Villager)
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
          (= 'exp (arrow-skill-of shooter))
          (.damage shooter 2)
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
              (.spawn (.getWorld target) (.getLocation target) change-to))
            (.remove target))
          (= arrow-skill-pull (arrow-skill-of shooter))
          (.teleport target shooter)
          (= arrow-skill-fire (arrow-skill-of shooter))
          (.setFireTicks target 400)
          (= 'cart (arrow-skill-of shooter))
          (let [cart (.spawn (.getWorld target) (.getLocation target) Minecart)]
            (.setPassenger cart target))))
      (when (instance? Blaze shooter)
        "arrow from blaze = always it's by blaze2"
        (blaze2-arrow-hit target)))))

(comment (let [cart (.spawn (.getWorld target) (.getLocation target) Minecart)]
           (future-call #(let [b (.getBlock (.getLocation target))]
                           (.setType b Material/RAILS)))
           (.setPassenger cart target)
           (c/add-velocity cart 0 5 0)))

(defn player-attacks-spider-event [evt player spider]
  (let [cave-spider (.spawn (.getWorld spider) (.getLocation spider) CaveSpider)]
    (.sendMessage player "The spider turned into a cave spider!")
    (.addPotionEffect cave-spider (PotionEffect.
                                    PotionEffectType/BLINDNESS
                                    500
                                    3)))
  (.remove spider))

(defn player-attacks-pig-event [evt player pig]
  (when (= 0 (rand-int 2))
    (future-call #(let [another-pig (.spawn (.getWorld pig) (.getLocation pig) Pig)]
                    (Thread/sleep 3000)
                    (when-not (.isDead another-pig)
                      (.remove another-pig))))))

(defn player-attacks-chicken-event [_ player chicken]
  (when (not= 0 (rand-int 3))
    (let [location (.getLocation player)
          world (.getWorld location)]
      (swap! chicken-attacking inc)
      (future-call #(do
                      (Thread/sleep 20000)
                      (swap! chicken-attacking dec)))
      (doseq [x [-2 -1 0 1 2] z [-2 -1 0 1 2]]
        (let [chicken (.spawn world (.add (.clone location) x 3 z) Chicken)]
          (future-call #(do
                          (Thread/sleep 10000)
                          (.remove chicken))))))))

(defn fish-damages-entity-event [evt fish target]
  (if-let [shooter (.getShooter fish)]
    (let [table {Cow Material/RAW_BEEF
                 Pig Material/PORK
                 Chicken Material/RAW_CHICKEN
                 Zombie Material/LEATHER_CHESTPLATE
                 Skeleton Material/BOW
                 Creeper Material/SULPHUR
                 CaveSpider Material/IRON_INGOT
                 Spider Material/REDSTONE
                 Sheep Material/BED
                 Villager Material/APPLE
                 Silverfish Material/IRON_SWORD
                 IronGolem Material/FISHING_ROD
                 Squid Material/RAW_FISH
                 Blaze Material/GLOWSTONE_DUST
                 MagmaCube Material/FLINT
                 Giant Material/DIRT}]
      (if-let [m (last (first (filter #(instance? (first %) target) table)))]
        (.dropItem (.getWorld target) (.getLocation target) (ItemStack. m 1))
        (cond
          (instance? Player target)
          (do
            (when-let [item (.getItemInHand target)]
              (.damage target 1)
              (.setItemInHand target (ItemStack. Material/AIR))
              (.setItemInHand shooter item)
              (c/lingr (str (.getDisplayName shooter) " fished " (.getDisplayName target)))))

          :else
          (.teleport target shooter))))))




(defn entity-damage-event [evt]
  (let [target (.getEntity evt)
        attacker (when (instance? EntityDamageByEntityEvent evt)
                   (.getDamager evt))]
    (cond
      (= EntityDamageEvent$DamageCause/DROWNING (.getCause evt))
      (when (player/zombie? target)
        (.setCancelled evt true)
        (player/rebirth-from-zombie target))

      (= EntityDamageEvent$DamageCause/ENTITY_EXPLOSION (.getCause evt))
      (do
        #_(prn 'entity-explosion (.getEntity evt))
        (if (= (rem @creeper-explosion-idx 3) 0)
          (.setDamage evt (min (.getDamage evt) 19))
          (.setDamage evt 0)))

      (= EntityDamageEvent$DamageCause/FIRE_TICK (.getCause evt))
      (when (instance? Creeper target)
        (.setCancelled evt true))

      (= EntityDamageEvent$DamageCause/FALL (.getCause evt))
      (cond
        (chimera-cow/is? target)
        (chimera-cow/fall-damage-event evt target)

        (when-let [vehicle (.getVehicle target)]
          (and vehicle (instance? Boat vehicle)))
        (.setCancelled evt true)

        :else
        (let [loc (.add (.getLocation target) 0 -1 0)]
          (doseq [fence [Material/FENCE Material/NETHER_FENCE]]
            (when (= fence (.getType (.getBlock loc)))
              (when (every? #(not= fence %)
                            (map (fn [[x z]]
                                   (.getType (.getBlock (.add (.clone loc) x 0 z))))
                                 [[-1 0] [1 0] [0 -1] [0 1]]))
                (when (instance? Player target)
                  (let [msg (str "Oh trap! " (.getDisplayName target) " was on a needle.")]
                    (c/lingr msg)
                    (.sendMessage target msg)))
                (.damage target 100))))))
      :else
      (do
        (when (and (instance? Blaze target)
                   (= "world" (.getName (.getWorld target))))
          (blaze2-get-damaged evt target))
        (when (and
                (instance? Villager target)
                (instance? EntityDamageByEntityEvent evt)
                (instance? Player attacker))
          (.damage attacker (.getDamage evt)))
        (when (instance? Fish attacker)
          (fish-damages-entity-event evt attacker target))
        (when (instance? Snowball attacker)
          (if-let [shooter (.getShooter attacker)]
            (if (or
                    (@special-snowball-set attacker)
                    (instance? Snowman shooter))
              (do
                (.setFireTicks target 50)
                (.damage target 2 (.getShooter attacker)))
              (let [direction (.subtract (.getLocation target) (.getLocation (.getShooter attacker)))
                    vector (.multiply (.normalize (.toVector direction)) 3)]
                (c/add-velocity target (.getX vector) (+ (.getY vector) 2.0) (.getZ vector))))))
        (when (instance? Enderman attacker)
          (when (instance? Player target)
            (if (= target (.getPassenger attacker))
              (.setCancelled evt true)
              (when (= 0 (rand-int 5))
                (.sendMessage target "Enderman picked you! (sneaking to get off)")
                (.setPassenger attacker target)))))
        (when (instance? Arrow attacker)
          (arrow-damages-entity-event evt attacker target))
        (when (instance? Player attacker)
          (when-not (instance? Player target)
            (spawn-block-generater target))
          (when-let [item (.getItemInHand attacker)]
            (when (c/pickaxes (.getType item))
              (when (= 'pickaxe-skill-fire (pickaxe-skill-of attacker))
                (.setFireTicks target 200))))
          (when (and (instance? Spider target)
                     (not (instance? CaveSpider target)))
            (player-attacks-spider-event evt attacker target))
          (when (instance? Pig target)
            (player-attacks-pig-event evt attacker target))
          (when (instance? Chicken target)
            (player-attacks-chicken-event evt attacker target)))
        (when (and (instance? Player target) (instance? EntityDamageByEntityEvent evt))
          (when (instance? Fireball attacker)
            (when-let [shooter (.getShooter attacker)]
              (when (chimera-cow/is? shooter)
                (chimera-cow/fireball-hit-player evt target shooter attacker))))
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
                (skill target actual-attacker))))
          (when (and (instance? Zombie attacker) (not (instance? PigZombie attacker)))
            (if (player/zombie? target)
              (.setCancelled evt true)
              (player/zombieze target)))
          (when (player/zombie? attacker)
            (player/zombieze target)
            (.sendMessage attacker "You made a friend")))
        (when (chimera-cow/is? target)
          (chimera-cow/damage-event evt target attacker))))))

(defn block-break-event [evt]
  (let [block (.getBlock evt)]
    (when-let [player (.getPlayer evt)]
      (when-let [item (.getItemInHand player)]
        (condp get (.getType item)
          #{Material/AIR}
          (do
            (.sendMessage player "Your hand hurts!")
            (.damage player (rand-int 5)))
          c/pickaxes
          (when (and
                  (= 'pickaxe-skill-ore (pickaxe-skill-of player))
                  (= Material/STONE (.getType block)))
            (letfn [(f [blocktype]
                      (.setType block blocktype)
                      (.setCancelled evt true)
                      (.playEffect (.getWorld block) (.getLocation block) Effect/MOBSPAWNER_FLAMES nil))]
              (cond
                (= 0 (rand-int 10)) (f Material/COAL_ORE)
                (= 0 (rand-int 20)) (f Material/IRON_ORE)
                (= 0 (rand-int 30)) (f Material/REDSTONE_ORE)
                (= 0 (rand-int 40)) (f Material/LAPIS_ORE)
                (= 0 (rand-int 50)) (f Material/GOLD_ORE)
                (= 0 (rand-int 1000)) (f Material/DIAMOND_ORE)
                (= 0 (rand-int 300)) (f Material/GLOWSTONE)
                (= 0 (rand-int 1000)) (f Material/LAPIS_BLOCK)
                (= 0 (rand-int 1500)) (f Material/GOLD_BLOCK)
                (= 0 (rand-int 2000)) (f Material/GOLD_BLOCK)
                (= 0 (rand-int 50000)) (f Material/DIAMOND_BLOCK)
                :else nil)))
          nil)))))

(defn block-grow-event [evt]
  (let [newstate (.getNewState evt)]
    (when (and
            (= Material/PUMPKIN (.getType newstate))
            (= 0 (rand-int 2))
            #_(not-empty (.getNearbyEntities)))
      (.spawn (.getWorld newstate) (.getLocation newstate) Squid)
      (.setCancelled evt true))))

(defn arrow-hit-event [evt entity]
  (cond
    (instance? Player (.getShooter entity))
    (let [skill (arrow-skill-of (.getShooter entity))]
      (cond
        (fn? skill) (skill entity)
        (symbol? skill) nil
        :else (.sendMessage (.getShooter entity) "You don't have a skill yet.")))
    (instance? Skeleton (.getShooter entity))
    (when (= 0 (rand-int 2))
      (.createExplosion (.getWorld entity) (.getLocation entity) 1)
      (.remove entity))
    (instance? Cow (.getShooter entity))
    (chimera-cow/arrow-hit evt)))
        ;(do
        ;  (comment (when (= (.getDisplayName (.getShooter entity)) "sugizou")
        ;             (let [location (.getLocation entity)
        ;                   world (.getWorld location)]
        ;               (.generateTree world location org.bukkit.TreeType/BIRCH))))
        ;  (when (= (.getDisplayName (.getShooter entity)) "Sandkat")
        ;    (doseq [near-target (filter
        ;                          #(instance? LivingEntity %)
        ;                          (.getNearbyEntities entity 2 2 2))]
        ;      (.damage near-target 3 entity)))
        ;  (when (= (.getDisplayName (.getShooter entity)) "ujm")
        ;    (do
        ;      (let [location (.getLocation entity)
        ;            world (.getWorld location)]
        ;        (.strikeLightningEffect world location))
        ;      (doseq [near-target (filter
        ;                            #(instance? Monster %)
        ;                            (.getNearbyEntities entity 10 10 3))]
        ;        (.damage near-target 30 (.getShooter entity))))))

(comment (defn fish-hit-event [evt fish]
  (if-let [shooter (.getShooter fish)]
    (prn ['fish-hit-event evt fish shooter]))))

(defn snowball-hit-event [evt snowball]
  (cond
    (@special-snowball-set snowball)
    (do
      (swap! special-snowball-set disj snowball)
      (comment (let [block (.getBlock (.getLocation snowball))]
                 (when (= Material/AIR (.getType block))
                   (.setType block Material/SNOW)))))
    (instance? Snowman (.getShooter snowball))
    nil

    :else
    (do
      (let [shooter (.getShooter snowball)]
        (.setFoodLevel shooter (dec (.getFoodLevel shooter))))
      (.createExplosion (.getWorld snowball) (.getLocation snowball) 0)
      (.remove snowball))))

(defn projectile-hit-event [evt]
  (let [entity (.getEntity evt)]
        (condp instance? entity
          #_(Fish (fish-hit-event evt entity))
          Arrow (arrow-hit-event evt entity)
          Snowball (snowball-hit-event evt entity)
          ;(instance? Snowball entity) (.strikeLightning (.getWorld entity) (.getLocation entity))
          nil)))


(defn block-dispense-event [evt]
  (when (= Material/SEEDS (.getType (.getItem evt)))
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
                                (= Material/SOIL (.getType soil))
                                (= Material/AIR (.getType air)))
                          (.setType air Material/CROPS)
                          (.remove item))))))
    (.setCancelled evt true)))

(defn player-bed-enter-event [evt]
  (let [player (.getPlayer evt)]
    (c/broadcast (.getDisplayName player) " is sleeping.")
    (.setHealth player 20)
    (future-call #(do
                    (Thread/sleep 3000)
                    (when (.isSleeping player)
                      (let [all-players (Bukkit/getOnlinePlayers)
                            bed-players (filter (memfn isSleeping) all-players)]
                        (when (< (count all-players) (inc (* (count bed-players) 2)))
                          (.setTime world 0)
                          (c/broadcast "good morning everyone!"))))))))

(defn player-bucket-empty-event [evt]
  (future-call
    #(do
       (arrow-skillchange (.getPlayer evt) (.getBlock (.add (.getLocation (.getBlockClicked evt)) 0 1 0)) nil))))

(defn player-toggle-sneak-event [evt]
  (let [player (.getPlayer evt)]
    "recovery spa"
    (let [loc (.add (.getLocation player) 0 1 0)]
      (when (= Material/STATIONARY_WATER (.getType (.getBlock loc)))
        (when (blazon? Material/STONE (.getBlock loc))
          (when (= 0 (rand-int 10))
            (.setType (.getBlock loc) Material/AIR))
          (c/broadcast (.getDisplayName player) ": recovery spa!")
          (.setHealth player 20)
          (.setFoodLevel player 20)
          (.teleport player loc)
          (c/add-velocity player 0 0.6 0))))
    (transport/cauldron-teleport player)
    (when-let [vehicle (.getVehicle player)]
      (cond
        (instance? Boat vehicle) (.setVelocity vehicle (Vector. 0 0 0))
        (instance? Enderman vehicle) (.leaveVehicle player)))))

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

(defn vehicle-block-collision-event [evt]
  (let [vehicle (.getVehicle evt)]
    (when-let [passenger (.getPassenger vehicle)]
      (when (instance? Boat vehicle)
        (when (.getWorkOnLand vehicle)
          (let [block (.getBlock evt)]
            (.teleport vehicle (.add (.getLocation vehicle) 0 1 0))))))))

#_(defn vehicle-damage-event [evt]
  (prn 'vehicle-damage))

(defn vehicle-destroy-event [evt]
  (let [vehicle (.getVehicle evt)]
    (when-let [passenger (.getPassenger vehicle)]
      (.setCancelled evt true))))

#_(defn vehicle-entity-collision-event [evt]
  (prn 'vehicle-entity-collision-event))


(comment (defn enderman-pickup-event* [evt]
  (prn 'epe)))

(comment (defn enderman-pickup-event []
  (c/auto-proxy [Listener] []
                (onEndermanPickup [evt] (enderman-pickup-event* evt)))))

(defn good-bye [klass]
  (count (seq (map #(.remove %)
                   (filter #(instance? klass %)
                           (.getLivingEntities world))))))

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
  (cloft.block/can-build-event evt))

(defonce swank* nil)
(defn on-enable [plugin]
  (when (nil? swank*)
    (def swank* (swank.swank/start-repl 4005)))
  (cloft.recipe/on-enable)
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin (fn [] (periodically)) 50 50)
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin (fn [] (cloft-scheduler/on-beat)) 0 1)
  (comment (proxy [java.lang.Object CommandExecuter] []
    (onCommand [this ^CommandSender sender ^Command command ^String label ^String[] args]
      (prn command))))
  (c/lingr "cloft plugin running...")
  (cloft-scheduler/msg "core.clj")
  (future-call (fn []
                 (do
                  (let [ctx (mq/context 1)
                        subscriber (mq/socket ctx mq/sub)]
                    (mq/bind subscriber "tcp://*:1235")
                    (mq/subscribe subscriber "")
                    (while true
                      (let [contents (read-string (mq/recv-str subscriber))
                            players (Bukkit/getOnlinePlayers)]
                        (prn 'received contents)
                        (condp #(.startsWith %2 %1) (:body contents)
                          "/list"
                          (let [msg (if (empty? players)
                                      "(no players)"
                                      (clojure.string/join "\n" (map #(player/player-inspect % (= (:body contents) "/list -l")) players)))]
                            (c/lingr "computer_science" msg)
                            (c/broadcast msg))
                          "/chicken"
                          (future-call
                            (fn []
                              (doseq [p (Bukkit/getOnlinePlayers)]
                                (when-not (.isDead p)
                                  (.spawn (.getWorld p) (.add (.getLocation p) 0 2 0) Chicken)))))
                          (when-not (empty? players)
                            (c/broadcast (str (:user contents) ": " (:body contents)))))))))))
  #_(c/lingr "cloft plugin running..."))
