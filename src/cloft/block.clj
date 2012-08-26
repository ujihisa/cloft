(ns cloft.block
  (:require [cloft.material :as m])
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
              m/air #{:enterable}
              m/water #{:enterable}
              m/lava #{:enterable}
              m/fire #{:enterable}
              m/stationary-water #{:gettable :enterable}
              m/stationary-lava  #{:gettable :enterable}
              m/web #{:gettable :combustible :enterable}
              m/brown-mushroom  #{:gettable :combustible :enterable}
              m/red-mushroom #{:gettable :combustible :enterable}
              m/red-rose #{:gettable :combustible :enterable}
              m/yellow-flower #{:gettable :combustible :enterable}
              m/wheat #{:gettable :combustible :enterable}
              m/pumpkin-stem #{:gettable :combustible :enterable}
              m/melon-stem #{:gettable :combustible :enterable}
              m/vine #{:combustible :enterable}
              m/dead-bush #{:combustible :enterable}
              m/sugar-cane #{:gettable :combustible :enterable}
              m/leaves  #{:gettable :combustible}
              m/melon-block #{:gettable :combustible}
              m/pumpkin #{:gettable :combustible}
              m/water-lily  #{:gettable :enterable}
              m/bed-block #{:gettable :combustible :enterable}
              m/cactus #{:gettable :combustible}
              m/sand #{:gettable}
              m/sandstone #{:gettable}
              m/stone #{:gettable}
              m/soul-sand #{:gettable}
              m/step #{:gettable :crafted :enterable}
              m/stone-button #{:gettable :crafted :enterable}
              m/tnt #{:crafted :gettable}
              m/wall-sign #{:crafted :enterable}
              m/soil #{:crafted :gettable}
              m/torch #{:crafted :enterable :gettable}
              m/trap-door #{:crafted :enterable :gettable}
              m/snow-block #{:crafted :gettable}
              m/snow #{:enterable :gettable}
              m/wood #{:combustible :gettable}
              m/workbench #{:combustible :crafted :gettable}
              m/redstone-torch-on #{:enterable :crafted :gettable}
              m/redstone-torch-off #{:enterable :crafted :gettable}
              m/smooth-brick #{:crafted :gettable}
              m/smooth-stairs #{:crafted :enterable :gettable}
              m/portal #{:crafted :enterable}
              m/nether-brick #{:crafted :gettable}
              m/nether-brick-stairs #{:crafted :gettable}
              m/nether-fence #{:crafted :gettable}
              m/nether-warts #{:gettable}
              m/mob-spawner #{:crafted}
              m/lever #{:crafted :enterable :gettable}
              m/jukebox #{:crafted :gettable}
              m/ladder #{:crafted :enterable :gettable}
              m/fence #{:crafted :enterable :gettable}
              m/fence-gate #{:crafted :enterable :gettable}
              m/cauldron #{:crafted :gettable :enterable}
              m/piston-base #{:crafted :gettable}
              m/piston-extension #{:crafted}
              m/piston-moving-piece #{:crafted}
              m/piston-sticky-base #{:crafted :gettable}
              m/rails #{:crafted :gettable}
              m/sign #{:crafted :enterable :gettable}
              m/wood-stairs #{:crafted :enterable :combustible :gettable}}]
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
          (#{m/yellow-flower m/red-rose} (.getMaterial evt))
          (= m/fence (.getType
                              (.getBlock (.add (.getLocation (.getBlock evt)) 0 -1 0)))))
    (.setBuildable evt true)))

(defn blazon? [block-type block-against]
  (and (every? #(= % block-type)
               (map #(.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                    [0 0 -1 1] [-1 1 0 0]))
       (every? #(not= % block-type)
               (map #(.getType (.getBlock (.add (.clone (.getLocation block-against)) %1 0 %2)))
                    [-1 1 0 0] [-1 1 0 0]))))
