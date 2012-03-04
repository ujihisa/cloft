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

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-chat");
        if (f.isBound()) f.invoke(event);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        clojure.lang.Var f = clojure.lang.RT.var("cloft.core", "player-login");
        if (f.isBound()) f.invoke(event);
    }

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
