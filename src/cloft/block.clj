(ns cloft.block
  (:import [org.bukkit Material])
  (:import [org.bukkit.util BlockIterator Vector]))

(defn category
  [& ks]
  """available options:
    :gettable  may have material from that block with proper tool
    :enterable   can step in.
    :combustible  can be set fire.
    :crafted  something crafted by player
    usage:
    (category :gettable :enterable) => #{} of gettable and enterable
    (category :gettable ) => #{} of enterable

    http://jd.bukkit.org/apidocs/org/bukkit/Material.html
    """
  (let [c (set ks)
        data {
              Material/AIR #{:enterable}
              Material/WATER #{:enterable}
              Material/LAVA #{:enterable}
              Material/FIRE #{:enterable}
              Material/STATIONARY_WATER #{:gettable :enterable}
              Material/STATIONARY_LAVA  #{:gettable :enterable}
              Material/WEB #{:gettable :combustible :enterable}
              Material/BROWN_MUSHROOM  #{:gettable :combustible :enterable}
              Material/RED_MUSHROOM #{:gettable :combustible :enterable}
              Material/RED_ROSE #{:gettable :combustible :enterable}
              Material/YELLOW_FLOWER #{:gettable :combustible :enterable}
              Material/WHEAT #{:gettable :combustible :enterable}
              Material/PUMPKIN_STEM #{:gettable :combustible :enterable}
              Material/MELON_STEM #{:gettable :combustible :enterable}
              Material/VINE #{:combustible :enterable}
              Material/DEAD_BUSH #{:combustible :enterable}
              Material/SUGAR_CANE #{:gettable :combustible :enterable}
              Material/LEAVES  #{:gettable :combustible}
              Material/MELON_BLOCK #{:gettable :combustible}
              Material/PUMPKIN #{:gettable :combustible}
              Material/WATER_LILY  #{:gettable :enterable}
              Material/BED_BLOCK #{:gettable :combustible :enterable}
              Material/CACTUS #{:gettable :combustible}
              Material/SAND #{:gettable}
              Material/SANDSTONE #{:gettable}
              Material/STONE #{:gettable}
              Material/SOUL_SAND #{:gettable}
              Material/STEP #{:gettable :crafted :enterable}
              Material/STONE_BUTTON #{:gettable :crafted :enterable}
              Material/TNT #{:crafted :gettable}
              Material/WALL_SIGN #{:crafted :enterable}
              Material/SOIL #{:crafted :gettable}
              Material/TORCH #{:crafted :enterable :gettable}
              Material/TRAP_DOOR #{:crafted :enterable :gettable}
              Material/SNOW_BLOCK #{:crafted :gettable}
              Material/SNOW #{:enterable :gettable}
              Material/WOOD #{:combustible :gettable}
              Material/WORKBENCH #{:combustible :crafted :gettable}
              Material/REDSTONE_TORCH_ON #{:enterable :crafted :gettable}
              Material/REDSTONE_TORCH_OFF #{:enterable :crafted :gettable}
              Material/SMOOTH_BRICK #{:crafted :gettable}
              Material/SMOOTH_STAIRS #{:crafted :enterable :gettable}
              Material/PORTAL #{:crafted :enterable}
              Material/NETHER_BRICK #{:crafted :gettable}
              Material/NETHER_BRICK_STAIRS #{:crafted :gettable}
              Material/NETHER_FENCE #{:crafted :gettable}
              Material/NETHER_WARTS #{:gettable}
              Material/MOB_SPAWNER #{:crafted}
              Material/LEVER #{:crafted :enterable :gettable}
              Material/JUKEBOX #{:crafted :gettable}
              Material/LADDER #{:crafted :enterable :gettable}
              Material/FENCE #{:crafted :enterable :gettable}
              Material/FENCE_GATE #{:crafted :enterable :gettable}
              Material/CAULDRON #{:crafted :gettable :enterable}
              Material/PISTON_BASE #{:crafted :gettable}
              Material/PISTON_EXTENSION #{:crafted}
              Material/PISTON_MOVING_PIECE #{:crafted}
              Material/PISTON_STICKY_BASE #{:crafted :gettable}
              Material/RAILS #{:crafted :gettable}
              Material/SIGN #{:crafted :enterable :gettable}
              Material/WOOD_STAIRS #{:crafted :enterable :combustible :gettable}}]
    (into {} (filter #(clojure.set/subset? c (last %)) data))))

(defn place-in-line-with-offset [world start end place-fn offset-count]
   (let [m (Math/ceil (.distance start end))
         unit (.normalize (.add (.clone end) (.multiply (.clone start) -1.0)))
         iter (BlockIterator. world (.add start (.multiply (.clone unit) offset-count)) unit 0.0 m)]
     (loop [done (.hasNext iter)
            i 0]
       (place-fn (.next iter) i)
       (when (.hasNext iter)
         (recur (.hasNext iter) (inc i))))))

(defn place-in-line [world start end place-fn]
   (place-in-line-with-offset world start end place-fn 0))

(defn blocks-in-radiaus-xz
  [world center inner outer]
  (let [center-block (.getBlockAt world center)
        grided-cetner-location (.getLocation center-block)
        grided-cetner-vector (.toVector grided-cetner-location)
        ux (Vector. 1.0 0.0 0.0)
        uy (Vector. 0.0 1.0 0.0)
        uz (Vector. 0.0 0.0 1.0)
        inner-radius (Math/floor inner)
        outer-radius (Math/ceil outer)
        inner-diameter (* 2 inner-radius)
        outer-diameter (* 2 outer-radius)
        width outer-diameter
        corner (.add (.add (.clone grided-cetner-vector) (.multiply (.clone ux) (- outer-radius)))
                     (.multiply (.clone uz) (- outer-radius)))]
    (for [dx (range 0 width)
          dz (range 0 width)
          :let [v (.add (.add (.clone corner) (.multiply (.clone ux) dx)) (.multiply (.clone uz) dz))]
          :when (and
                  (< (.distance grided-cetner-vector v) (Math/ceil outer))
                  (> (.distance grided-cetner-vector v) (Math/floor inner)))]
      (.getBlockAt world (.toLocation v world)))))

(defn place-in-circle
  [world inner outer center place-fn]
  """with fill. naive way."""
  (doseq [v (blocks-in-radiaus-xz world center inner outer)]
    (place-fn v 0)))

(defn can-build-event [evt]
  (when (and
          (#{Material/YELLOW_FLOWER Material/RED_ROSE} (.getMaterial evt))
          (= Material/FENCE (.getType
                              (.getBlock (.add (.getLocation (.getBlock evt)) 0 -1 0)))))
    (.setBuildable evt true)))
