(ns cloft.core
  (:require [cljminecraft.core :as c])
  (:import [org.bukkit.event Event Event$Type])
  (:require clj-http.client))

(def NAME-ICON
  {"ujm" "http://www.gravatar.com/avatar/d9d0ceb387e3b6de5c4562af78e8a910.jpg?s=28"
   "sbwhitecap" "http://www.gravatar.com/avatar/198149c17c72f7db3a15e432b454067e.jpg?s=28"
   "Sandkat" "https://twimg0-a.akamaihd.net/profile_images/1584518036/claire2_mini.jpg"})

(defn name2icon [name]
  (get NAME-ICON name name))

(defn lingr [msg]
  (future-call
    #(clj-http.client/post
       "http://lingr.com/api/room/say"
       {:form-params
        {:room "computer_science"
         :bot 'cloft
         :text (str msg)
         :bot_verifier "[FIXME]"}})))

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

(defn get-playerloginlistener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerLogin
      [evt]
      (lingr (str (name2icon (.getName (.getPlayer evt))) " logged in now.")))))

(defn get-playerquitlistener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerQuit
      [evt]
      (lingr (str (name2icon (.getName (.getPlayer evt))) " quitted.")))))

(defn get-player-chat []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerChat
      [evt]
      (let [name (.getName (.getPlayer evt))]
        (lingr (str (name2icon name) " " (.getMessage evt)))))))

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
            (and
              (instance? org.bukkit.entity.Zombie target)
              (not (instance? org.bukkit.entity.PigZombie target))) (d 322)
            (instance? org.bukkit.entity.Villager target) (d 92)
            (instance? org.bukkit.entity.Squid target) (let [player (.getPlayer evt)]
                                                         (.chat player "ikakawaiidesu")
                                                         (.setFoodLevel player 0))
            (instance? org.bukkit.entity.Player target) (.setFoodLevel target (dec (.getFoodLevel target)))
            ))))))

(defn entity2name [entity]
  (cond (instance? org.bukkit.entity.Blaze entity) "Blaze"
        (instance? org.bukkit.entity.CaveSpider entity) "CaveSpider"
        (instance? org.bukkit.entity.Chicken entity) "Chicken"
        ;(instance? org.bukkit.entity.ComplexLivingEntity entity) "ComplexLivingEntity"
        (instance? org.bukkit.entity.Cow entity) "Cow"
        ;(instance? org.bukkit.entity.Creature entity) "Creature"
        (instance? org.bukkit.entity.Creeper entity) "Creeper"
        (instance? org.bukkit.entity.EnderDragon entity) "EnderDragon"
        (instance? org.bukkit.entity.Enderman entity) "Enderman"
        ;(instance? org.bukkit.entity.Flying entity) "Flying"
        (instance? org.bukkit.entity.Ghast entity) "Ghast"
        (instance? org.bukkit.entity.Giant entity) "Giant"
        ;(instance? org.bukkit.entity.HumanEntity entity) "HumanEntity"
        (instance? org.bukkit.entity.MagmaCube entity) "MagmaCube"
        ;(instance? org.bukkit.entity.Monster entity) "Monster"
        (instance? org.bukkit.entity.MushroomCow entity) "MushroomCow"
        ;(instance? org.bukkit.entity.NPC entity) "NPC"
        (instance? org.bukkit.entity.Pig entity) "Pig"
        (instance? org.bukkit.entity.PigZombie entity) "PigZombie"
        ;(instance? org.bukkit.entity.Player entity) "Player"
        (instance? org.bukkit.entity.Sheep entity) "Sheep"
        (instance? org.bukkit.entity.Silverfish entity) "Silverfish"
        (instance? org.bukkit.entity.Skeleton entity) "Skeleton"
        (instance? org.bukkit.entity.Slime entity) "Slime"
        (instance? org.bukkit.entity.Snowman entity) "Snowman"
        (instance? org.bukkit.entity.Spider entity) "Spider"
        (instance? org.bukkit.entity.Squid entity) "Squid"
        (instance? org.bukkit.entity.Villager entity) "Villager"
        ;(instance? org.bukkit.entity.WaterMob entity) "WaterMob"
        (instance? org.bukkit.entity.Wolf entity) "Wolf"
        (instance? org.bukkit.entity.Zombie entity) "Zombie"
        (instance? org.bukkit.entity.TNTPrimed entity) "TNT"
        :else (class entity)))

(defn pig-death-event [entity]
  (comment (let [world (.getWorld entity)]
    (.setStorm world true)))
  (.setFireTicks (.getKiller entity) 100))

(defn entity-death-event [entity]
  (lingr
    (str (name2icon (.getName (.getKiller entity))) " killed " (entity2name entity))))

(defn get-entity-death-listener []
  (c/auto-proxy
    [org.bukkit.event.entity.EntityListener] []
    (onEntityDeath [evt]
      (let [entity (.getEntity evt)]
        (when (instance? org.bukkit.entity.Pig entity)
          (pig-death-event entity))
        (cond
          (instance? org.bukkit.entity.Player entity) (lingr (str (name2icon (.getName entity)) " " (.getDeathMessage evt)))
          (and (instance? org.bukkit.entity.LivingEntity entity) (.getKiller entity)) (entity-death-event entity)
          )))))

(defn get-entity-explode-listener []
  (c/auto-proxy
    [org.bukkit.event.entity.EntityListener] []
    (onEntityExplode [evt]
      (let [entity (.getEntity evt)]
        (lingr (str (entity2name entity) " is exploding"))))))

(defn get-entity-damage-listener []
  (c/auto-proxy
    [org.bukkit.event.entity.EntityListener] []
    (onEntityDamage [evt]
      (let [entity (.getEntity evt)]
        (when (and
                (instance? org.bukkit.entity.Villager entity)
                (instance? org.bukkit.event.entity.EntityDamageByEntityEvent evt))
          (let [attacker (.getDamager evt)]
            (when (instance? org.bukkit.entity.Player attacker)
              (lingr (str (name2icon (.getName attacker)) " is attacking a Villager"))
              (.damage attacker (.getDamage evt)))))))))

(defn get-entity-projectilehit-listener []
  (c/auto-proxy
    [org.bukkit.event.entity.EntityListener] []
    (onProjectileHit [evt]
      (let [entity (.getEntity evt)]
        (when (instance? org.bukkit.entity.Fireball entity)
          (.setYield entity 0.0))))))

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
      (hehehe get-playerloginlistener :PLAYER_LOGIN)
      (hehehe get-playerquitlistener :PLAYER_QUIT)
      (hehehe get-player-chat :PLAYER_CHAT)
      (hehehe get-player-interact-entity :PLAYER_INTERACT_ENTITY)
      (hehehe get-entity-death-listener :ENTITY_DEATH)
      (hehehe get-entity-explode-listener :ENTITY_EXPLODE)
      (hehehe get-entity-damage-listener :ENTITY_DAMAGE)
      (hehehe get-entity-projectilehit-listener :PROJECTILE_HIT))
  (lingr "server running...")
  (c/log-info "cloft started"))

(defn disable-plugin [plugin]
  (lingr "server stopping...")
  (c/log-info "cloft stopped"))
