(ns cloft.chimera-cow
    (:require [cloft.cloft :as c])
    (:require [cloft.arrow :as a])
    (:import [org.bukkit.entity Cow Fireball Arrow Minecart Player])
    (:import [org.bukkit Material Location Effect])
    (:import [org.bukkit.inventory ItemStack])
    (:import [org.bukkit.util Vector]))

(def chimera-cows (atom #{}))

(defn is? [entity]
  (and
    (instance? Cow entity)
    (@chimera-cows entity)))

(defn birth [player cow]
  (.sendMessage player "chimera!")
  (swap! chimera-cows conj cow)
  #_(future-call #(let [world (.getWorld cow)]
                  (.setThundering world true)
                  (Thread/sleep 10000)
                  (.setThundering world false))))

(defn murder-event [evt cow player]
  (future-call #(let [world (.getWorld cow)
                      loc (.getLocation cow)]
                  (doseq [_ (range 0 20)]
                    (Thread/sleep 1000)
                    (.dropItemNaturally world loc (if (= 0 (rand-int 10))
                                                    (ItemStack. Material/GOLDEN_APPLE)
                                                    (ItemStack. Material/APPLE))))))
  (.setDroppedExp evt 200)
  (c/broadcast (format "%s beated a chimera cow!" (.getDisplayName player)))
  (c/lingr (format "%s beated a chimera cow!" (.getDisplayName player))))


(defn fall-damage-event [evt cow]
  (.setCancelled evt true))

(defn damage-event [evt cow attacker]
  (.setDamage evt (min (.getDamage evt) 2))
  (condp instance? attacker
    Fireball (.setCancelled evt true)
    Arrow (a/reflect-arrow evt attacker cow)
    nil))

(defn arrow-hit [entity]
  (.remove entity))

(defn fireball-hit-player [evt player cow fireball]
  #_(.sendMessage player "Chimeracow's fireball hit!")
  (case (rand-int 3)
    0 (let [cart (.spawn (.getWorld player) (.getLocation player) Minecart)]
        (.setPassenger cart player))
    1 (.setFireTicks player 200)
    2 nil
    3 nil))

(defn periodically []
  (doseq [c @chimera-cows]
    (if (or (.isDead c) (= 0 (rand-int 1000)))
      (swap! chimera-cows disj c)
      (do
        (.playEffect (.getWorld c) (.getLocation c) Effect/MOBSPAWNER_FLAMES nil)
        (when (not= 0 (rand-int 10))
          (future-call
            (fn []
              (Thread/sleep (rand-int 2500))
              (let [players (filter #(instance? Player %) (.getNearbyEntities c 50 50 50))]
                (when-let [player (when-not (empty? players)
                                    (rand-nth players))]
                  (let [dire (.subtract (.clone (.getLocation player))
                                        (.clone (.getLocation c)))
                        vect (.normalize (.toVector dire))]
                    (if (not= 0 (rand-int 5))
                      (do
                        (.setVelocity c (Vector. (* (.getX vect) 0.1)
                                                 (rand-nth [0.3 0.5 0.7])
                                                 (* (.getZ vect) 0.1)))
                      (Thread/sleep 800)
                      (.setVelocity c (Vector. 0.0 0.5 0.0))
                      (let [dire (.subtract (.clone (.getLocation player))
                                            (.clone (.getLocation c)))
                            vect (.normalize (.toVector dire))]
                        (doseq [_ [0 1 2]]
                          (let [fb (.launchProjectile c Fireball)]
                            (.setShooter fb c)
                            (.setYield fb 0.0)
                            (.teleport fb (.add (.clone (.getLocation c)) vect))
                            (Thread/sleep 300)
                            (.setDirection fb vect)
                            (.setVelocity fb (.add vect (Vector. (- (rand) 0.5) 0.0 (- (rand) 0.5))))))))
                      (letfn [(rand1 []
                                (* 0.8 (- (rand) 0.5)))]
                        (doseq [_ (range 0 50)]
                          (let [ar (.launchProjectile c Arrow)]
                            (.setShooter ar c)
                            (.setVelocity
                              ar
                              (Vector. (rand1) 1.0 (rand1)))))))))))))))))
