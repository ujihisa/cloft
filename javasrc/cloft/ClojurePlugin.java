package cloft;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginLoader;
import java.util.HashSet;
import java.net.URLClassLoader;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Server;
import java.lang.ClassLoader;
import java.net.URL;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event;
//import org.bukkit.event.player.*;
//import org.bukkit.event.entity.*;
//import org.bukkit.event.block.*;
//import org.bukkit.event.vehicle.*;
//import org.bukkit.event.world.*;
//import org.bukkit.event.painting.*;
//import org.bukkit.event.server.*;

public class ClojurePlugin extends JavaPlugin implements Listener {
    private String ns;

    public void onEnable() {
        String name = getDescription().getName();
        this.ns = name + ".core";
        System.out.println("Enabling " + name + " clojure Plugin");
        invokeClojureFunc("on-enable", this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable(String ns, String disableFunction) {
        clojure.lang.RT.var(ns, disableFunction).invoke(this);
    }

    public void onDisable() {
        /*
        String name = getDescription().getName();
        System.out.println("Disabling "+name+" clojure Plugin");
        if ("clj-minecraft".equals(name)) {
            onEnable("cljminecraft.core", "onenable");
        } else {
            onEnable(name+".core", "disable-plugin");
        }
        */
    }

    /* begin auto-generated code */
    @EventHandler
    public void onAsyncPlayerPreLogin(org.bukkit.event.player.AsyncPlayerPreLoginEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "async-player-pre-login-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockBurn(org.bukkit.event.block.BlockBurnEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-burn-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockCanBuild(org.bukkit.event.block.BlockCanBuildEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-can-build-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockDamage(org.bukkit.event.block.BlockDamageEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-damage-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockDispense(org.bukkit.event.block.BlockDispenseEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-dispense-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockBreak(org.bukkit.event.block.BlockBreakEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-break-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onFurnaceExtract(org.bukkit.event.inventory.FurnaceExtractEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "furnace-extract-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockFade(org.bukkit.event.block.BlockFadeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-fade-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockFromTo(org.bukkit.event.block.BlockFromToEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-from-to-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockForm(org.bukkit.event.block.BlockFormEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-form-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockSpread(org.bukkit.event.block.BlockSpreadEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-spread-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityBlockForm(org.bukkit.event.block.EntityBlockFormEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-block-form-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockIgnite(org.bukkit.event.block.BlockIgniteEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-ignite-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockPhysics(org.bukkit.event.block.BlockPhysicsEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-physics-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-piston-extend-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-piston-retract-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-place-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockRedstone(org.bukkit.event.block.BlockRedstoneEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "block-redstone-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBrew(org.bukkit.event.inventory.BrewEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "brew-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onFurnaceBurn(org.bukkit.event.inventory.FurnaceBurnEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "furnace-burn-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onFurnaceSmelt(org.bukkit.event.inventory.FurnaceSmeltEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "furnace-smelt-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onLeavesDecay(org.bukkit.event.block.LeavesDecayEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "leaves-decay-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onNotePlay(org.bukkit.event.block.NotePlayEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "note-play-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onSignChange(org.bukkit.event.block.SignChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "sign-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onCreatureSpawn(org.bukkit.event.entity.CreatureSpawnEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "creature-spawn-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onCreeperPower(org.bukkit.event.entity.CreeperPowerEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "creeper-power-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityChangeBlock(org.bukkit.event.entity.EntityChangeBlockEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-change-block-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityBreakDoor(org.bukkit.event.entity.EntityBreakDoorEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-break-door-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityCombust(org.bukkit.event.entity.EntityCombustEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-combust-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityCombustByBlock(org.bukkit.event.entity.EntityCombustByBlockEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-combust-by-block-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityCombustByEntity(org.bukkit.event.entity.EntityCombustByEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-combust-by-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityCreatePortal(org.bukkit.event.entity.EntityCreatePortalEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-create-portal-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityDamageByBlock(org.bukkit.event.entity.EntityDamageEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-damage-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityDamageByBlock(org.bukkit.event.entity.EntityDamageByBlockEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-damage-by-block-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityDamageByEntity(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-damage-by-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-death-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-death-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-explode-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityInteract(org.bukkit.event.entity.EntityInteractEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-interact-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityRegainHealth(org.bukkit.event.entity.EntityRegainHealthEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-regain-health-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityShootBow(org.bukkit.event.entity.EntityShootBowEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-shoot-bow-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityTame(org.bukkit.event.entity.EntityTameEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-tame-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityTarget(org.bukkit.event.entity.EntityTargetEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-target-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityTargetLivingEntity(org.bukkit.event.entity.EntityTargetLivingEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-target-living-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityTeleport(org.bukkit.event.entity.EntityTeleportEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "entity-teleport-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onExplosionPrime(org.bukkit.event.entity.ExplosionPrimeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "explosion-prime-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "food-level-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onItemDespawn(org.bukkit.event.entity.ItemDespawnEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "item-despawn-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onItemSpawn(org.bukkit.event.entity.ItemSpawnEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "item-spawn-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPigZap(org.bukkit.event.entity.PigZapEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "pig-zap-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onProjectileHit(org.bukkit.event.entity.ProjectileHitEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "projectile-hit-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onExpBottle(org.bukkit.event.entity.ExpBottleEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "exp-bottle-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPotionSplash(org.bukkit.event.entity.PotionSplashEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "potion-splash-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onProjectileLaunch(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "projectile-launch-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onSheepDyeWool(org.bukkit.event.entity.SheepDyeWoolEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "sheep-dye-wool-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onSheepRegrowWool(org.bukkit.event.entity.SheepRegrowWoolEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "sheep-regrow-wool-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onSlimeSplit(org.bukkit.event.entity.SlimeSplitEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "slime-split-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onHangingBreak(org.bukkit.event.hanging.HangingBreakEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "hanging-break-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onHangingBreakByEntity(org.bukkit.event.hanging.HangingBreakByEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "hanging-break-by-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onHangingPlace(org.bukkit.event.hanging.HangingPlaceEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "hanging-place-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEnchantItem(org.bukkit.event.enchantment.EnchantItemEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "enchant-item-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "inventory-click-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onCraftItem(org.bukkit.event.inventory.CraftItemEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "craft-item-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "inventory-close-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "inventory-open-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPrepareItemCraft(org.bukkit.event.inventory.PrepareItemCraftEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "prepare-item-craft-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPrepareItemEnchant(org.bukkit.event.enchantment.PrepareItemEnchantEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "prepare-item-enchant-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPaintingBreak(org.bukkit.event.painting.PaintingBreakEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "painting-break-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPaintingBreakByEntity(org.bukkit.event.painting.PaintingBreakByEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "painting-break-by-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPaintingPlace(org.bukkit.event.painting.PaintingPlaceEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "painting-place-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onAsyncPlayerChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "async-player-chat-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerAnimation(org.bukkit.event.player.PlayerAnimationEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-animation-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerBedEnter(org.bukkit.event.player.PlayerBedEnterEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-bed-enter-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerBedLeave(org.bukkit.event.player.PlayerBedLeaveEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-bed-leave-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-bucket-empty-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerBucketFill(org.bukkit.event.player.PlayerBucketFillEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-bucket-fill-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerChangedWorld(org.bukkit.event.player.PlayerChangedWorldEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-changed-world-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerRegisterChannel(org.bukkit.event.player.PlayerRegisterChannelEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-register-channel-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerUnregisterChannel(org.bukkit.event.player.PlayerUnregisterChannelEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-unregister-channel-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerChat(org.bukkit.event.player.PlayerChatEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-chat-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerChatTabComplete(org.bukkit.event.player.PlayerChatTabCompleteEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-chat-tab-complete-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerCommandPreprocess(org.bukkit.event.player.PlayerCommandPreprocessEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-command-preprocess-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerDropItem(org.bukkit.event.player.PlayerDropItemEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-drop-item-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerEggThrow(org.bukkit.event.player.PlayerEggThrowEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-egg-throw-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerExpChange(org.bukkit.event.player.PlayerExpChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-exp-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerFish(org.bukkit.event.player.PlayerFishEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-fish-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerGameModeChange(org.bukkit.event.player.PlayerGameModeChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-game-mode-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-interact-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-interact-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerItemBreak(org.bukkit.event.player.PlayerItemBreakEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-item-break-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerItemHeld(org.bukkit.event.player.PlayerItemHeldEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-item-held-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-join-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerKick(org.bukkit.event.player.PlayerKickEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-kick-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerLevelChange(org.bukkit.event.player.PlayerLevelChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-level-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerLogin(org.bukkit.event.player.PlayerLoginEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-login-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-move-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerTeleport(org.bukkit.event.player.PlayerTeleportEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-teleport-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerPortal(org.bukkit.event.player.PlayerPortalEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-portal-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-pickup-item-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-quit-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-respawn-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerShearEntity(org.bukkit.event.player.PlayerShearEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-shear-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerToggleFlight(org.bukkit.event.player.PlayerToggleFlightEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-toggle-flight-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-toggle-sneak-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerToggleSprint(org.bukkit.event.player.PlayerToggleSprintEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-toggle-sprint-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerVelocity(org.bukkit.event.player.PlayerVelocityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-velocity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerPreLogin(org.bukkit.event.player.PlayerPreLoginEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "player-pre-login-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onMapInitialize(org.bukkit.event.server.MapInitializeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "map-initialize-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPluginDisable(org.bukkit.event.server.PluginDisableEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "plugin-disable-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPluginEnable(org.bukkit.event.server.PluginEnableEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "plugin-enable-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onServerCommand(org.bukkit.event.server.ServerCommandEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "server-command-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onRemoteServerCommand(org.bukkit.event.server.RemoteServerCommandEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "remote-server-command-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onServerListPing(org.bukkit.event.server.ServerListPingEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "server-list-ping-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onServiceRegister(org.bukkit.event.server.ServiceRegisterEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "service-register-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onServiceUnregister(org.bukkit.event.server.ServiceUnregisterEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "service-unregister-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleBlockCollision(org.bukkit.event.vehicle.VehicleBlockCollisionEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-block-collision-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleEntityCollision(org.bukkit.event.vehicle.VehicleEntityCollisionEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-entity-collision-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleCreate(org.bukkit.event.vehicle.VehicleCreateEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-create-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleDamage(org.bukkit.event.vehicle.VehicleDamageEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-damage-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleDestroy(org.bukkit.event.vehicle.VehicleDestroyEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-destroy-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleEnter(org.bukkit.event.vehicle.VehicleEnterEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-enter-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleExit(org.bukkit.event.vehicle.VehicleExitEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-exit-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleMove(org.bukkit.event.vehicle.VehicleMoveEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-move-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleUpdate(org.bukkit.event.vehicle.VehicleUpdateEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "vehicle-update-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onLightningStrike(org.bukkit.event.weather.LightningStrikeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "lightning-strike-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onThunderChange(org.bukkit.event.weather.ThunderChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "thunder-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onWeatherChange(org.bukkit.event.weather.WeatherChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "weather-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "chunk-load-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onChunkPopulate(org.bukkit.event.world.ChunkPopulateEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "chunk-populate-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onChunkUnload(org.bukkit.event.world.ChunkUnloadEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "chunk-unload-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPortalCreate(org.bukkit.event.world.PortalCreateEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "portal-create-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onSpawnChange(org.bukkit.event.world.SpawnChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "spawn-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onStructureGrow(org.bukkit.event.world.StructureGrowEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "structure-grow-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onWorldInit(org.bukkit.event.world.WorldInitEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "world-init-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onWorldLoad(org.bukkit.event.world.WorldLoadEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "world-load-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onWorldSave(org.bukkit.event.world.WorldSaveEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "world-save-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onWorldUnload(org.bukkit.event.world.WorldUnloadEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "world-unload-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onDynmapWebChat(org.dynmap.DynmapWebChatEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var(ns, "dynmap-web-chat-event");
        if (f.isBound()) f.invoke(event);
    }

    /* end auto-generated code */

    private void invokeClojureFunc(String enableFunction, Object arg) {
        try {
            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); 

            clojure.lang.RT.loadResourceScript(ns.replaceAll("[.]", "/")+".clj");
            clojure.lang.RT.var(ns, enableFunction).invoke(arg);

            Thread.currentThread().setContextClassLoader(previous);
        } catch (Exception e) {
            System.out.println("Something broke setting up Clojure");
            e.printStackTrace();
        }
    }
}
