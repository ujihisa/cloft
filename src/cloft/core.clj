(ns cloft.core
  (:require [cloft.cloft :as c])
  ;(:require [clojure.core.match :as m])
  (:require [swank.swank])
  (:require [clojure.string :as s])
  (comment (:import [org.bukkit.command CommandExecuter CommandSender Command]))
  (:import [org.bukkit Bukkit Material])
  (:import [org.bukkit.entity Animals Arrow Blaze Boat CaveSpider Chicken
            ComplexEntityPart ComplexLivingEntity Cow Creature Creeper Egg
            EnderCrystal EnderDragon EnderDragonPart Enderman EnderPearl
            EnderSignal ExperienceOrb Explosive FallingSand Fireball Fish
            Flying Ghast Giant HumanEntity IronGolem Item LightningStrike LivingEntity
            MagmaCube Minecart Monster MushroomCow NPC Painting Pig PigZombie
            Player PoweredMinecart Projectile Sheep Silverfish Skeleton Slime
            SmallFireball Snowball Snowman Spider Squid StorageMinecart
            ThrownPotion TNTPrimed Vehicle Villager WaterMob Weather Wolf
            Zombie Ocelot])
  (:import [org.bukkit.event.entity EntityDamageByEntityEvent
            EntityDamageEvent$DamageCause])
  (:import [org.bukkit.potion Potion PotionEffect PotionEffectType])
  (:import [org.bukkit.inventory ItemStack])
  (:import [org.bukkit.util Vector])
  (:import [org.bukkit.event.block Action]))


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
;   [Listener] []
;   (onBlockBreak [evt] (if (.isCancelled evt) nil (block-break evt)))
;   (onSignChange [evt] (if (.isCancelled evt) nil (sign-change evt))))
;  )

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

(def player-death-locations (atom {}))
(def last-vertical-shots (atom {}))

(defn player-teleport-machine [evt player]
  (when (and
          (= (.getWorld player) world)
          (< (.distance place2 (.getLocation player)) 1))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place3))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place1 (.getLocation player)) 1)
          (.isLoaded (.getChunk place4)))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place4))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place5 (.getLocation player)) 1)
          (.isLoaded (.getChunk place6)))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place6))
  (when (and
          (= (.getWorld player) world)
          (or
            (< (.distance place9 (.getLocation player)) 1)
            (< (.distance place10 (.getLocation player)) 1))
          (.isLoaded (.getChunk place-main)))
    (c/lingr (str (.getDisplayName player) " is teleporting..."))
    (.setTo evt place-main))
  (when (and
          (= (.getWorld player) world)
          (< (.distance place7 (.getLocation player)) 1))
    (let [death-point (get @player-death-locations (.getDisplayName player))]
      (when death-point
        (.isLoaded (.getChunk death-point)) ; for side-effect
        (c/lingr (str (.getDisplayName player) " is teleporting to the last death place..."))
        (.setTo evt death-point)))))


(defn player-super-jump [evt player]
  (let [name (.getDisplayName player)]
    (when (= (.getType (.getItemInHand player)) Material/FEATHER)
      (let [amount (.getAmount (.getItemInHand player))
            x (if (.isSprinting player) (* amount 2) amount)
            x2 (/ (java.lang.Math/log x) 2) ]
        (c/lingr (str name " is super jumping with level " x))
        (c/consume-itemstack (.getInventory player) Material/FEATHER)
        (.setVelocity
          player
          (.add (Vector. 0.0 x2 0.0) (.getVelocity player)))))))

(def sanctuary [(org.bukkit.Location. world 45 30 -75)
                (org.bukkit.Location. world 84 90 -44)])
(def sanctuary-players (atom #{}))

(defn event [evt]
  (prn evt))

(defn entity-combust-event [evt]
  (.setCancelled evt true))

(def bossbattle-player nil)
(defn player-move-event [evt]
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
    #_(let [name (.getDisplayName player)]
      (if (get @sanctuary-players name)
        (when (not (c/location-bound? (.getLocation player) (first sanctuary) (second sanctuary)))
          (swap! sanctuary-players disj name))
        (when (c/location-bound? (.getLocation player) (first sanctuary) (second sanctuary))
          (c/broadcast name " entered the sanctuary.")
          (swap! sanctuary-players conj name)))
      (when (and (= (.getWorld player) world) (< (.distance (org.bukkit.Location. world 70 66 -58) (.getLocation player)) 1))
        (if (c/jumping? evt)
          (when (not= @bossbattle-player player)
            (c/broadcast player " entered the boss' room!")
            (dosync
              (ref-set bossbattle-player player)))
          (do
            (.sendMessage player "You can't leave")
            (.setTo evt (.add (.getFrom evt) 0 0.5 0))))))
    (when (c/jumping? evt)
      (player-teleport-machine evt player))
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
    (if (and
            (nil? (#{Material/AIR Material/CHEST Material/FURNACE Material/BURNING_FURNACE} (.getType block)))
            (not (.isLiquid block)))
      (let [shooter-loc (.getLocation (.getShooter entity))]
        (.setType (.getBlock shooter-loc) (.getType block))
        (.setType block Material/AIR)
        (.remove entity)
        (.teleport (.getShooter entity) (.add shooter-loc 0 1 0)))
      (.sendMessage (.getShooter entity) "PULL failed"))))

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
      (.setFireTicks target 200))))

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

(def arrow-skill (atom {}))
(defn arrow-skill-of [player]
  (get @arrow-skill (.getDisplayName player)))

(def counter-skill (atom {}))
(defn counter-skill-of [player]
  (get @counter-skill (.getDisplayName player)))

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

(defn enough-previous-shots-by-players? [triggered-by threshold]
  (let [locs (vals @last-vertical-shots) ]
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
                        (Thread/sleep 500) (.shootArrow (.getEntity evt))
                        ))))
      (when (arrow-velocity-vertical? (.getProjectile evt))
        (prn last-vertical-shots)
        (swap! last-vertical-shots assoc (.getDisplayName shooter) (.getLocation shooter))
        (prn last-vertical-shots)
        (future-call #(let [shooter-name (.getDisplayName shooter)]
                        (check-and-thunder shooter)
                        (Thread/sleep 1000)
                        (swap! last-vertical-shots dissoc shooter-name))))
      (when (= arrow-skill-shotgun (arrow-skill-of shooter))
        (doseq [_ (range 1 80)]
          (let [rand1 (fn [] (* 0.8 (- (rand) 0.5)))
                arrow (.launchProjectile shooter Arrow)]
            (.setVelocity arrow (.getVelocity (.getProjectile evt)))
            (c/add-velocity arrow (rand1) (rand1) (rand1))))))))

(defn entity-target-event [evt]
  (let [entity (.getEntity evt)]
    (when (instance? Creeper entity)
      (when-let [target (.getTarget evt)]
        (let [block (.getBlock (.getLocation entity))]
          (when (= Material/AIR (.getType block))
            (.setType block Material/FIRE)))
        (when (instance? Player target)
          (c/broadcast "Takumi is watching " (.getDisplayName target)))))))

(defn entity-explosion-prime-event [evt]
  nil)

;(defn build-long [block block-against]
;  (comment (when (= (.getType block) (.getType block-against))
;    (let [world (.getWorld block)
;          loc (.getLocation block)
;          diff (.subtract (.clone loc) (.getLocation block-against))]
;      (doseq [m (range 1 10)]
;        (let [newblock (.getBlockAt
;                         world
;                         (.add (.clone loc) (.multiply (.clone diff) (double m))))]
;          (when (= (.getType newblock) Material/AIR)
;            (.setType newblock (.getType block)))))))))
;
(defn arrow-skillchange [player block block-against]
  (when (and
          (every? identity (map
                             #(=
                                (.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                                Material/STONE)
                             [0 0 -1 1] [-1 1 0 0]))
          (every? identity (map
                             #(not=
                                (.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                                Material/STONE)
                             [-1 1 0 0] [-1 1 0 0])))
    (let [table {Material/GLOWSTONE ['strong "STRONG"]
                 Material/TNT [arrow-skill-explosion "EXPLOSION"]
                 Material/TORCH [arrow-skill-torch "TORCH"]
                 Material/REDSTONE_TORCH_ON [arrow-skill-pull "PULL"]
                 Material/YELLOW_FLOWER [arrow-skill-teleport "TELEPORT"]
                 Material/RED_ROSE [arrow-skill-fire "FIRE"]
                 Material/SAPLING [arrow-skill-tree "TREE"]
                 Material/WORKBENCH [arrow-skill-ore "ORE"]
                 Material/BROWN_MUSHROOM ['fly "FLY"]
                 Material/CACTUS [arrow-skill-shotgun "SHOTGUN"]
                 Material/RAILS ['cart "CART"]
                 Material/BOOKSHELF ['mobchange "MOBCHANGE"]
                 Material/SNOW_BLOCK [arrow-skill-ice "ICE"]}]
      (if-let [skill-name (table (.getType block))]
        (c/broadcast (.getDisplayName player) " changed arrow-skill to " (last skill-name))
        (swap! arrow-skill assoc (.getDisplayName player) (first skill-name))))))

(defn block-place-event [evt]
  (let [block (.getBlock evt)]
    (comment (.spawn (.getWorld block) (.getLocation block) Pig))
    (let [player (.getPlayer evt)]
      (arrow-skillchange player block (.getBlockAgainst evt))
      (comment (prn (vector-from-to block player))
               (.setVelocity player (vector-from-to player block))
               (doseq [entity (.getNearbyEntities player 4 4 4)]
                 (.setVelocity entity (vector-from-to entity block)))))
    ;(build-long block (.getBlockAgainst evt))
    (comment (when (c/location-bound? (.getLocation block) (first sanctuary) (second sanctuary))
      (.setCancelled evt true)))))

(defn player-login-event [evt]
  (let [player (.getPlayer evt)]
    (comment (when (= (.getDisplayName player) "Player")
      (.setDisplayName player "raa0121")))
    (future-call #(do
                    (Thread/sleep 1000)
                    (let [ip (.. player getAddress getAddress getHostAddress)]
                      (if (or
                            (= "10.0" (apply str (take 4 ip)))
                            (= "127.0.0.1" ip)
                            (= "219.111.70.24" ip)
                            (= "113.151.154.229" ip))
                        (do
                          (.setOp player true)
                          (prn [player 'is 'op]))
                        (.setOp player false)))))
    (c/lingr (str (name2icon (.getDisplayName player)) "logged in now."))))

;(defn c/get-player-quit-listener []
;  (c/auto-proxy
;    [Listener] []
;    (onPlayerQuit
;      [evt]
;      (c/lingr (str (name2icon (.getDisplayName (.getPlayer evt))) "quitted.")))))

(defn player-chat-event [evt]
  (let [name (.getDisplayName (.getPlayer evt))]
    (c/lingr (str (name2icon name) (.getMessage evt)))))

(defn player-drop-item-event [evt]
  (let [player (.getPlayer evt)]
    (when false
      (.sendMessage player "feather jump!")
      (c/add-velocity player 0 0.5 0))))

(defn touch-player [target]
  (.setFoodLevel target (dec (.getFoodLevel target))))

(defn teleport-machine? [loc]
  (=
    (for [x [-1 0 1] z [-1 0 1]]
      (if (and (= x 0) (= z 0))
        'any
        (.getType (.getBlock (.add (.clone loc) x 0 z)))))
    (list Material/GLOWSTONE Material/GLOWSTONE Material/GLOWSTONE
          Material/GLOWSTONE 'any Material/GLOWSTONE
          Material/GLOWSTONE Material/GLOWSTONE Material/GLOWSTONE)))

(defn teleport-up [entity block]
  (when (#{Material/STONE_PLATE Material/WOOD_PLATE} (.getType block))
    (let [entity-loc (.getLocation entity)
          loc (.add (.getLocation block) 0 -1 0)]
      (when (teleport-machine? loc)
        (when (instance? Player entity)
          (.sendMessage entity "teleport up!"))
        (future-call #(let [newloc (.add (.getLocation entity) 0 30 0)]
                        (Thread/sleep 10)
                        (condp = (.getType block)
                          Material/STONE_PLATE (.teleport entity newloc)
                          Material/WOOD_PLATE (c/add-velocity entity 0 1.5 0))
                        (.playEffect (.getWorld entity-loc) (.add entity-loc 0 1 0) org.bukkit.Effect/BOW_FIRE nil)
                        (.playEffect (.getWorld newloc) newloc org.bukkit.Effect/BOW_FIRE nil)
                        (.playEffect (.getWorld entity-loc) entity-loc org.bukkit.Effect/ENDER_SIGNAL nil)
                        (.playEffect (.getWorld newloc) newloc org.bukkit.Effect/ENDER_SIGNAL nil)))))))

(defn entity-interact-physical-event [evt entity]
  (teleport-up entity (.getBlock evt)))

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
      (.setType (.getBlock (.add (.getLocation door) 0 10 0)) Material/IRON_DOOR)
      ))))

(defn player-interact-event [evt]
  (let [player (.getPlayer evt)]
    (cond
      #_(
        (and
          (= Action/LEFT_CLICK_BLOCK (.getAction evt))
          (= Material/WOODEN_DOOR (.getType (.getClickedBlock evt))))
        (elevator player (.getClickedBlock evt))
      )

      (= (.getAction evt) Action/PHYSICAL)
      (teleport-up player (.getClickedBlock evt))
      (and
        (= (.getAction evt) Action/RIGHT_CLICK_BLOCK)
        (= (.getType (.getClickedBlock evt)) Material/CAKE_BLOCK))
      (if-let [death-point (get @player-death-locations (.getDisplayName player))]
        (do
          (.getChunk death-point)
          (c/broadcast (str (.getDisplayName player) " is teleporting to the last death place..."))
          (.teleport player death-point))
        (.sendMessage player "You didn't die yet."))
      (and
        (= (.. player (getItemInHand) (getType)) Material/GLASS_BOTTLE)
        (or
          (= (.getAction evt) Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) Action/RIGHT_CLICK_BLOCK)))
      (.setItemInHand player (.toItemStack (Potion. (rand-nth c/potion-types))  (rand-nth [1 1 2 3 5])))
      (and
        (= (.. player (getItemInHand) (getType)) Material/GOLD_SWORD)
        (= (.getHealth player) (.getMaxHealth player))
        (or
          (= (.getAction evt) Action/LEFT_CLICK_AIR)
          (= (.getAction evt) Action/LEFT_CLICK_BLOCK)))
      (if (empty? (.getEnchantments (.getItemInHand player)))
        (let [snowball (.launchProjectile player Snowball)]
          (swap! special-snowball-set conj snowball)
          (.setVelocity snowball (.multiply (.getVelocity snowball) 3)))
        (let [arrow (.launchProjectile player Arrow)]
          (.setVelocity arrow (.multiply (.getVelocity arrow) 3))))
      (and
        (= (.. evt (getMaterial)) Material/MILK_BUCKET)
        (or
          (= (.getAction evt) Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) Action/RIGHT_CLICK_BLOCK)))
      (do
        (.damage player 8)
        (.sendMessage player "you drunk milk"))
      (and
        (= (.. evt (getMaterial)) Material/FEATHER)
        (or
          (= (.getAction evt) Action/RIGHT_CLICK_AIR)
          (= (.getAction evt) Action/RIGHT_CLICK_BLOCK)))
      (player-super-jump evt player))))

(defn player-drop-item-event [evt]
  (let [item (.getItemDrop evt)
        itemstack (.getItemStack item)
        table {Material/RAW_BEEF [Material/ROTTEN_FLESH Material/COOKED_BEEF]
               Material/RAW_CHICKEN [Material/ROTTEN_FLESH Material/COOKED_CHICKEN]
               Material/RAW_FISH [Material/RAW_FISH Material/COOKED_FISH]
               Material/PORK [Material/ROTTEN_FLESH Material/GRILLED_PORK]
               Material/APPLE [Material/APPLE Material/GOLDEN_APPLE]
               Material/ROTTEN_FLESH [Material/ROTTEN_FLESH Material/COAL]}]
    (cond
      (table (.getType itemstack))
      (future-call #(let [pair (table (.getType itemstack))]
                      (Thread/sleep 5000)
                      (when (not (.isDead item))
                        (let [new-item-material
                              (if (#{Material/FURNACE Material/BURNING_FURNACE}
                                      (.getType (.getBlock (.add (.getLocation item) 0 -1 0))))
                                (last pair)
                                (first pair))]
                          (.dropItem (.getWorld item) (.getLocation item) (ItemStack. new-item-material (.getAmount itemstack))))
                        (.remove item)))))))

(defn player-interact-entity-event [evt]
  (let [target (.getRightClicked evt)]
    (letfn [(d
              ([n] (d n 1))
              ([n ^Byte m]
                (.dropItem (.getWorld target)
                (.getLocation target)
                (ItemStack. (int n) (int 1) (short 0) (Byte. m)))))]
      (cond
        (= Material/STRING (.getType (.getItemInHand (.getPlayer evt))))
        (let [player (.getPlayer evt)]
          (c/consume-item player)
          (.setPassenger player target)
          (.setCancelled evt true)
          (cond
            (or
              (instance? Pig target)
              (instance? Chicken target))
            (.setAllowFlight player true)

            (instance? Player target)
            (future-call #(do
              (Thread/sleep 10000)
              (when (= player (.getPassenger player))
                (.setPassenger player nil))))))

        (and (= (.getType (.getItemInHand (.getPlayer evt))) Material/COAL)
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
             (= (.getTypeId (.getItemInHand (.getPlayer evt))) 296))
        (do
          (c/swap-entity target Pig)
          (c/consume-item (.getPlayer evt)))
        ; give zombeef to pig -> zombie pigman
        (and (instance? Pig target)
             (= (.getTypeId (.getItemInHand (.getPlayer evt))) 367))
        (do
          (c/swap-entity target PigZombie)
          (c/consume-item (.getPlayer evt)))
        ; right-click chicken -> rail
        (instance? Chicken target) (d 66)
        ; right-click pig -> cocoa
        (instance? Pig target) (d 351 3)
        ; right-click cow -> charcoal
        (instance? Cow target) (d 263)
        ; right-click villager -> cake
        (instance? Villager target) (d 92)
        ; right-click creeper -> gunpowder
        (instance? Creeper target) (d 289)

        (and (instance? Zombie target) (not (instance? PigZombie target)))
        (let [player (.getPlayer evt)]
          (if (= Material/ROTTEN_FLESH (.getType (.getItemInHand player)))
            (do
              (when (= 0 (rand-int 20))
                (.spawn (.getWorld target) (.getLocation target) Giant)
                (c/broadcast "Giant!"))
              (c/consume-item player)
              (.remove target))
            ; right-click zombie -> zombeef
            (d 367)))

        ; right-click skelton -> arrow
        (instance? Skeleton target) (d 262)
        ; right-click spider -> string
        (instance? Spider target) (d 287)
        ; right-click squid -> chat and hungry
        (instance? Squid target)
        (let [player (.getPlayer evt)]
          (.chat player "ikakawaiidesu")
          (.setFoodLevel player 0))
        ; right-click player -> makes it hungry
        (instance? Player target) (touch-player target)))))

(defn player-level-change-event [evt]
  (when (< (.getOldLevel evt) (.getNewLevel evt))
    (c/broadcast "Level up! "(.getDisplayName (.getPlayer evt)) " is Lv" (.getNewLevel evt))))

; internal
(defn zombie-player-periodically [zplayer]
  (when (= 15 (.getLightLevel (.getBlock (.getLocation zplayer))))
    (.setFireTicks zplayer 100))
  (.setFoodLevel zplayer (dec (.getFoodLevel zplayer))))

(comment (def chain (atom {:entity nil :loc nil})))

(defn chain-entity [entity shooter]
  (comment (swap! chain assoc :entity entity :loc (.getLocation entity)))
  (let [block (.getBlock (.getLocation entity))]
    (when (not (.isLiquid block))
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
  (seq (map zombie-player-periodically
            (filter zombie-player? (Bukkit/getOnlinePlayers))))
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
      (when (instance? Giant entity)
        (.setDroppedExp evt 1000))
      (when (instance? Creeper entity)
        (.setDroppedExp evt 20))
      (when (and
              (= 0 (rand-int 2))
              (instance? CaveSpider entity))
        (.dropItem (.getWorld entity) (.getLocation entity) (ItemStack. Material/GOLD_SWORD)))
      (.setDroppedExp evt (int (* (.getDroppedExp evt) (/ 15 (.getHealth killer)))))
      (c/broadcast (.getDisplayName killer) " killed " (c/entity2name entity) " (exp: " (.getDroppedExp evt) ")"))))

(defn player-death-event [evt player]
  (swap! player-death-locations assoc (.getDisplayName player) (.getLocation player))
  (c/lingr (str (name2icon (.getDisplayName player)) (.getDeathMessage evt))))

(defn entity-death-event [evt]
  (let [entity (.getEntity evt)]
    (cond
      (instance? Pig entity) (pig-death-event entity)
      (instance? Player entity) (player-death-event evt entity)
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
  (if (c/location-bound? (.getLocation entity) (first sanctuary) (second sanctuary))
    (prn 'cancelled)
    (let [loc (.getLocation entity)]
      (.setType (.getBlock loc) Material/PUMPKIN)
      (c/broadcast "break the bomb before it explodes!")
      (future-call #(do
                      (Thread/sleep 7000)
                      (c/broadcast "zawa...")
                      (Thread/sleep 1000)
                      (when (= (.getType (.getBlock loc)) Material/PUMPKIN)
                        (.createExplosion (.getWorld loc) loc 6 true)))))))

(defn creeper-explosion-3 [evt entity]
  (.setCancelled evt true)
  (.createExplosion (.getWorld entity) (.getLocation entity) 0)
  )

(def creeper-explosion-idx (atom 0))
(defn current-creeper-explosion []
  (get [(fn [_ _] nil)
        creeper-explosion-1
        creeper-explosion-2
        creeper-explosion-3
        ] (rem @creeper-explosion-idx 4)))

(defn entity-explode-event [evt]
  (let [entity (.getEntity evt)]
    (when entity
      (let [ename (c/entity2name entity)
            entities-nearby (filter #(instance? Player %) (.getNearbyEntities entity 5 5 5))]
        (cond
          (c/location-bound? (.getLocation entity) (first sanctuary) (second sanctuary))
          (.setCancelled evt true)

        (instance? TNTPrimed entity)
        (prn ['TNT entity])

        (instance? Creeper entity)
        (do
          ((current-creeper-explosion) evt entity)
          (swap! creeper-explosion-idx inc))

        (and ename (not-empty entities-nearby) (not (instance? EnderDragon entity)))
        (letfn [(join [xs x]
                  (apply str (interpose x xs)))]
          (c/lingr (str ename " is exploding near " (join (map #(.getDisplayName %) entities-nearby) ", ")))))))))

(defn zombieze [entity]
  (swap! zombie-players conj (.getDisplayName entity))
  (.setMaximumAir entity 1)
  (.setRemainingAir entity 1)
  (.sendMessage entity "You turned into a zombie.")
  (c/lingr (str (name2icon (.getDisplayName entity)) "turned into a zombie.")))

(comment (defn potion-weakness [name]
  (.apply
    (org.bukkit.potion.PotionEffect. org.bukkit.potion.PotionEffectType/WEAKNESS 500 1)
    (Bukkit/getPlayer name))))

(defn arrow-damages-entity-event [_ arrow target]
  (let [shooter (.getShooter arrow)]
    (when (instance? Player shooter)
      (cond
        (.contains (.getInventory shooter) Material/WEB)
        (do
          (chain-entity target shooter)
          (c/consume-itemstack (.getInventory shooter) Material/WEB))
        (= arrow-skill-explosion (arrow-skill-of shooter))
        (.damage target 10 shooter)
        (= arrow-skill-ice (arrow-skill-of shooter))
        (when (not (.isDead target))
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
                            (when (not (.isDead target))
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
                                  (.setType block Material/AIR))))))))
        (= 'fly (arrow-skill-of shooter))
        (future-call #(c/add-velocity target 0 1 0))
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
          (.setPassenger cart target))))))

(comment (let [cart (.spawn (.getWorld target) (.getLocation target) Minecart)]
           (future-call #(let [b (.getBlock (.getLocation target))]
                           (.setType b Material/RAILS)))
           (.setPassenger cart target)
           (c/add-velocity cart 0 5 0)))

(defn vector-from-to [ent-from ent-to]
  (.toVector (.subtract (.getLocation ent-to) (.getLocation ent-from))))

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
                    (when (not (.isDead another-pig))
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

(defn rebirth-from-zombie [evt target]
  (.setCancelled evt true)
  (.setMaximumAir target 300) ; default maximum value
  (.setRemainingAir target 300)
  (.setHealth target (.getMaxHealth target))
  (swap! zombie-players disj (.getDisplayName target))
  (.sendMessage target "You rebirthed as a human."))

(defn fish-damages-entity-event [evt fish target]
  (if-let [shooter (.getShooter fish)]
    (let [table {Cow Material/RAW_BEEF
                 Pig Material/PORK
                 Chicken Material/RAW_CHICKEN
                 Zombie Material/LEATHER_CHESTPLATE
                 Skeleton Material/BOW
                 Creeper Material/TNT
                 CaveSpider Material/IRON_INGOT
                 Spider Material/REDSTONE
                 Sheep Material/BED
                 Villager Material/LEATHER_LEGGINGS
                 Silverfish Material/DIAMOND_PICKAXE}]
      (if-let [m (last (first (filter #(instance? (first %) target) table)))]
        (.dropItem (.getWorld target) (.getLocation target) (ItemStack. m 1))
        (cond
          (instance? Player target)
          (do
            (if-let [item (.getItemInHand target)]
              (do
                (.setItemInHand target (ItemStack. Material/AIR))
                (.setItemInHand shooter item)
                (c/lingr (str (.getDisplayName shooter) " fished " (.getDisplayName target))))))

          :else
          (.teleport target shooter))))))

(defn entity-damage-event [evt]
  (let [target (.getEntity evt)
        attacker (when (instance? EntityDamageByEntityEvent evt)
                   (.getDamager evt))]
    (cond
      (= EntityDamageEvent$DamageCause/DROWNING (.getCause evt))
      (when (and
              (instance? Player target)
              (zombie-player? target))
        (rebirth-from-zombie evt target))

      (= EntityDamageEvent$DamageCause/ENTITY_EXPLOSION (.getCause evt))
      (if (= (rem @creeper-explosion-idx 3) 0)
        (.setDamage evt (min (.getDamage evt) 19))
        (.setDamage evt 0))

      :else
      (do
        (when (and
                (instance? Villager target)
                (instance? EntityDamageByEntityEvent evt)
                (instance? Player attacker))
          (c/lingr (str (name2icon (.getDisplayName attacker)) "is attacking a Villager"))
          (.damage attacker (.getDamage evt)))
        (when (instance? Fish attacker)
          (fish-damages-entity-event evt attacker target))
        (when (instance? Snowball attacker)
          (if-let [shooter (.getShooter attacker)]
            (when (or
                    (@special-snowball-set attacker)
                    (instance? Snowman shooter))
              (do
                (.setFireTicks target 50)
                (.damage target 2 (.getShooter attacker))))))
        (when (instance? Arrow attacker)
          (arrow-damages-entity-event evt attacker target))
        (when (instance? Player attacker)
          (when (and (instance? Spider target)
                     (not (instance? CaveSpider target)))
            (player-attacks-spider-event evt attacker target))
          (when (instance? Pig target)
            (player-attacks-pig-event evt attacker target))
          (when (instance? Chicken target)
            (player-attacks-chicken-event evt attacker target))
          (when (= 'fly (arrow-skill-of attacker))
            (future-call #(c/add-velocity target 0 1 0))))
        (comment
          (when (and (instance? Player target) (= EntityDamageEvent$DamageCause/FALL (.getCause evt))))
            (when (= Material/SLIME_BALL (.getType (.getItemInHand target)))
              (.setCancelled evt true)
              (c/add-velocity target 0 1 0)
              (c/consume-item target))
        )
        (when (and (instance? Player target) (instance? EntityDamageByEntityEvent evt))
          (when (and (instance? Zombie attacker) (not (instance? PigZombie attacker)))
            (if (zombie-player? target)
              (.setCancelled evt true)
              (zombieze target)))
          (when (and (instance? Player attacker) (zombie-player? attacker))
            (do
              (zombieze target)
              (.sendMessage attacker "You made a friend"))))))))

(defn block-break-event [evt]
  (if-let [player (.getPlayer evt)]
    (when (= (.getType (.getItemInHand player)) Material/AIR)
      (.sendMessage player "Your hand hurts!")
      (.damage player (rand-int 5)))))

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
      (.remove entity))))
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
      (.createExplosion (.getWorld snowball) (.getLocation snowball) 0)
      (.remove snowball))))

(defn projectile-hit-event [evt]
  (let [entity (.getEntity evt)]
        (condp instance? entity
          #_(Fish (fish-hit-event evt entity))
          Fireball (.setYield entity 0.0)
          Arrow (arrow-hit-event evt entity)
          Snowball (snowball-hit-event evt entity)
          ;(instance? Snowball entity) (.strikeLightning (.getWorld entity) (.getLocation entity))
          )))

(defn player-bed-enter-event [evt]
  (c/broadcast (.. evt (getPlayer) (getDisplayName)) " is sleeping.")
  (future-call #(do
                  (Thread/sleep 3000)
                  (when (.. evt (getPlayer) (isSleeping))
                    (let [all-players (Bukkit/getOnlinePlayers)
                          bed-players (filter (memfn isSleeping) all-players)]
                      (when (< (count all-players) (inc (* (count bed-players) 2)))
                        (.setTime world 0)
                        (c/broadcast "good morning everyone!")))))))

;(defn vehicle-enter-event* [evt]
;  (let [vehicle (.getVehicle evt)
;        entity (.getEntered evt)
;        rail (.getBlock (.getLocation vehicle))
;        block-under (.getBlock (.add (.getLocation vehicle) 0 -1 0))]
;    (when (and
;            (instance? Player entity)
;            (= (.getType rail) Material/RAILS)
;            (= (.getType block-under) Material/LAPIS_BLOCK))
;      (let [direction (.getDirection (.getNewData (.getType rail) (.getData rail)))
;            diff (cond
;                   (= org.bukkit.block.BlockFace/SOUTH direction) (Vector. -1 0 0)
;                   (= org.bukkit.block.BlockFace/NORTH direction) (Vector. 1 0 0)
;                   (= org.bukkit.block.BlockFace/WEST direction) (Vector. 0 0 1)
;                   (= org.bukkit.block.BlockFace/EAST direction) (Vector. 0 0 -1))
;            destination (first (filter
;                                 #(= (.getType %) Material/LAPIS_BLOCK)
;                                 (map
;                                   #(.getBlock (.add (.clone (.getLocation block-under)) (.multiply (.clone diff) %)))
;                                   (range 3 100))))]
;        (when destination
;          (.teleport vehicle (.add (.getLocation destination) 0 3 0)))))))
;
;(defn vehicle-enter-event []
;  (c/auto-proxy [Listener] []
;                (onVehicleEnter [evt] (vehicle-enter-event* evt))))

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

(def recipe-string-web
  (let [x (org.bukkit.inventory.ShapelessRecipe.
            (ItemStack. Material/WEB 3))]
    (.addIngredient x 3 Material/STRING)
    x))

(def recipe-gravel-flint
  (let [x (org.bukkit.inventory.ShapelessRecipe.
            (ItemStack. Material/FLINT 1))]
    (.addIngredient x 3 Material/GRAVEL)
    x))

(defonce swank* nil)
(defn on-enable [plugin]
  (when (nil? swank*)
    (def swank* (swank.swank/start-repl 4005)))
  (Bukkit/addRecipe recipe-string-web)
  (Bukkit/addRecipe recipe-gravel-flint)
  (.scheduleSyncRepeatingTask (Bukkit/getScheduler) plugin (fn [] (periodically)) 50 50)
  (comment (proxy [java.lang.Object CommandExecuter] []
    (onCommand [this ^CommandSender sender ^Command command ^String label ^String[] args]
      (prn command))))
  (c/lingr "cloft plugin running..."))

;  (c/lingr "cloft plugin stopping...")
