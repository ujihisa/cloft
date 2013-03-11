(ns tough-explosive.core
  (:gen-class)
  #_(:require [swank.swank])
  (:require [cloft.loc :as loc]))

(defn entity-damage-event [evt]
  (.setDamage evt (int (/ (.getDamage evt) 2))))

(defn block-damage-event [evt]
  (let [block (.getBlock evt)]
    (loc/explode (.getLocation block) 1 false)))

(defonce swank* nil)
(defn on-enable [plugin]
  #_(when-not swank*
    (def swank* (swank.swank/start-repl 4005))))
