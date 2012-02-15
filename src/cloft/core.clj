(ns cloft.core
  (:require [cljminecraft.core :as c])
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

(defn- zombie-player? [p]
  (boolean (get @zombie-players (.getName p))))

(defn- name2icon [name]
  (get NAME-ICON name (str name " ")))

(def BOT-VERIFIER (apply str (drop-last (slurp "bot_verifier.txt"))))
(prn (str "'" BOT-VERIFIER "'"))

(defn- lingr [msg]
  (future-call
    #(clj-http.client/post
       "http://lingr.com/api/room/say"
       {:form-params
        {:room "computer_science"
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

(defn- swap-entity [target klass]
  (let [location (.getLocation target)
        world (.getWorld target)]
    (.remove target)
    (.spawn world location klass)))

(defn- consume-item [player]
  (let [itemstack (.getItemInHand player)]
    (.setAmount itemstack (dec (.getAmount itemstack)))))

(defn- get-player-login-listener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerLogin
      [evt]
      (let [player (.getPlayer evt)]
        (lingr (str (name2icon (.getName player)) "logged in now."))
        (.sendMessage player "[UPDATE] You can turn into a zombie.")))))

(defn- get-player-quit-listener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerQuit
      [evt]
      (lingr (str (name2icon (.getName (.getPlayer evt))) "quitted.")))))

(defn- get-player-chat []
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

(defn- touch-player [target]
  (.setFoodLevel target (dec (.getFoodLevel target))))

(defn- get-player-interact-entity []
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
            ; right-click spider -> cat record
            (instance? Spider target) (d 2257)
            ; right-click squid -> chat and hungry
            (instance? Squid target) (let [player (.getPlayer evt)]
                                       (.chat player "ikakawaiidesu")
                                       (.setFoodLevel player 0))
            ; right-click player -> makes it hungry
            (instance? Player target) (touch-player)))))))

; internal
(defn- zombie-player-periodically [zplayer]
  (when (= 15 (.getLightLevel (.getBlock (.getLocation zplayer))))
    (.setFireTicks zplayer 100))
  (when (= 0 (rand-int 2))
    (.setFoodLevel zplayer (dec (.getFoodLevel zplayer)))))

(defn- periodically []
  (seq (map zombie-player-periodically
            (filter zombie-player? (org.bukkit.Bukkit/getOnlinePlayers))))
  nil)

(defn- entity2name [entity]
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
        ;(instance? Player entity) "Player"
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

(defn- pig-death-event [entity]
  (comment (let [world (.getWorld entity)]
    (.setStorm world true)))
  (.setFireTicks (.getKiller entity) 100))

(defn- entity-death-event [entity]
  (lingr
    (str (name2icon (.getName (.getKiller entity))) "killed " (entity2name entity))))

(defn- get-entity-death-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityDeath [evt]
      (let [entity (.getEntity evt)]
        (when (instance? Pig entity)
          (pig-death-event entity))
        (cond
          (instance? Player entity) (lingr (str (name2icon (.getName entity)) (.getDeathMessage evt)))
          (and (instance? LivingEntity entity) (.getKiller entity)) (entity-death-event entity)
          )))))

(defn- get-entity-explode-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityExplode [evt]
      (let [entity (.getEntity evt)]
        (lingr (str (entity2name entity) " is exploding"))))))

(defn- zombieze [entity]
  (swap! zombie-players conj (.getName entity))
  (.setMaximumAir entity 1)
  (.setRemainingAir entity 1)
  (.sendMessage entity "You turned into a zombie.")
  (lingr (str (name2icon (.getName entity)) "turned into a zombie.")))

(defn- get-entity-damage-listener []
  (c/auto-proxy
    [EntityListener] []
    (onEntityDamage [evt]
      (let [entity (.getEntity evt)]
        (when (and (instance? Villager entity) (instance? EntityDamageByEntityEvent evt))
          (let [attacker (.getDamager evt)]
            (when (instance? Player attacker)
              (lingr (str (name2icon (.getName attacker)) "is attacking a Villager"))
              (.damage attacker (.getDamage evt)))))
        (when (and
                (instance? Player entity)
                (zombie-player? entity)
                (= EntityDamageEvent$DamageCause/DROWNING (.getCause evt))
                (= 0 (rand-int 2)))
          (.setCancelled evt true)
          (.setMaximumAir entity 300) ; default maximum value
          (.setRemainingAir entity 300)
          (.setHealth entity (.getMaxHealth entity))
          (swap! zombie-players disj (.getName entity))
          (.sendMessage entity "You rebirthed as a human."))
        (when (and (instance? Player entity) (instance? EntityDamageByEntityEvent evt))
          (let [attacker (.getDamager evt)]
            (when (and (instance? Zombie attacker) (not (instance? PigZombie attacker)))
              (if (zombie-player? entity)
                (.setCancelled evt true)
                (zombieze entity)))
            (when (and (instance? Player attacker) (zombie-player? attacker))
              (do
                (zombieze entity)
                (.sendMessage attacker "You made a friend")))))))))

(defn- get-entity-projectile-hit-listener []
  (c/auto-proxy
    [EntityListener] []
    (onProjectileHit [evt]
      (let [entity (.getEntity evt)]
        (cond
          (instance? Fireball entity) (.setYield entity 0.0)
          ;(instance? Snowball entity) (.strikeLightning (.getWorld entity) (.getLocation entity))
          )))))

(defn enable-plugin [plugin]
    (def plugin* plugin)
    (def server* (.getServer plugin*))
    (def plugin-manager* (.getPluginManager server* ))
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
    (letfn [(hehehe [f label]
              (let [listener (f)]
                (.registerEvent
                  plugin-manager*
                  (label c/event-types)
                  listener
                  (:Normal c/event-priorities)
                  plugin*)))]
      (hehehe get-player-login-listener :PLAYER_LOGIN)
      ;(hehehe get-player-quit-listener :PLAYER_QUIT)
      (hehehe get-player-chat :PLAYER_CHAT)
      (hehehe get-player-interact-entity :PLAYER_INTERACT_ENTITY)
      (hehehe get-entity-death-listener :ENTITY_DEATH)
      (hehehe get-entity-explode-listener :ENTITY_EXPLODE)
      (hehehe get-entity-damage-listener :ENTITY_DAMAGE)
      (hehehe get-entity-projectile-hit-listener :PROJECTILE_HIT))
  (.scheduleSyncRepeatingTask
    (org.bukkit.Bukkit/getScheduler)
    plugin*
    periodically
    50
    50)
  (lingr "server running...")
  (c/log-info "cloft started"))

(defn disable-plugin [plugin]
  (lingr "server stopping...")
  (c/log-info "cloft stopped"))
