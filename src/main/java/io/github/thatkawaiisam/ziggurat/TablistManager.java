package io.github.thatkawaiisam.ziggurat;

import io.github.thatkawaiisam.ziggurat.utils.ITablistHelper;
import io.github.thatkawaiisam.ziggurat.utils.defaultping.IPingProvider;
import io.github.thatkawaiisam.ziggurat.utils.defaultping.impl.DefaultPingImpl;
import io.github.thatkawaiisam.ziggurat.utils.impl.SpigotImplementation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TablistManager {

    private JavaPlugin plugin;
    private ITablistAdapter adapter;
    private Map<UUID, PlayerTablist> tablists;
    private ZigguratThread thread;
    private ITablistHelper implementation;
    private ZigguratListeners listeners;

    //Tablist Ticks
    @Setter
    private long ticks = 20;
    @Setter
    private boolean hook = false;
    @Setter
    private IPingProvider pingProvider;

    public TablistManager(JavaPlugin plugin, ITablistAdapter adapter) {
        if (plugin == null) {
            throw new RuntimeException("TablistManager can not be instantiated without a plugin instance!");
        }

        this.plugin = plugin;
        this.adapter = adapter;
        this.tablists = new ConcurrentHashMap<>();

        this.registerImplementation();
        this.registerPingImplementation();

        this.setup();
    }

    private void registerImplementation() {
        this.implementation = new SpigotImplementation();
    }

    private void registerPingImplementation() {
        pingProvider = new DefaultPingImpl();
    }

    private void setup() {
        listeners = new ZigguratListeners(this);

        //Register Events
        this.plugin.getServer().getPluginManager().registerEvents(listeners, this.plugin);

        //Ensure that the thread has stopped running
        if (this.thread != null) {
            this.thread.stop();
            this.thread = null;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getTablists().containsKey(player.getUniqueId())) {
                continue;
            }

            getTablists().put(player.getUniqueId(), new PlayerTablist(player, this, true));
        }

        //Start Thread
        this.thread = new ZigguratThread(this);
    }

    public void disable() {
        if (this.thread != null) {
            this.thread.stop();
            this.thread = null;
        }

        if (listeners != null) {
            HandlerList.unregisterAll(listeners);
            listeners = null;
        }

        for (UUID uuid : getTablists().keySet()) {
            getTablists().get(uuid).cleanup();
            getTablists().remove(uuid);
        }
    }
}
