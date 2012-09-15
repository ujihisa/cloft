(ns cloft.transport
  (:use [cloft.cloft :only [later]])
  (:require [cloft.cloft :as c])
  (:require [cloft.material :as m])
  (:require [cloft.player :as player])
  (:require [cloft.loc :as loc])
  (:import [org.bukkit.util Vector])
  (:import [org.bukkit Bukkit])
  (:import [org.bukkit Location Effect])
  (:import [org.bukkit.entity Player]))

(defn teleport-machine? [loc]
  (=
    (for [x [-1 0 1] z [-1 0 1]]
      (if (and (= x 0) (= z 0))
        'any
        (.getType (.getBlock (.add (.clone loc) x 0 z)))))
    (list m/glowstone m/glowstone m/glowstone
          m/glowstone 'any m/glowstone
          m/glowstone m/glowstone m/glowstone)))

(defn teleport-up [entity block]
  (when (#{m/stone-plate m/wood-plate} (.getType block))
    (let [entity-loc (.getLocation entity)
          loc (.add (.getLocation block) 0 -1 0)]
      (when (teleport-machine? loc)
        (when (instance? Player entity)
          (.sendMessage entity "teleport up!"))
        (let [newloc (.add (.getLocation entity) 0 30 0)]
          (later
            (condp = (.getType block)
              m/stone-plate (.teleport entity newloc)
              m/wood-plate (c/add-velocity entity 0 1.5 0)
              nil))
          (loc/play-effect (.add entity-loc 0 1 0) Effect/BOW_FIRE nil)
          (loc/play-effect newloc Effect/BOW_FIRE nil)
          (loc/play-effect entity-loc Effect/ENDER_SIGNAL nil)
          (loc/play-effect newloc Effect/ENDER_SIGNAL nil))))))

#_(defn teleport-machine [player block block-against]
  (when (= m/water (.getType block))
    (let [wools (for [x [-1 0 1] z [-1 0 1] :when (or (not= x 0) (not= z 0))]
                  (.getType (.getBlock
                              (.add (.clone (.getLocation block-against)) x 0 z))))]
      nil)))

(defn cauldron-teleport [player]
  (let [center-cauldron (.getBlock (.getLocation player))
        surround-type (.getType (.getBlock (.add (.clone (.getLocation player)) 1 0 1)))
        cds (for [[x z] [[1 0] [0 1] [-1 0] [0 -1]]
                  :let [loc (.add (.getLocation player) x 0 z)
                        block (.getBlock loc)]
                  :when (= m/cauldron (.getType block))]
              (.multiply (Vector. x 0 z)
                         (* (Math/pow 2 (.getData block)) (Math/pow 2 (.getData center-cauldron)) 10)))]
    (when (and
            (= m/cauldron (.getType center-cauldron))
            (every? identity cds))
      (let [vect (Vector. 0 1 0)]
        (reduce #(.add %1 %2) vect cds)
        (let [newloc (.add (.getLocation player) vect)]
          (if (and
                (let [btype (.getType (.getBlock newloc))]
                  (and ((cloft.block/category :enterable) btype)
                       (not= m/lava btype)))
                (let [btype (.getType (.getBlock (.add (.clone newloc) 0 1 0)))]
                  (and ((cloft.block/category :enterable) btype)
                       (not= m/lava btype))))
            (do
              (c/broadcast (format "%s teleports with cauldron!" (.getDisplayName player)))
              (.teleport player newloc))
            (if (= m/piston-base surround-type)
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
;            (= (.getType rail) m/rails)
;            (= (.getType block-under) m/lapis-block))
;      (let [direction (.getDirection (.getNewData (.getType rail) (.getData rail)))
;            diff (cond
;                   (= org.bukkit.block.BlockFace/SOUTH direction) (Vector. -1 0 0)
;                   (= org.bukkit.block.BlockFace/NORTH direction) (Vector. 1 0 0)
;                   (= org.bukkit.block.BlockFace/WEST direction) (Vector. 0 0 1)
;                   (= org.bukkit.block.BlockFace/EAST direction) (Vector. 0 0 -1))
;            destination (first (filter
;                                 #(= (.getType %) m/lapis-block)
;                                 (map
;                                   #(.getBlock (.add (.clone (.getLocation block-under)) (.multiply (.clone diff) %)))
;                                   (range 3 100))))]
;        (when destination
;          (.teleport vehicle (.add (.getLocation destination) 0 3 0)))))))
;
;(defn vehicle-enter-event []
;  (c/auto-proxy [Listener] []
;                (onVehicleEnter [evt] (vehicle-enter-event* evt))))
