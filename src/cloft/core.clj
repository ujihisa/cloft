(ns cloft.core
  (:require [cljminecraft.core :as c])
  (:import [org.bukkit.event Event Event$Type])
  (:require clj-http.client))

(defn lingr [msg]
  (clj-http.client/post "http://lingr.com/api/room/say"
               {:form-params
                {:room "computer_science"
                 :bot 'cloft
                 :text (str msg)
                 :bot_verifier "[FIXME]"}}))

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
      (lingr (str (.getName (.getPlayer evt)) " logged in now.")))))

(defn get-playerquitlistener []
  (c/auto-proxy
    [org.bukkit.event.player.PlayerListener] []
    (onPlayerQuit
      [evt]
      (lingr (str (.getName (.getPlayer evt)) " quitted.")))))

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
        :else (class entity)))

(defn pig-death-event [entity]
  (comment (let [world (.getWorld entity)]
    (.setStorm world true)))
  (.setFireTicks (.getKiller entity) 100))

(defn entity-death-event [entity]
  (lingr
    (str (.getName (.getKiller entity)) " killed " (entity2name entity))))

(defn get-entity-death-listener []
  (c/auto-proxy
    [org.bukkit.event.entity.EntityListener] []
    (onEntityDeath [evt]
      (let [entity (.getEntity evt)]
        (when (instance? org.bukkit.entity.Pig entity)
          (pig-death-event entity))
        (cond
          (instance? org.bukkit.entity.Player entity) (lingr (.getDeathMessage evt))
          (and (instance? org.bukkit.entity.LivingEntity entity) (.getKiller entity)) (entity-death-event entity)
          )))))

(defn get-entity-projectilehit-listener []
  (c/auto-proxy
    [org.bukkit.event.entity.EntityListener] []
    (onProjectileHit [evt]
      (let [entity] (.getEntity evt)
        (prn entity)))))

(defn enable-plugin [plugin]
    (def plugin* plugin)
    (def server* (.getServer plugin*))
    (def plugin-manager* (.getPluginManager server* ))
    (def plugin-desc* (.getDescription plugin*))

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
    (let [listener (get-playerloginlistener)]
      (.registerEvent
        plugin-manager*
        (:PLAYER_LOGIN c/event-types)
        listener
        (:Normal c/event-priorities)
        plugin*))
    (let [listener (get-playerquitlistener)]
      (.registerEvent
        plugin-manager*
        (:PLAYER_QUIT c/event-types)
        listener
        (:Normal c/event-priorities)
        plugin*))
    (let [listener (get-entity-death-listener)]
      (.registerEvent
        plugin-manager*
        (:ENTITY_DEATH c/event-types)
        listener
        (:Normal c/event-priorities)
        plugin*))
    (let [listener (get-entity-projectilehit-listener)]
      (.registerEvent
        plugin-manager*
        (:PROJECTILE_HIT c/event-types)
        listener
        (:Normal c/event-priorities)
        plugin*))
  (c/log-info "cloft started"))

(defn disable-plugin [plugin]
  (c/log-info "cloft stopped"))
