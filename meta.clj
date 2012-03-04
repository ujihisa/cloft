(ns meta
  (:require [clojure.string :as s]))

(def events
  [;player
   '(PlayerAnimationEvent PlayerBedEnterEvent PlayerBedLeaveEvent
    PlayerBucketEvent PlayerChangedWorldEvent PlayerChatEvent
    PlayerDropItemEvent PlayerEggThrowEvent PlayerExpChangeEvent
    PlayerFishEvent PlayerGameModeChangeEvent PlayerInteractEntityEvent
    PlayerInteractEvent PlayerInventoryEvent PlayerItemHeldEvent
    PlayerJoinEvent PlayerKickEvent PlayerLevelChangeEvent PlayerLoginEvent
    PlayerMoveEvent PlayerPickupItemEvent PlayerQuitEvent PlayerRespawnEvent
    PlayerShearEntityEvent PlayerToggleSneakEvent PlayerToggleSprintEvent
    PlayerVelocityEvent)
   ;entity
   '(CreatureSpawnEvent CreeperPowerEvent EntityChangeBlockEvent
    EntityCombustEvent EntityCreatePortalEvent EntityDamageEvent
    EntityDeathEvent EntityExplodeEvent EntityInteractEvent
    EntityPortalEnterEvent EntityRegainHealthEvent EntityShootBowEvent
    EntityTameEvent EntityTargetEvent EntityTeleportEvent ExplosionPrimeEvent
    FoodLevelChangeEvent ItemDespawnEvent ItemSpawnEvent PigZapEvent
    ProjectileHitEvent SheepDyeWoolEvent SheepRegrowWoolEvent SlimeSplitEvent)
   ;block
   '(BlockBreakEvent BlockBurnEvent BlockCanBuildEvent BlockDamageEvent
     BlockDispenseEvent BlockFadeEvent BlockFromToEvent BlockGrowEvent
     BlockIgniteEvent BlockPhysicsEvent BlockPistonEvent BlockPlaceEvent
     BlockRedstoneEvent
     ;BrewEvent FurnaceBurnEvent FurnaceSmeltEvent
     ;LeavesDecayEvent SignChangeEvent
     )
   ;vehicle
   '(VehicleCollisionEvent VehicleCreateEvent VehicleDamageEvent
     VehicleDestroyEvent VehicleEnterEvent VehicleExitEvent VehicleMoveEvent
     VehicleUpdateEvent)])

(doseq [category events]
  (doseq [e category]
    (let [noevent (s/replace e #"Event$" "")
          hyphen (s/lower-case
                    (s/replace e #"(.)(\p{Upper})" "$1-$2"))]
      (println (apply str (interpose "\n"
        ["    @EventHandler"
         (str "    public void on" noevent "(" e " event) {")
         (str "        clojure.lang.Var f = clojure.lang.RT.var(\"cloft.core\", \"" hyphen "\");")
         "        if (f.isBound()) f.invoke(event);"
         "    }"]))))))

