(ns cloft.loc)

(defn spawn [loc klass]
  (.spawn (.getWorld loc) loc klass))

(defn explode [loc power fire?]
  (.createExplosion (.getWorld loc) loc power fire?))

(defn drop-item [loc itemstack]
  (.dropItemNaturally (.getWorld loc) loc itemstack))

(defn play-effect [loc effect data]
  (.playEffect (.getWorld loc) loc effect data))
