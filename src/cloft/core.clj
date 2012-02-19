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
   "kldsas" "http://a0.twimg.com/profile_images/1825629510/____normal.png\n"})

(def zombie-players (atom #{}))

(defn zombie-player? [p]
  (boolean (get @zombie-players (.getName p))))

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
        {:room "Arch"
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
(def anotherbed (org.bukkit.Location.  world -237.8704284429714 72.5625 -53.82154923217098 19.349966 -180.45361))

(defn ujm [] (Bukkit/getPlayer "ujm"))

(comment (def jumping-state (atom {})))
(defn jumping? [moveevt]
  (comment (let [name (.getName (.getPlayer moveevt))]
    (if (< (.getY (.getFrom moveevt)) (.getY (.getTo moveevt)))
      (do
        (when (not (get @jumping-state name))
            (swap! jumping-state name true))
        true)
      (do (swap! jumping-state name false)
        false))))
  (< (.getY (.getFrom moveevt)) (.getY (.getTo moveevt))))

(def player-death-locations (atom {}))
(defn player-teleport-machine [evt player]
  (when (and
          (= (.getWorld player) world)
          (< (.distance place2 (.getLocation player)) 1))
    (lingr (str (.getName player) " is teleporting..."))
    (.setTo evt place3))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place1 (.getLocation player)) 1)
          (.isLoaded (.getChunk place4)))
    (lingr (str (.getName player) " is teleporting..."))
    (.setTo evt place4))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place5 (.getLocation player)) 1)
          (.isLoaded (.getChunk place6)))
    (lingr (str (.getName player) " is teleporting..."))
    (.setTo evt place6))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place7 (.getLocation player)) 1))
    (let [death-point (get @player-death-locations (.getName player))]
      (when death-point
        (.isLoaded (.getChunk death-point)) ; for side-effect
        (lingr (str (.getName player) " is teleporting to the last death place..."))
        (.setTo evt death-point)))))

(def super-jump-flags (atom {}))
(defn player-super-jump [evt player]
  (let [name (.getName player)]
    (when (not (get @super-jump-flags name))
      (when (= (.getType (.getItemInHand player)) org.bukkit.Material/FEATHER)
        (swap! super-jump-flags assoc name true)
        (future-call #(do
                   (Thread/sleep 5000)
                   (swap! super-jump-flags assoc name false)))
        (let [x (/ (java.lang.Math/log (.getAmount (.getItemInHand player))) 4)]
          (prn (str "super jump level " x))
          (.setVelocity
            player
            (.add (org.bukkit.util.Vector. 0.0 (double x) 0.0) (.getVelocity player))))))))

(defn get-player-move* [evt]
  (let [player (.getPlayer evt)]
    (when (jumping? evt)
      (player-teleport-machine evt player)
      (player-super-jump evt player))))

(defn arrow-skill-torch [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.setType (.getBlockAt world location) org.bukkit.Material/TORCH)))

(defn arrow-skill-teleport [entity]
  (let [location (.getLocation entity)
        world (.getWorld location)]
    (.teleport (.getShooter entity) location)))

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

(defn broadcast [& strs]
  (.broadcastMessage (Bukkit/getServer) (apply str strs)))

(def jobs (atom {}))

(defn entity-shoot-bow-event* [evt]
  (let [shooter (.getEntity evt)]
    (when (= "ujm" (.getName shooter))
      (future-call #(do
                      (Thread/sleep 100) (.shootArrow (.getEntity evt))
                      (Thread/sleep 300) (.shootArrow (.getEntity evt))
                      (Thread/sleep 500) (.shootArrow (.getEntity evt))
                      )))
    (when (instance? Player shooter)
      (when (= (.getType (.getBlock (.getLocation shooter))) org.bukkit.Material/TORCH)
        (broadcast (.getName shooter) " changed arrow skill to TORCH")
        (swap! jobs assoc (.getName shooter) arrow-skill-torch))
      (when (= (.getType (.getBlock (.getLocation shooter))) org.bukkit.Material/YELLOW_FLOWER)
        (broadcast (.getName shooter) " changed arrow skill to TELEPORT")
        (swap! jobs assoc (.getName shooter) arrow-skill-teleport))
      (when (= (.getType (.getBlock (.getLocation shooter))) org.bukkit.Material/RED_ROSE)
        (broadcast (.getName shooter) " changed arrow skill to FIRE")
        (swap! jobs assoc (.getName shooter) arrow-skill-fire))
      (when (= (.getType (.getBlock (.getLocation shooter))) org.bukkit.Material/SAPLING)
        (broadcast (.getName shooter) " changed arrow skill to TREE")
        (swap! jobs assoc (.getName shooter) arrow-skill-tree)))))

(defn entity-shoot-bow-event []
  (c/auto-proxy [org.bukkit.event.entity.EntityListener] []
                (onEntityShootBow [evt] (entity-shoot-bow-event* evt))))

(defn entity-target-event* [evt]
  (prn evt))

(defn entity-target-event []
  (c/auto-proxy [org.bukkit.event.entity.EntityListener] []
                (onEntityTarget [evt] (entity-target-event* evt))))

(defn get-player-move []
  (c/auto-proxy [org.bukkit.event.player.PlayerListener] []
                  (onPlayerMove [evt] (get-player-move* evt))))

(defn get-player-login-listener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerLogin
      [evt]
      (let [player (.getPlayer evt)]
        (lingr (str (name2icon (.getName player)) "logged in now."))))))

(defn get-player-quit-listener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerQuit
      [evt]
      (lingr (str (name2icon (.getName (.getPlayer evt))) "quitted.")))))

(defn get-player-chat []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerChat
      [evt]
      (let [name (.getName (.getPlayer evt))]
        (lingr (str (name2icon name) (.getMessage evt)))
        (comment (let [creepers (filter #(instance? Creeper %) (.getLivingEntities (.getWorld (.getPlayer evt))))
              your-location (.getLocation (.getPlayer evt))
              distances (map #(.distance (.getLocation %) your-location) creepers)
              close-distances (filter #(< 10 %) distances)]
          (.sendMessage player
                        (if (empty? close-distances)
                          "safe"
                          (str (seq (sort close-distances)))))))))))

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

(defn chicken-touch-player [chicken player]
  ;(.setYaw (.getLocation chicken) (.getYaw (.getLocation player)))
  (.teleport chicken (.getLocation player))
  (.damage player 2 chicken))

(defn entity-touch-player-event []
  (doseq [player (Bukkit/getOnlinePlayers)]
    (let [entities (.getNearbyEntities player 2 2 2)
          chickens (filter #(instance? Chicken %) entities)]
      (for [chicken chickens]
        (chicken-touch-player chicken player)))))

(defn periodically []
  (rechain-entity)
  (entity-touch-player-event)
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
        (instance? Player entity) (.getName entity)
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
        :else (class entity)))

(defn pig-death-event [entity]
  (comment (let [world (.getWorld entity)]
    (.setStorm world true)))
  (let [killer (.getKiller entity)]
    (when killer
      (.setFireTicks killer 100))))

(defn entity-death-event [entity]
  (let [killer (.getKiller entity)]
    (when (instance? Player killer)
      (broadcast (.getName killer) " killed " (entity2name entity)))))

(defn player-death-event [evt player]
  (swap! player-death-locations assoc (.getName player) (.getLocation player))
  (lingr (str (name2icon (.getName player)) (.getDeathMessage evt))))

(defn get-entity-death-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityDeath [evt]
      (let [entity (.getEntity evt)]
        (when (instance? Pig entity)
          (pig-death-event entity))
        (cond
          (instance? Player entity) (player-death-event evt entity)
          (and (instance? LivingEntity entity) (.getKiller entity)) (entity-death-event entity)
          )))))

(defn get-entity-explode-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityExplode [evt]
      (let [entity (.getEntity evt)
            ename (entity2name entity)
            entities-nearby (filter #(instance? Player %) (.getNearbyEntities entity 5 5 5))]
        (when (and ename (not-empty entities-nearby))
          (letfn [(join [xs x]
                    (apply str (interpose x xs)))]
            (lingr (str ename " is exploding near " (join (map #(.getName %) entities-nearby) ", ")))))))))

(defn zombieze [entity]
  (swap! zombie-players conj (.getName entity))
  (.setMaximumAir entity 1)
  (.setRemainingAir entity 1)
  (.sendMessage entity "You turned into a zombie.")
  (lingr (str (name2icon (.getName entity)) "turned into a zombie.")))

(defn potion-weakness [name]
  (.apply
    (org.bukkit.potion.PotionEffect. org.bukkit.potion.PotionEffectType/WEAKNESS 500 1)
    (Bukkit/getPlayer name)))

(defn consume-itemstack [inventory mtype]
  (let [idx (.first inventory mtype)
        itemstack (.getItem inventory idx)
        amount (.getAmount itemstack)]
    (if (= 1 amount)
      (.remove inventory itemstack)
      (.setAmount itemstack (dec amount)))))

(defn arrow-attacks-by-player-event [_ arrow target]
  (let [shooter (.getShooter arrow)]
    (when (and
            (instance? Player shooter)
            (.contains (.getInventory shooter) org.bukkit.Material/WEB))
      (let [msg (str (.getName shooter) " chained " (entity2name target))]
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
  (when (= 0 (rand-int 3))
    (let [location (.getLocation player)
          world (.getWorld location)]
      (doseq [x [-2 -1 0 1 2] z [-2 -1 0 1 2]]
        (let [chicken (.spawn world (.add (.clone location) x 3 z) Chicken)]
          (future-call #(do
                          (Thread/sleep 10000)
                          (.remove chicken))))))))

(defn get-entity-damage-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityDamage [evt]
      (let [target (.getEntity evt)
            attacker (when (instance? EntityDamageByEntityEvent evt)
                       (.getDamager evt))]
        (when (and (instance? Villager target) (instance? EntityDamageByEntityEvent evt))
          (when (instance? Player attacker)
              (lingr (str (name2icon (.getName attacker)) "is attacking a Villager"))
              (.damage attacker (.getDamage evt))))
        (when (and
                (instance? Player target)
                (zombie-player? target)
                (= EntityDamageEvent$DamageCause/DROWNING (.getCause evt))
                (= 0 (rand-int 2)))
          (.setCancelled evt true)
          (.setMaximumAir target 300) ; default maximum value
          (.setRemainingAir target 300)
          (.setHealth target (.getMaxHealth target))
          (swap! zombie-players disj (.getName target))
          (.sendMessage target "You rebirthed as a human."))
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
                (.sendMessage attacker "You made a friend"))))))))


(defn arrow-hit-event [evt entity]
  (when (instance? Player (.getShooter entity))
    (let [skill (get @jobs (.getName (.getShooter entity)))]
      (if skill
        (skill entity)
        (do
          (comment (when (= (.getName (.getShooter entity)) "sugizou")
                     (let [location (.getLocation entity)
                           world (.getWorld location)]
                       (.generateTree world location org.bukkit.TreeType/BIRCH))))
          (when (= (.getName (.getShooter entity)) "kldsas")
            (arrow-skill-torch entity))
          (when (= (.getName (.getShooter entity)) "sbwhitecap")
            (arrow-skill-teleport entity))
          (when (= (.getName (.getShooter entity)) "Sandkat")
            (doseq [near-target (filter
                                  #(instance? LivingEntity %)
                                  (.getNearbyEntities entity 2 2 2))]
              (.damage near-target 3 entity)))
          (when (= (.getName (.getShooter entity)) "ujm")
            (do
              (let [location (.getLocation entity)
                    world (.getWorld location)]
                (.strikeLightningEffect world location))
              (doseq [near-target (filter
                                    #(instance? Monster %)
                                    (.getNearbyEntities entity 10 10 3))]
                (.damage near-target 30 (.getShooter entity))))))))
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
      (hehehe get-player-login-listener :PLAYER_LOGIN)
      (hehehe get-player-move :PLAYER_MOVE)
      (hehehe get-player-chat :PLAYER_CHAT)
      (hehehe get-player-interact-entity :PLAYER_INTERACT_ENTITY)
      (hehehe get-entity-death-listener :ENTITY_DEATH)
      (hehehe get-entity-explode-listener :ENTITY_EXPLODE)
      (hehehe get-entity-damage-listener :ENTITY_DAMAGE)
      (hehehe entity-shoot-bow-event :ENTITY_SHOOT_BOW)
      (hehehe entity-target-event :ENTITY_TARGET)
      (hehehe get-entity-projectile-hit-listener :PROJECTILE_HIT)
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
