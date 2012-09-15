(ns cloft.loc
  (:import [org.bukkit Bukkit]))

(defn spawn [loc klass]
  (.spawn (.getWorld loc) loc klass))

(defn fall-block [loc material data]
  (.spawnFallingBlock (.getWorld loc) loc material data))

(defn explode [loc power fire?]
  (.createExplosion (.getWorld loc) loc power fire?))

(defn drop-item [loc itemstack]
  (.dropItemNaturally (.getWorld loc) loc itemstack))

(defn play-effect [loc effect data]
  (.playEffect (.getWorld loc) loc effect data))

(defn play-sound [loc sound volume pitch]
  (.playSound (.getWorld loc) loc sound volume pitch))

(defn of [pname]
  (.getLocation (Bukkit/getPlayer pname)))

(def ujm (partial of "ujm"))
