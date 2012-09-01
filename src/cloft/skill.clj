(ns cloft.skill
  (:require [cloft.cloft :as c])
  (:require [cloft.material :as m]))

(defprotocol Learn
  (block [_]))

(defprotocol ArrowSkill
  (arrow-damage-entity [_ evt arrow target])
  (arrow-hit [_ evt arrow]))

(def arrow-skill-teleport
  (reify
    clojure.lang.Named
    (getName [_] "TELEPORT")

    Learn
    (block [_] 'yellow-flower)
    ArrowSkill
    (arrow-damage-entity [_ evt arrow target]
      (.setCancelled evt true))
    (arrow-hit [_ evt arrow]
      (let [location (.getLocation arrow)
            world (.getWorld location)
            shooter (.getShooter arrow)]
        (.setFallDistance shooter 0.0)
        (c/teleport-without-angle shooter location))
      (.remove arrow))))
