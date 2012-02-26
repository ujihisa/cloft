(ns cloft.core
  (:require [cljminecraft.core :as c])
  (:import [org.bukkit Bukkit])
  (:import [org.bukkit.event Event Event$Type])
  (:import [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity Item LightningStrike LivingEntity
            MagmaCube Minecart Monster MushroomCow NPC Painting Pig PigZombie
            Player PoweredMinecart Projectile Sheep Silverfish Skeleton Slime
            SmallFireball Snowball Snowman Spider Squid StorageMinecart
            ThrownPotion TNTPrimed Vehicle Villager WaterMob Weather Wolf
            Zombie])
  (:import [org.bukkit.event.entity CreatureSpawnEvent CreeperPowerEvent
            EndermanPickupEvent EndermanPlaceEvent EntityChangeBlockEvent
            EntityCombustByBlockEvent EntityCombustByEntityEvent
            EntityCombustEvent EntityCreatePortalEvent EntityDamageByBlockEvent
            EntityDamageByEntityEvent EntityDamageByProjectileEvent
            EntityDamageEvent EntityDeathEvent EntityEvent EntityExplodeEvent
            EntityDamageEvent$DamageCause
            EntityInteractEvent EntityListener EntityPortalEnterEvent
            EntityRegainHealthEvent EntityShootBowEvent EntityTameEvent
            ;EntityTargetEvent EntityTeleportEvent ExplosionPrimeEvent
            EntityTargetEvent ExplosionPrimeEvent
            FoodLevelChangeEvent ItemDespawnEvent ItemSpawnEvent PigZapEvent
            PlayerDeathEvent PotionSplashEvent ProjectileHitEvent
            SheepDyeWoolEvent SheepRegrowWoolEvent SlimeSplitEvent])
  (:require clj-http.client))

(def NAME-ICON
  {"ujm" "http://www.gravatar.com/avatar/d9d0ceb387e3b6de5c4562af78e8a910.jpg?s=28\n"
   "sbwhitecap" "http://www.gravatar.com/avatar/198149c17c72f7db3a15e432b454067e.jpg?s=28\n"
   "Sandkat" "https://twimg0-a.akamaihd.net/profile_images/1584518036/claire2_mini.jpg\n"
   "kldsas" "http://a0.twimg.com/profile_images/1825629510/____normal.png\n"
   "raa0121" "http://a0.twimg.com/profile_images/1414030177/nakamigi_normal.png\n"})

(def zombie-players (atom #{}))

(defn zombie-player? [p]
  (boolean (get @zombie-players (.getDisplayName p))))

(defn name2icon [name]
  (get NAME-ICON name (str name ": ")))

(def BOT-VERIFIER
  (apply str (drop-last (try
                          (slurp "bot_verifier.txt")
                          (catch java.io.FileNotFoundException e "")))))

(defn lingr [msg]
  (future-call
    #(clj-http.client/post
       "http://lingr.com/api/room/say"
       {:form-params
        {:room "mcujm"
         :bot 'cloft
         :text (str msg)
         :bot_verifier BOT-VERIFIER}})))

;(defn block-break [evt]
;  (.sendMessage (.getPlayer evt) "You know. Breaking stuff should be illegal."))
;
;(defn sign-change [evt]
;  (.sendMessage (.getPlayer evt) "Now I've placed a sign and changed the text"))
;
;(defn player-move [evt]
;  (.sendMessage (.getPlayer evt) "Ok, no, really.. stop moving."))
;
;(defn get-blocklistener []
;  (c/auto-proxy
;   [org.bukkit.event.block.BlockListener] []
;   (onBlockBreak [evt] (if (.isCancelled evt) nil (block-break evt)))
;   (onSignChange [evt] (if (.isCancelled evt) nil (sign-change evt))))
;  )

(defn location-in-lisp [location]
  (list
    'org.bukkit.Location.
    (.getName (.getWorld location))
    (.getX location)
    (.getY location)
    (.getZ location)
    (.getPitch location)
    (.getYaw location)))

(defn swap-entity [target klass]
  (let [location (.getLocation target)
        world (.getWorld target)]
    (.remove target)
    (.spawn world location klass)))

(defn consume-item [player]
  (let [itemstack (.getItemInHand player)]
    (.setAmount itemstack (dec (.getAmount itemstack)))))

(def world (Bukkit/getWorld "world"))
(def place1 (org.bukkit.Location. world -55.5 71.5 73.5)) ; lighter
(def place2 (org.bukkit.Location. world -63.5 71.5 73.5)) ; darker
(def place3 (org.bukkit.Location. world 18.46875 103.0 41.53125 -10.501099 1.5000023)) ; on top of tree
(def place4 (org.bukkit.Location. world -363.4252856675041 65.0 19.551327467732065 -273.89978 15.149968)) ; goboh villae
(def place5 (org.bukkit.Location. world -5 73 -42.5)) ; top of pyramid
(def place6 (org.bukkit.Location. world 308.98823982676504 78 133.16713120198153 -55.351166 20.250006)) ; dessert village
(def place7 (org.bukkit.Location. world -1.4375 63.5 5.28125)) ; toilet
(def place8 (org.bukkit.Location. world 61.0 57.0 3.375)) ; at a log house
(def place9 (org.bukkit.Location. world -456.6875 64.0 25.53125)) ; from goboh3 village
(def place10 (org.bukkit.Location. world 317.4375 72.0 112.5)) ; from dessert village
(def place-main (org.bukkit.Location. world 4.294394438259979 67.0 0.6542090982205075 -7.5000114 -40.35013))
(def anotherbed (org.bukkit.Location.  world -237.8704284429714 72.5625 -53.82154923217098 19.349966 -180.45361))

(defn get-player [name]
  (first (filter #(= (.getDisplayName %) name) (Bukkit/getOnlinePlayers))))
(defn ujm [] (get-player "ujm"))

(defn jumping? [moveevt]
  (< (.getY (.getFrom moveevt)) (.getY (.getTo moveevt))))

(def player-death-locations (atom {}))
(defn player-teleport-machine [evt player]
  (when (and
          (= (.getWorld player) world)
          (< (.distance place2 (.getLocation player)) 1))
    (lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place3))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place1 (.getLocation player)) 1)
          (.isLoaded (.getChunk place4)))
    (lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place4))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place5 (.getLocation player)) 1)
          (.isLoaded (.getChunk place6)))
    (lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place6))
  (when (and
          (= (.getWorld player) world)
          (or
            (< (.distance place9 (.getLocation player)) 1)
            (< (.distance place10 (.getLocation player)) 1))
          (.isLoaded (.getChunk place-main)))
    (lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place-main))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place7 (.getLocation player)) 1))
    (let [death-point (get @player-death-locations (.getDisplayName player))]
      (when death-point
        (.isLoaded (.getChunk death-point)) ; for side-effect
        (lingr (str (.getDisplayName player) " is teleporting to the last death place..."))
        (.setTo evt death-point)))))

(defn consume-itemstack [inventory mtype]
  (let [idx (.first inventory mtype)
        itemstack (.getItem inventory idx)
        amount (.getAmount itemstack)]
    (if (= 1 amount)
      (.remove inventory itemstack)
      (.setAmount itemstack (dec amount)))))

(def super-jump-flags (atom {}))
(defn player-super-jump [evt player]
  (let [name (.getDisplayName player)]
    (when (not (get @super-jump-flags name))
      (when (= (.getType (.getItemInHand player)) org.bukkit.Material/FEATHER)
        (swap! super-jump-flags assoc name true)
        (future-call #(do
                        (Thread/sleep 2000)
                        (.sendMessage player "super jump charging...")
                        (Thread/sleep 5000)
                        (.sendMessage player "super jump charge done")
                        (swap! super-jump-flags assoc name false)))
        (let [amount (.getAmount (.getItemInHand player))
              x (if (.isSprinting player) (* amount 2) amount)
              x2 (/ (java.lang.Math/log x) 2) ]
          (lingr (str name " is super jumping with level " x))
          (consume-itemstack (.getInventory player) org.bukkit.Material/FEATHER)
          (.setVelocity
            player
            (.add (org.bukkit.util.Vector. 0.0 x2 0.0) (.getVelocity player))))))))

(defn broadcast [& strs]
  (.broadcastMessage (Bukkit/getServer) (apply str strs)))

(def sanctuary [(org.bukkit.Location. world 45 30 -75)
                (org.bukkit.Location. world 84 90 -44)])
(def sanctuary-players (atom #{}))

(defn location-bound? [loc min max]
  (.isInAABB (.toVector loc) (.toVector min) (.toVector max)))

(defn player-move-event* [evt]
  (let [player (.getPlayer evt)]
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
    (let [name (.getDisplayName player)]
      (if (get @sanctuary-players name)
        (when (not (location-bound? (.getLocation player) (first sanctuary) (second sanctuary)))
          (swap! sanctuary-players disj name))
        (when (location-bound? (.getLocation player) (first sanctuary) (second sanctuary))
          (broadcast name " entered the sanctuary.")
          (swap! sanctuary-players conj name))))
    (when (jumping? evt)
      (player-teleport-machine evt player)
      (player-super-jump evt player))))

(defn arrow-skill-torch [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.setType (.getBlockAt world location) org.bukkit.Material/TORCH)))

(defn arrow-skill-teleport [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)
        shooter (.getShooter entity)]
    (.setYaw location (.getYaw (.getLocation shooter)))
    (.setPitch location (.getPitch (.getLocation shooter)))
    (.teleport shooter location)))

(defn arrow-skill-fire [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (doseq [target (filter #(instance? LivingEntity %) (.getNearbyEntities entity 1 1 1))]
      (do
        (prn (str "fire on " target))
        (.setFireTicks target 200)))))

(defn arrow-skill-tree [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.generateTree world location org.bukkit.TreeType/BIRCH)))

(defn arrow-skill-ore [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)
        velocity (.getVelocity entity)
        direction (.multiply (.clone velocity) (double (/ 1 (.length velocity))))
        block (.getBlock (.add (.clone location) direction))]
    (when (= (.getType block) org.bukkit.Material/STONE)
      (let [block-to-choices [org.bukkit.Material/COAL_ORE
                              org.bukkit.Material/COAL_ORE
                              org.bukkit.Material/COBBLESTONE
                              org.bukkit.Material/COBBLESTONE
                              org.bukkit.Material/GRAVEL
                              org.bukkit.Material/IRON_ORE
                              org.bukkit.Material/LAPIS_ORE
                              org.bukkit.Material/GOLD_ORE
                              org.bukkit.Material/REDSTONE_ORE]]
        (.setType block (rand-nth block-to-choices))))))

(def jobs (atom {}))

(def bowgun-players (atom #{"ujm"}))
(defn add-bowgun-player [name]
  (swap! bowgun-players conj name))

(defn entity-shoot-bow-event* [evt]
  (let [shooter (.getEntity evt)]
    (when (instance? Player shooter)
      (when (.isSneaking shooter)
        (.setVelocity (.getProjectile evt) (.multiply (.getVelocity (.getProjectile evt)) 3)))
      (comment (.setCancelled evt true))
      (comment (.setVelocity shooter (.multiply (.getVelocity (.getProjectile evt)) 2)))
      (comment (when (and
              (get @bowgun-players (.getDisplayName shooter))
              (not= arrow-skill-teleport (get @jobs (.getDisplayName shooter))))
        (future-call #(do
                        (Thread/sleep 100) (.shootArrow (.getEntity evt))
                        (Thread/sleep 300) (.shootArrow (.getEntity evt))
                        (Thread/sleep 500) (.shootArrow (.getEntity evt))
                        )))))))

(defn entity-shoot-bow-event []
  (c/auto-proxy [org.bukkit.event.entity.EntityListener] []
                (onEntityShootBow [evt] (entity-shoot-bow-event* evt))))

(defn entity-target-event* [evt]
  (when (instance? Creeper (.getEntity evt))
    (broadcast "Takumi is watching " (.getDisplayName (.getTarget evt)))))

(defn entity-target-event []
  (c/auto-proxy [org.bukkit.event.entity.EntityListener] []
                (onEntityTarget [evt] (entity-target-event* evt))))

(defn entity-explosion-prime-event* [evt]
  nil)

(defn entity-explosion-prime-event []
  (c/auto-proxy [org.bukkit.event.entity.EntityListener] []
                (onEntityExplosionPrime [evt] (entity-explosion-prime-event* evt))))

(defn build-long [block block-against]
  (comment (when (= (.getType block) (.getType block-against))
    (let [world (.getWorld block)
          loc (.getLocation block)
          diff (.subtract (.clone loc) (.getLocation block-against))]
      (doseq [m (range 1 10)]
        (let [newblock (.getBlockAt
                         world
                         (.add (.clone loc) (.multiply (.clone diff) (double m))))]
          (when (= (.getType newblock) org.bukkit.Material/AIR)
            (.setType newblock (.getType block)))))))))

(defn skillchange [player block block-against]
  (when (and
          (every? identity (map
                             #(=
                                (.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                                org.bukkit.Material/STONE)
                             [0 0 -1 1] [-1 1 0 0]))
          (every? identity (map
                             #(not=
                                (.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                                org.bukkit.Material/STONE)
                             [-1 1 0 0] [-1 1 0 0])))
    (when (= (.getType block) org.bukkit.Material/TORCH)
      (broadcast (.getDisplayName player) " changed arrow skill to TORCH")
      (swap! jobs assoc (.getDisplayName player) arrow-skill-torch))
    (when (= (.getType block) org.bukkit.Material/YELLOW_FLOWER)
      (broadcast (.getDisplayName player) " changed arrow skill to TELEPORT")
      (swap! jobs assoc (.getDisplayName player) arrow-skill-teleport))
    (when (= (.getType block) org.bukkit.Material/RED_ROSE)
      (broadcast (.getDisplayName player) " changed arrow skill to FIRE")
      (swap! jobs assoc (.getDisplayName player) arrow-skill-fire))
    (when (= (.getType block) org.bukkit.Material/SAPLING)
      (broadcast (.getDisplayName player) " changed arrow skill to TREE")
      (swap! jobs assoc (.getDisplayName player) arrow-skill-tree))
    (when (= (.getType block) org.bukkit.Material/WORKBENCH)
      (broadcast (.getDisplayName player) " changed arrow skill to ORE")
      (swap! jobs assoc (.getDisplayName player) arrow-skill-ore))))

(defn block-place-event* [evt]
  (let [block (.getBlock evt)]
    (comment (.spawn (.getWorld block) (.getLocation block) Pig))
    (let [player (.getPlayer evt)]
      (skillchange player block (.getBlockAgainst evt))
      (comment (prn (vector-from-to block player))
               (.setVelocity player (vector-from-to player block))
               (doseq [entity (.getNearbyEntities player 4 4 4)]
                 (.setVelocity entity (vector-from-to entity block)))))
    (build-long block (.getBlockAgainst evt))
    (when (location-bound? (.getLocation block) (first sanctuary) (second sanctuary))
      (.setCancelled evt true))))

(defn block-place-event []
  (c/auto-proxy [org.bukkit.event.block.BlockListener] []
                (onBlockPlace [evt] (block-place-event* evt))))

(defn block-break-event* [evt]
  (let [block (.getBlock evt)]
    (when (location-bound? (.getLocation block) (first sanctuary) (second sanctuary))
      (.setCancelled evt true))))

(defn block-break-event []
  (c/auto-proxy [org.bukkit.event.block.BlockListener] []
                (onBlockBreak [evt] (block-break-event* evt))))

(defn player-move-event []
  (c/auto-proxy [org.bukkit.event.player.PlayerListener] []
                  (onPlayerMove [evt] (player-move-event* evt))))

(defn player-login-event* [evt]
  (let [player (.getPlayer evt)]
    (when (= (.getDisplayName player) "Player")
      (.setDisplayName player "raa0121"))
    (lingr (str (name2icon (.getDisplayName player)) "logged in now."))))

(defn player-login-event []
  (c/auto-proxy [org.bukkit.event.player.PlayerListener] []
     (onPlayerLogin [evt] (player-login-event* evt))))

(defn get-player-quit-listener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerQuit
      [evt]
      (lingr (str (name2icon (.getDisplayName (.getPlayer evt))) "quitted.")))))

(defn get-player-chat []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerChat
      [evt]
      (let [name (.getDisplayName (.getPlayer evt))]
        (lingr (str (name2icon name) (.getMessage evt)))))))

(defn touch-player [target]
  (.setFoodLevel target (dec (.getFoodLevel target))))

(defn get-player-interact-entity []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerInteractEntity
      [evt]
      (let [target (.getRightClicked evt)]
        (letfn [(d [n]
                  (.dropItem (.getWorld target)
                             (.getLocation target)
                             (org.bukkit.inventory.ItemStack. n 1)))]
          (cond
            ; give wheat to zombie pigman -> pig
            (and
              (instance? PigZombie target)
              (= (.getTypeId (.getItemInHand (.getPlayer evt))) 296)) (do
                                                                        (swap-entity target Pig)
                                                                        (consume-item (.getPlayer evt)))
            ; give zombeef to pig -> zombie pigman
            (and
              (instance? Pig target)
              (= (.getTypeId (.getItemInHand (.getPlayer evt))) 367)) (do
                                                                        (swap-entity target PigZombie)
                                                                        (consume-item (.getPlayer evt)))
            ; right-click villager -> cake
            (instance? Villager target) (d 92)
            ; right-click zombie -> zombeef
            (and (instance? Zombie target) (not (instance? PigZombie target))) (d 367)
            ; right-click skelton -> arrow
            (instance? Skeleton target) (d 262)
            ; right-click spider -> string
            (instance? Spider target) (d 287)
            ; right-click squid -> chat and hungry
            (instance? Squid target) (let [player (.getPlayer evt)]
                                       (.chat player "ikakawaiidesu")
                                       (.setFoodLevel player 0))
            ; right-click player -> makes it hungry
            (instance? Player target) (touch-player target)))))))

(defn player-level-change-event* [evt]
  (when (< (.getOldLevel evt) (.getNewLevel evt))
    (broadcast "Level up! "(.getDisplayName (.getPlayer evt)) " is Lv" (.getNewLevel evt))))

(defn player-level-change-event []
  (c/auto-proxy [org.bukkit.event.player.PlayerListener] []
                (onPlayerLevelChange [evt] (player-level-change-event* evt))))

; internal
(defn zombie-player-periodically [zplayer]
  (when (= 15 (.getLightLevel (.getBlock (.getLocation zplayer))))
    (.setFireTicks zplayer 100))
  (when (= 0 (rand-int 2))
    (.setFoodLevel zplayer (dec (.getFoodLevel zplayer)))))

(def chain (atom {:entity nil :loc nil}))

(defn chain-entity [entity]
  (swap! chain assoc :entity entity :loc (.getLocation entity))
  (let [block (.getBlockAt world (:loc @chain))]
    (when (= org.bukkit.Material/AIR (.getType block))
      (.setType block org.bukkit.Material/WEB))))

(defn rechain-entity []
  (when (:entity @chain)
    (.teleport (:entity @chain) (:loc @chain))))

(def chicken-attacking (atom 0))
(defn chicken-touch-player [chicken player]
  (when (not= @chicken-attacking 0)
    (.teleport chicken (.getLocation player))
    (.damage player 3 chicken)))

(defn entity-touch-player-event []
  (doseq [player (Bukkit/getOnlinePlayers)]
    (let [entities (.getNearbyEntities player 2 2 2)
          chickens (filter #(instance? Chicken %) entities)]
      (doseq [chicken chickens]
        (chicken-touch-player chicken player)))))

(defn periodically []
  (rechain-entity)
  (entity-touch-player-event)
  (comment (.setHealth v (inc (.getHealth v))))
  (seq (map zombie-player-periodically
            (filter zombie-player? (Bukkit/getOnlinePlayers))))
  nil)

(defn entity2name [entity]
  (cond (instance? Blaze entity) "Blaze"
        (instance? CaveSpider entity) "CaveSpider"
        (instance? Chicken entity) "Chicken"
        ;(instance? ComplexLivingEntity entity) "ComplexLivingEntity"
        (instance? Cow entity) "Cow"
        ;(instance? Creature entity) "Creature"
        (instance? Creeper entity) "Creeper"
        (instance? EnderDragon entity) "EnderDragon"
        (instance? Enderman entity) "Enderman"
        ;(instance? Flying entity) "Flying"
        (instance? Ghast entity) "Ghast"
        (instance? Giant entity) "Giant"
        ;(instance? HumanEntity entity) "HumanEntity"
        (instance? MagmaCube entity) "MagmaCube"
        ;(instance? Monster entity) "Monster"
        (instance? MushroomCow entity) "MushroomCow"
        ;(instance? NPC entity) "NPC"
        (instance? Pig entity) "Pig"
        (instance? PigZombie entity) "PigZombie"
        (instance? Player entity) (.getDisplayName entity)
        (instance? Sheep entity) "Sheep"
        (instance? Silverfish entity) "Silverfish"
        (instance? Skeleton entity) "Skeleton"
        (instance? Slime entity) "Slime"
        (instance? Snowman entity) "Snowman"
        (instance? Spider entity) "Spider"
        (instance? Squid entity) "Squid"
        (instance? Villager entity) "Villager"
        ;(instance? WaterMob entity) "WaterMob"
        (instance? Wolf entity) "Wolf"
        (instance? Zombie entity) "Zombie"
        (instance? TNTPrimed entity) "TNT"
        :else (str (class entity))))

(defn pig-death-event [entity]
  (let [killer (.getKiller entity)]
    (when killer
      (.sendMessage killer "PIG: Pig Is God")
      (.setFireTicks killer 100))))

(defn entity-murder-event [evt entity]
  (let [killer (.getKiller entity)]
    (when (instance? Player killer)
      (when (instance? Giant entity)
        (.setDroppedExp evt 1000))
      (when (instance? Creeper entity)
        (.setDroppedExp evt 20))
      (.setDroppedExp evt (int (* (.getDroppedExp evt) (/ 15 (.getHealth killer)))))
      (broadcast (.getDisplayName killer) " killed " (entity2name entity) " (exp: " (.getDroppedExp evt) ")"))))

(defn player-death-event [evt player]
  (swap! player-death-locations assoc (.getDisplayName player) (.getLocation player))
  (lingr (str (name2icon (.getDisplayName player)) (.getDeathMessage evt))))

(defn entity-death-event* [evt]
  (let [entity (.getEntity evt)]
    (cond
      (instance? Pig entity) (pig-death-event entity)
      (instance? Player entity) (player-death-event evt entity)
      (and (instance? LivingEntity entity) (.getKiller entity)) (entity-murder-event evt entity))))

(defn entity-death-event []
  (c/auto-proxy [EntityListener] []
                (onEntityDeath [evt] (entity-death-event* evt))))

(defn creeper-explosion-1 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0)
  (doseq [e (filter #(instance? LivingEntity %) (.getNearbyEntities entity 5 5 5))]
    (let [v (.multiply (.toVector (.subtract (.getLocation e) (.getLocation entity))) 2.0)
          x (- 5 (.getX v))
          z (- 5 (.getZ v))]
      (when (instance? Player e)
        (.sendMessage e "Air Explosion"))
      (.setVelocity e (org.bukkit.util.Vector. x 1.5 z))))
  (comment (let [another (.spawn (.getWorld entity) (.getLocation entity) Creeper)]
             (.setVelocity another (org.bukkit.util.Vector. 0 1 0)))))

(defn creeper-explosion-2 [evt entity]
  (.setCancelled evt true)
  (if (location-bound? (.getLocation entity) (first sanctuary) (second sanctuary))
    (prn 'cancelled)
    (let [loc (.getLocation entity)]
      (.setType (.getBlock loc) org.bukkit.Material/PUMPKIN)
      (broadcast "break the bomb before it explodes!")
      (future-call #(do
                      (Thread/sleep 7000)
                      (broadcast "zawa...")
                      (Thread/sleep 1000)
                      (when (= (.getType (.getBlock loc)) org.bukkit.Material/PUMPKIN)
                        (.createExplosion (.getWorld loc) loc 6)))))))

(def creeper-explosion-idx (atom 0))
(defn entity-explode-event* [evt]
  (let [entity (.getEntity evt)
        ename (entity2name entity)
        entities-nearby (filter #(instance? Player %) (.getNearbyEntities entity 5 5 5))]
    (when (and ename (not-empty entities-nearby) (not (instance? EnderDragon entity)))
      (letfn [(join [xs x]
                (apply str (interpose x xs)))]
        (lingr (str ename " is exploding near " (join (map #(.getDisplayName %) entities-nearby) ", ")))))
    (when (instance? Creeper entity)
      ((get [(fn [_ _] nil)
             creeper-explosion-1
             creeper-explosion-2
             ] (rem @creeper-explosion-idx 3)) evt entity)
      (swap! creeper-explosion-idx inc))
    (when (instance? TNTPrimed entity)
      (prn ['TNT entity]))
    (when (location-bound? (.getLocation entity) (first sanctuary) (second sanctuary))
      (.setCancelled evt true))))

(defn entity-explode-event []
  (c/auto-proxy [EntityListener] []
                (onEntityExplode [evt] (entity-explode-event* evt))))

(defn zombieze [entity]
  (swap! zombie-players conj (.getDisplayName entity))
  (.setMaximumAir entity 1)
  (.setRemainingAir entity 1)
  (.sendMessage entity "You turned into a zombie.")
  (lingr (str (name2icon (.getDisplayName entity)) "turned into a zombie.")))

(comment (defn potion-weakness [name]
  (.apply
    (org.bukkit.potion.PotionEffect. org.bukkit.potion.PotionEffectType/WEAKNESS 500 1)
    (Bukkit/getPlayer name))))

(defn arrow-attacks-by-player-event [_ arrow target]
  (let [shooter (.getShooter arrow)]
    (when (and
            (instance? Player shooter)
            (.contains (.getInventory shooter) org.bukkit.Material/WEB))
      (let [msg (str (.getDisplayName shooter) " chained " (entity2name target))]
        (.sendMessage shooter msg)
        (lingr msg))
      (chain-entity target)
      (consume-itemstack (.getInventory shooter) org.bukkit.Material/WEB))))

(defn vector-from-to [ent-from ent-to]
  (.toVector (.subtract (.getLocation ent-to) (.getLocation ent-from))))

(defn player-attacks-pig-event [evt player pig]
  (let [arrow (.shootArrow pig)
        enemy (first (filter #(instance? Monster %) (.getNearbyEntities pig 20 20 20)))]
    (when enemy
      (.setTarget pig player)
      (.setVelocity arrow (vector-from-to pig enemy))))
  (.setCancelled evt true))

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

(defn rebirth-from-zombie [evt target]
  (.setCancelled evt true)
  (.setMaximumAir target 300) ; default maximum value
  (.setRemainingAir target 300)
  (.setHealth target (.getMaxHealth target))
  (swap! zombie-players disj (.getDisplayName target))
  (.sendMessage target "You rebirthed as a human."))

(defn get-entity-damage-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityDamage [evt]
      (let [target (.getEntity evt)
            attacker (when (instance? EntityDamageByEntityEvent evt)
                       (.getDamager evt))]
        (if (= EntityDamageEvent$DamageCause/DROWNING (.getCause evt))
          (when (and
                  (instance? Player target)
                  (zombie-player? target)
                  (= 0 (rand-int 2)))
            (rebirth-from-zombie evt target))
          (do
            (when (and
                    (instance? Villager target)
                    (instance? EntityDamageByEntityEvent evt)
                    (instance? Player attacker))
              (lingr (str (name2icon (.getDisplayName attacker)) "is attacking a Villager"))
              (.damage attacker (.getDamage evt)))
            (when (instance? Arrow attacker)
              (arrow-attacks-by-player-event evt attacker target))
            (when (and (instance? Player attacker) (instance? Pig target))
              (player-attacks-pig-event evt attacker target))
            (when (and (instance? Player attacker) (instance? Chicken target))
              (player-attacks-chicken-event evt attacker target))
            (when (and (instance? Player target) (instance? EntityDamageByEntityEvent evt))
              (when (and (instance? Zombie attacker) (not (instance? PigZombie attacker)))
                (if (zombie-player? target)
                  (.setCancelled evt true)
                  (zombieze target)))
              (when (and (instance? Player attacker) (zombie-player? attacker))
                (do
                  (zombieze target)
                  (.sendMessage attacker "You made a friend"))))))))))


(defn arrow-hit-event [evt entity]
  (when (instance? Player (.getShooter entity))
    (let [skill (get @jobs (.getDisplayName (.getShooter entity)))]
      (if skill
        (skill entity)
        (.sendMessage (.getShooter entity) "You don't have a skill yet.")
        ;(do
        ;  (comment (when (= (.getDisplayName (.getShooter entity)) "sugizou")
        ;             (let [location (.getLocation entity)
        ;                   world (.getWorld location)]
        ;               (.generateTree world location org.bukkit.TreeType/BIRCH))))
        ;  (when (= (.getDisplayName (.getShooter entity)) "kldsas")
        ;    (arrow-skill-torch entity))
        ;  (when (= (.getDisplayName (.getShooter entity)) "sbwhitecap")
        ;    (arrow-skill-teleport entity))
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
        ))
    ))

(defn get-entity-projectile-hit-listener []
  (c/auto-proxy
    [EntityListener] []
    (onProjectileHit [evt]
      (let [entity (.getEntity evt)]
        (cond
          (instance? Fireball entity) (.setYield entity 0.0)
          (instance? Arrow entity) (arrow-hit-event evt entity)
          ;(instance? Snowball entity) (.strikeLightning (.getWorld entity) (.getLocation entity))
          )))))

(defn vehicle-enter-event* [evt]
  (let [vehicle (.getVehicle evt)
        entity (.getEntered evt)
        rail (.getBlock (.getLocation vehicle))
        block-under (.getBlock (.add (.getLocation vehicle) 0 -1 0))]
    (when (and
            (instance? Player entity)
            (= (.getType rail) org.bukkit.Material/RAILS)
            (= (.getType block-under) org.bukkit.Material/LAPIS_BLOCK))
      (let [direction (.getDirection (.getNewData (.getType rail) (.getData rail)))
            diff (cond
                   (= org.bukkit.block.BlockFace/SOUTH direction) (org.bukkit.util.Vector. -1 0 0)
                   (= org.bukkit.block.BlockFace/NORTH direction) (org.bukkit.util.Vector. 1 0 0)
                   (= org.bukkit.block.BlockFace/WEST direction) (org.bukkit.util.Vector. 0 0 1)
                   (= org.bukkit.block.BlockFace/EAST direction) (org.bukkit.util.Vector. 0 0 -1))
            destination (first (filter
                                 #(= (.getType %) org.bukkit.Material/LAPIS_BLOCK)
                                 (map
                                   #(.getBlock (.add (.clone (.getLocation block-under)) (.multiply (.clone diff) %)))
                                   (range 3 100))))]
        (when destination
          (.teleport vehicle (.add (.getLocation destination) 0 3 0)))))))

(defn vehicle-enter-event []
  (c/auto-proxy [org.bukkit.event.vehicle.VehicleListener] []
                (onVehicleEnter [evt] (vehicle-enter-event* evt))))

(defn good-bye-creeper []
  (count (seq (map #(.remove %)
                   (filter #(instance? Creeper %)
                           (.getLivingEntities world))))))

(def pre-stalk (ref nil))

(defn stalk-on [player-name]
  (let [player (Bukkit/getPlayer player-name)]
    (.hidePlayer player (ujm))
    (dosync
      (ref-set pre-stalk (.getLocation (ujm))))
    (.teleport (ujm) (.getLocation player))))

(defn stalk-off [player-name]
  (let [player (Bukkit/getPlayer player-name)]
    (.teleport (ujm) @pre-stalk)
    (.showPlayer player (ujm))))

(def plugin-manager* (Bukkit/getPluginManager))
(def plugin* (.getPlugin plugin-manager* "cloft"))

(defn hehehe [f label]
  (let [listener (f)]
    (.registerEvent
      plugin-manager*
      (label c/event-types)
      listener
      (:Normal c/event-priorities)
      plugin*)))

(def first-time (ref true))

(defn enable-plugin [plugin]
    ;(def plugin-desc* (.getDescription plugin*))

    ;(let [listener (get-blocklistener)]
    ;  (.registerEvent
    ;    plugin-manager*
    ;    (:BLOCK_BREAK c/event-types)
    ;    listener
    ;    (:Normal c/event-priorities)
    ;    plugin*)
    ;  (.registerEvent
    ;    plugin-manager*
    ;    (:SIGN_CHANGE c/event-types)
    ;    listener
    ;    (:Normal c/event-priorities)
    ;    plugin*))

  (when @first-time
    ;(hehehe get-player-quit-listener :PLAYER_QUIT)
    (do
      (hehehe player-login-event :PLAYER_LOGIN)
      (hehehe player-move-event :PLAYER_MOVE)
      (hehehe get-player-chat :PLAYER_CHAT)
      (hehehe get-player-interact-entity :PLAYER_INTERACT_ENTITY)
      (hehehe player-level-change-event :PLAYER_LEVEL_CHANGE)
      (hehehe entity-death-event :ENTITY_DEATH)
      (hehehe entity-explode-event :ENTITY_EXPLODE)
      (hehehe get-entity-damage-listener :ENTITY_DAMAGE)
      (hehehe entity-shoot-bow-event :ENTITY_SHOOT_BOW)
      (hehehe entity-target-event :ENTITY_TARGET)
      ;(hehehe entity-explosion-prime-event :ENTITY_EXPLOSION_PRIME)
      (hehehe block-place-event :BLOCK_PLACE)
      (hehehe block-break-event :BLOCK_BREAK)
      (hehehe get-entity-projectile-hit-listener :PROJECTILE_HIT)
      (hehehe vehicle-enter-event :VEHICLE_ENTER)
      (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin* (fn [] (periodically)) 50 50)))
  (dosync
    (ref-set first-time false))
  (lingr "cloft plugin running...")
  (c/log-info "cloft started"))

(defn disable-plugin [plugin]
  (lingr "cloft plugin stopping...")
  (c/log-info "cloft stopped"))

(defn restart []
  (.disablePlugin plugin-manager* plugin*)
  (.enablePlugin plugin-manager* plugin*))
