(ns cloft.chest
  (:require [cloft.material :as m])
  (:require [cloft.loc :as loc])
  (:import [org.bukkit.inventory ItemStack]))

(defn break-and-scatter [block player]
  (condp = (.getType block)
    m/chest
    (.breakNaturally block (ItemStack. m/air))

    m/ender-chest
    (let [loc (.getLocation block)]
      (.breakNaturally block (ItemStack. m/air))
      (doseq [itemstack (.getContents (.getEnderChest player))
              :when itemstack]
        (loc/drop-item loc itemstack))
      (.clear (.getEnderChest player)))

    (prn
      (format "assertion error! %s had to be either chest or ender-chest"
              (.getType block)))))
