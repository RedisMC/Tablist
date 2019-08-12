package io.github.thatkawaiisam.ziggurat;

import io.github.thatkawaiisam.ziggurat.utils.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

import java.util.*;

@Getter
public class PlayerTablist {

    private Player player;
    private Scoreboard scoreboard;

    private Set<TabEntry> currentEntries = new HashSet<>();
    private TablistManager instance;

    public PlayerTablist(Player player, TablistManager instance, boolean online) {
        this.player = player;
        this.instance = instance;

        if (instance.isHook() || !(player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard())) {
            this.scoreboard = player.getScoreboard();
        } else {
            this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(this.scoreboard);
        }

        this.setup();

        Team team1 = player.getScoreboard().getTeam("\\u000181");
        if (team1 == null) {
            team1 = player.getScoreboard().registerNewTeam("\\u000181");
        }
        team1.addEntry(player.getName());
        for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
            Team team = loopPlayer.getScoreboard().getTeam("\\u000181");
            if (team == null) {
                team = loopPlayer.getScoreboard().registerNewTeam("\\u000181");
            }
            team.addEntry(player.getName());
            team.addEntry(loopPlayer.getName());
            team1.addEntry(loopPlayer.getName());
            team1.addEntry(player.getName());
        }

        PlayerTablist localTab = this;
        if (online && ProtocolSupportAPI.getProtocolVersion(player) != ProtocolVersion.MINECRAFT_1_8) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    for (Player loopPlayer : Bukkit.getOnlinePlayers()) {
                        getInstance().getImplementation().destoryFakePlayer(localTab, new TabEntry(
                                loopPlayer.getName(),
                                Bukkit.getOfflinePlayer(loopPlayer.getUniqueId()),
                                loopPlayer.getName(),
                                localTab,
                                ZigguratCommons.DEFAULT_TEXTURE,
                                TabColumn.LEFT,
                                1,
                                1,
                                1
                        ), loopPlayer.getName());
                    }
                }
            }.runTaskLater(instance.getPlugin(), 1);
        }
    }


    private void setup() {
        final int possibleSlots = (ProtocolSupportAPI.getProtocolVersion(player) == ProtocolVersion.MINECRAFT_1_8 ? 80 : 60);
        for (int i = 1; i <= possibleSlots; i++) {
            if (this.scoreboard == null || this.scoreboard != player.getScoreboard()) {
                continue;
            }
            final TabColumn tabColumn = TabColumn.getFromSlot(player, i);
            if (tabColumn == null) {
                continue;
            }
            TabEntry tabEntry = instance.getImplementation().createFakePlayer(
                    this,
                    "0" + (i > 9 ? i : "0" + i) + "|Tab",
                    tabColumn,
                    tabColumn.getNumb(player, i),
                    i
            );

            if (Bukkit.getPluginManager().getPlugin("Featherboard") == null && (ProtocolSupportAPI.getProtocolVersion(player).isBefore(ProtocolVersion.MINECRAFT_1_8))) {
                Team team = player.getScoreboard().getTeam(LegacyClientUtils.teamNames.get(i-1));
                if (team != null) {
                    team.unregister();
                }
                team = player.getScoreboard().registerNewTeam(LegacyClientUtils.teamNames.get(i-1));
                team.setPrefix("");
                team.setSuffix("");

                team.addEntry(LegacyClientUtils.tabEntrys.get(i - 1));

            }

            currentEntries.add(tabEntry);
        }
    }

    public void cleanup() {
        for (TabEntry tabEntry : getCurrentEntries()) {
            if (ProtocolSupportAPI.getProtocolVersion(player).isBefore(ProtocolVersion.MINECRAFT_1_8)) {
                Team team = player.getScoreboard().getTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot() - 1));
                if (team != null) {
                    team.unregister();
                }
            }

            getInstance().getImplementation().destoryFakePlayer(this, tabEntry, null);
        }
        if (!getInstance().isHook()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public void update() {
        Set<TabEntry> previous = new HashSet<>(currentEntries);
        Set<BufferedTabObject> processedObjects;
        //Null checker for twats
        if (instance.getAdapter().getSlots(player) == null) {
            processedObjects = new HashSet<>();
        } else {
            processedObjects = instance.getAdapter().getSlots(player);
        }
        for (BufferedTabObject scoreObject : processedObjects) {
            TabEntry tabEntry = getEntry(scoreObject.getColumn(), scoreObject.getSlot());
            if (tabEntry != null) {
                previous.remove(tabEntry);
                if (scoreObject.getPing() == null) {
                    instance.getImplementation().updateFakeLatency(this, tabEntry, getInstance().getPingProvider().getDefaultPing(player));
                } else {
                    instance.getImplementation().updateFakeLatency(this, tabEntry, scoreObject.getPing());
                }
                instance.getImplementation().updateFakeName(this, tabEntry, scoreObject.getText());
                if (ProtocolSupportAPI.getProtocolVersion(player).isAfter(ProtocolVersion.MINECRAFT_1_7_10)) {
                    if (!tabEntry.getTexture().toString().equals(scoreObject.getSkinTexture().toString())) {
                        instance.getImplementation().updateFakeSkin(this, tabEntry, scoreObject.getSkinTexture());
                    }
                }
            }
        }
        for (TabEntry tabEntry : previous) {
            instance.getImplementation().updateFakeName(this, tabEntry, "");
            instance.getImplementation().updateFakeLatency(this, tabEntry, getInstance().getPingProvider().getDefaultPing(player));
            if (ProtocolSupportAPI.getProtocolVersion(player).isAfter(ProtocolVersion.MINECRAFT_1_7_10)) {
                instance.getImplementation().updateFakeSkin(this, tabEntry, ZigguratCommons.DEFAULT_TEXTURE);
            }
        }
        previous.clear();

        if (player.getScoreboard() != scoreboard) {
            player.setScoreboard(scoreboard);
        }
    }

    public TabEntry getEntry(TabColumn column, Integer slot){
        for (TabEntry entry : currentEntries){
            if (entry.getColumn().name().equalsIgnoreCase(column.name()) && entry.getSlot() == slot){
                return entry;
            }
        }
        return null;
    }

    public static String[] splitStrings(String text, int rawSlot) {
        if (text.length() > 16) {
            String prefix = text.substring(0, 16);
            String suffix;

            if (prefix.charAt(15) == ChatColor.COLOR_CHAR || prefix.charAt(15) == '&') {
                prefix = prefix.substring(0, 15);
                suffix = text.substring(15, text.length());
            } else if (prefix.charAt(14) == ChatColor.COLOR_CHAR || prefix.charAt(14) == '&') {
                prefix = prefix.substring(0, 14);
                suffix = text.substring(14, text.length());
            } else {
                suffix = ChatColor.getLastColors(ChatColor.translateAlternateColorCodes('&',prefix)) + text.substring(16, text.length());
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            return new String[] {
                    prefix,
                    suffix
            };
        } else {
            return new String[] {
                    text
            };
        }
    }
}
