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
import org.bukkit.event.player.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.block.*;
import org.bukkit.event.vehicle.*;

public class ClojurePlugin extends JavaPlugin implements Listener {
    public void onEnable() {
        String name = getDescription().getName();
        System.out.println("Enabling "+name+" clojure Plugin");
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
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-bed-enter-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-bed-leave-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-chat-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-drop-item-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-interact-entity-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-interact-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-level-change-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-login-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-move-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "entity-damage-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "entity-death-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "entity-explode-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "entity-shoot-bow-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "entity-target-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "explosion-prime-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "projectile-hit-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "block-break-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "block-damage-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "block-place-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onBlockRedstone(BlockRedstoneEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "block-redstone-event");
        if (f.isBound()) f.invoke(event);
    }
    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "vehicle-enter-event");
        if (f.isBound()) f.invoke(event);
    }
    /* end auto-generated code */

    private void invokeClojureFunc(String enableFunction, Object arg) {
        String ns = "cloft.core";
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
