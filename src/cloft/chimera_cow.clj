(ns cloft.chimera-cow
  (:use [cloft.cloft :only [later]])
  (:require [cloft.cloft :as c]
            [cloft.material :as m]
            [cloft.arrow :as arrow]
            [cloft.lingr :as lingr]
            [cloft.loc :as loc])
  (:import [org.bukkit.entity Cow Fireball Arrow Minecart Player]
           [org.bukkit Material Location Effect]
           [org.bukkit.inventory ItemStack]
           [org.bukkit.util Vector]
           [org.bukkit Bukkit DyeColor]))

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
  (let [world (.getWorld cow)
        loc (.getLocation cow)]
    (future
      (dotimes [_ 20]
        (Thread/sleep 1000)
        (later
          (.dropItemNaturally world loc (if (= 0 (rand-int 10))
                                          (ItemStack. m/golden-apple)
                                          (ItemStack. m/apple)))))))
  (.setDroppedExp evt 200)
  (c/broadcast (format "%s beated a chimera cow!" (.getDisplayName player)))
  (lingr/say-in-mcujm (format "%s beated a chimera cow!" (.getDisplayName player))))


(defn fall-damage-event [evt cow]
  (.setCancelled evt true))

(defn damage-event [evt cow attacker]
  (if (= 0 (rand-int 3))
    (.setDamage evt 0)
    (.setDamage evt (min (.getDamage evt) 2)))
  (condp instance? attacker
    Fireball (.setCancelled evt true)
    Arrow (arrow/reflect evt attacker cow)
    nil))

(defn arrow-hit [evt]
  (.remove (.getEntity evt)))

(defn fireball-hit-player [evt player cow fireball]
  #_(.sendMessage player "Chimeracow's fireball hit!")
  (case (rand-int 3)
    0 (let [cart (.spawn (.getWorld player) (.getLocation player) Minecart)]
        (.setPassenger cart player))
    1 (.setFireTicks player 200)
    2 nil
    'must-not-happen))

(defn periodically []
  (doseq [c @chimera-cows
          :when c]
    (loc/play-effect (.getLocation c) Effect/MOBSPAWNER_FLAMES nil)
    (if (or (.isDead c) (= 0 (rand-int 1000)))
      (swap! chimera-cows disj c)
      (when (not= 0 (rand-int 10))
        (future
          (Thread/sleep (rand-int 2500))
          (let [players (Bukkit/getOnlinePlayers)]
            (when-let [player (when-not (empty? players)
                                (first (sort-by #(.distance (.getLocation c) (.getLocation %)) players)))]
              (let [dire (.subtract (.clone (.getLocation player))
                                    (.clone (.getLocation c)))
                    vect (.normalize (.toVector dire))]
                (if (not= 0 (rand-int 5))
                  (do
                    (later
                      (.setVelocity c (Vector. (* (.getX vect) 0.1)
                                               (rand-nth [0.3 0.5 0.7])
                                               (* (.getZ vect) 0.1))))
                    (Thread/sleep 800)
                    (later
                      (.setVelocity c (Vector. 0.0 0.5 0.0)))
                    (let [dire (.subtract (.clone (.getLocation player))
                                          (.clone (.getLocation c)))
                          vect (.normalize (.toVector dire))]
                      (doseq [_ [0 1 2]]
                        (let [fb (.launchProjectile c Fireball)]
                          (later
                            (.setShooter fb c)
                            (.setYield fb 0.0)
                            (.teleport fb (.add (.clone (.getLocation c)) vect)))
                          (Thread/sleep 300)
                          (later
                            (.setDirection fb vect)
                            (.setVelocity fb (.add vect (Vector. (- (rand) 0.5) 0.0 (- (rand) 0.5)))))))))
                  (letfn [(rand1 []
                            (* 0.8 (- (rand) 0.5)))]
                    (later
                      (dotimes [_ 50]
                        (let [ar (.launchProjectile c Arrow)]
                          (.setShooter ar c)
                          (.setVelocity
                            ar
                            (Vector. (rand1) 1.0 (rand1))))))))))))))))
