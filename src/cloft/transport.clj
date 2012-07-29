(ns cloft.transport
  (:require [cloft.cloft :as c])
  (:require [cloft.player :as player])
  (:import [org.bukkit.util Vector])
  (:import [org.bukkit Bukkit Material])
  (:import [org.bukkit Location Effect])
  (:import [org.bukkit.entity Player]))

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
                          Material/WOOD_PLATE (c/add-velocity entity 0 1.5 0)
                          nil)
                        (.playEffect (.getWorld entity-loc) (.add entity-loc 0 1 0) Effect/BOW_FIRE nil)
                        (.playEffect (.getWorld newloc) newloc Effect/BOW_FIRE nil)
                        (.playEffect (.getWorld entity-loc) entity-loc Effect/ENDER_SIGNAL nil)
                        (.playEffect (.getWorld newloc) newloc Effect/ENDER_SIGNAL nil)))))))

(defn teleport-machine [player block block-against]
  (when (= Material/WATER (.getType block))
    (let [wools (for [x [-1 0 1] z [-1 0 1] :when (or (not= x 0) (not= z 0))]
                  (.getType (.getBlock
                              (.add (.clone (.getLocation block-against)) x 0 z))))]
      nil)))

(defn cauldron-teleport [player]
  (let [center-cauldron (.getBlock (.getLocation player))
        surround-type (.getType (.getBlock (.add (.clone (.getLocation player)) 1 0 1)))
        cds (for [[x z] [[1 0] [0 1] [-1 0] [0 -1]]]
                 (let [loc (.add (.getLocation player) x 0 z)
                       block (.getBlock loc)]
                   (when (= Material/CAULDRON (.getType block))
                     (.multiply (Vector. x 0 z) (* (Math/pow 2 (.getData block)) (Math/pow 2 (.getData center-cauldron)) 10)))))]
    (when (and
            (= Material/CAULDRON (.getType center-cauldron))
            (every? identity cds))
      (let [vect (Vector. 0 1 0)]
        (reduce #(.add %1 %2) vect cds)
        (let [newloc (.add (.getLocation player) vect)]
          (if (and
                (let [btype (.getType (.getBlock newloc))]
                  (and ((cloft.block/category :enterable) btype)
                       (not= Material/LAVA btype)))
                (let [btype (.getType (.getBlock (.add (.clone newloc) 0 1 0)))]
                  (and ((cloft.block/category :enterable) btype)
                       (not= Material/LAVA btype))))
            (do
              (c/broadcast (format "%s teleports with cauldron!" (.getDisplayName player)))
              (.teleport player newloc))
            (if (= Material/PISTON_BASE surround-type)
              (do
                (c/broadcast (format "%s teleports with cauldron with fly!" (.getDisplayName player)))
                (.teleport player (.add newloc 0 128 0)))
              (.sendMessage player "the destination isn't safe"))))))))


;
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
