package io.github.thatkawaiisam.ziggurat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ZigguratListeners implements Listener {

    private TablistManager instance;

    public ZigguratListeners(TablistManager instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        instance.getTablists().put(player.getUniqueId(), new PlayerTablist(player, instance, false));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        instance.getTablists().remove(player.getUniqueId());
    }

}
