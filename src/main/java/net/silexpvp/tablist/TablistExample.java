 package net.silexpvp.tablist;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.thatkawaiisam.ziggurat.TablistManager;
import io.github.thatkawaiisam.ziggurat.ITablistAdapter;
import io.github.thatkawaiisam.ziggurat.utils.BufferedTabObject;
import io.github.thatkawaiisam.ziggurat.utils.SkinTexture;
import io.github.thatkawaiisam.ziggurat.utils.TabColumn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TablistExample extends JavaPlugin {

    private final String[] ladders = {"NoDebuff", "Debuff", "BuildUHC", "Sumo", "Spleef", "Bed Wars"};

    @Override
    public void onEnable() {
        new TablistManager(this, new ITablistAdapter() {
            @Override
            public Set<BufferedTabObject> getSlots(Player player) {
                Set<BufferedTabObject> slots = new HashSet<>();

                slots.add(new BufferedTabObject().slot(2).column(TabColumn.LEFT).text("&7Online: &f" + Bukkit.getOnlinePlayers().size()));

                slots.add(new BufferedTabObject().slot(1).column(TabColumn.MIDDLE).text("&6&lSilex Network"));

                slots.add(new BufferedTabObject().slot(2).column(TabColumn.MIDDLE).text("&7Your Connection").ping(player.spigot().getPing()));

                slots.add(new BufferedTabObject().slot(4).column(TabColumn.MIDDLE).text("&6&lYour Rankings"));

                for (int i = 0; i < ladders.length; i++) {
                    slots.add(new BufferedTabObject().slot(5 + i - (i % 3)).column(TabColumn.getFromOrdinal(i % 3)).text(ChatColor.GRAY +ladders[i]));
                    slots.add(new BufferedTabObject().slot(5 + i - (i % 3) + 1).column(TabColumn.getFromOrdinal(i % 3)).text("&6Gold V"));
                }

                slots.add(new BufferedTabObject().slot(2).column(TabColumn.RIGHT).text("&7Fighting: &f0"));

                return slots;
            }

            @Override
            public String getFooter() {
                return null;
            }

            @Override
            public String getHeader() {
                return null;
            }
        });
    }
}
