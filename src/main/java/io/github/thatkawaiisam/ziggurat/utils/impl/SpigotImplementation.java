package io.github.thatkawaiisam.ziggurat.utils.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.github.thatkawaiisam.ziggurat.PlayerTablist;
import io.github.thatkawaiisam.ziggurat.ZigguratCommons;
import io.github.thatkawaiisam.ziggurat.utils.*;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.WorldSettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

import java.util.Map;
import java.util.UUID;

public class SpigotImplementation implements ITablistHelper {

    public SpigotImplementation() {
    }

    @Override
    public TabEntry createFakePlayer(PlayerTablist playerTablist, String string, TabColumn column, Integer slot, Integer rawSlot) {
        final OfflinePlayer offlinePlayer = new OfflinePlayer() {
            private final UUID uuid = UUID.randomUUID();

            @Override
            public boolean isOnline() {
                return true;
            }

            @Override
            public String getName() {
                return string;
            }

            @Override
            public UUID getUniqueId() {
                return uuid;
            }

            @Override
            public boolean isBanned() {
                return false;
            }

            @Override
            public void setBanned(boolean b) {

            }

            @Override
            public boolean isWhitelisted() {
                return false;
            }

            @Override
            public void setWhitelisted(boolean b) {

            }

            @Override
            public Player getPlayer() {
                return null;
            }

            @Override
            public long getFirstPlayed() {
                return 0;
            }

            @Override
            public long getLastPlayed() {
                return 0;
            }

            @Override
            public boolean hasPlayedBefore() {
                return false;
            }

            @Override
            public Location getBedSpawnLocation() {
                return null;
            }

            @Override
            public Map<String, Object> serialize() {
                return null;
            }

            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }
        };

        Player player = playerTablist.getPlayer();
        ProtocolVersion playerVersion = ProtocolSupportAPI.getProtocolVersion(player);

        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        packet.setAction(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), playerVersion.isAfter(ProtocolVersion.MINECRAFT_1_7_10) ? string : LegacyClientUtils.tabEntrys.get(rawSlot - 1) + "");

        PacketPlayOutPlayerInfo.PlayerInfoData e = new PacketPlayOutPlayerInfo.PlayerInfoData(profile, 1, WorldSettings.EnumGamemode.SURVIVAL, new ChatMessage(playerVersion == ProtocolVersion.MINECRAFT_1_8 ? "" : profile.getName()));
        if (playerVersion == ProtocolVersion.MINECRAFT_1_8) {
            profile.getProperties().put("texture", new Property("textures", ZigguratCommons.DEFAULT_TEXTURE.SKIN_VALUE, ZigguratCommons.DEFAULT_TEXTURE.SKIN_SIGNATURE));
        }

        packet.getData().clear();
        packet.getData().add(e);

        player.sendPacket(packet);

        return new TabEntry(string, offlinePlayer, "", playerTablist, ZigguratCommons.DEFAULT_TEXTURE, column, slot, rawSlot, 0);
    }

    @Override
    public void destoryFakePlayer(PlayerTablist playerTablist, TabEntry tabEntry, String customName) {
        Player player = playerTablist.getPlayer();
        ProtocolVersion playerVersion = ProtocolSupportAPI.getProtocolVersion(player);

        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        packet.setAction(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);

        GameProfile profile = new GameProfile(tabEntry.getOfflinePlayer().getUniqueId(), playerVersion == ProtocolVersion.MINECRAFT_1_8 ? tabEntry.getId() : LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + "");
        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = new PacketPlayOutPlayerInfo.PlayerInfoData(profile, 1, WorldSettings.EnumGamemode.SURVIVAL, new ChatMessage(playerVersion == ProtocolVersion.MINECRAFT_1_8 ? "" : profile.getName()));

        packet.getData().add(playerInfoData);
        player.sendPacket(packet);
    }

    @Override
    public void updateFakeName(PlayerTablist playerTablist, TabEntry tabEntry, String text) {
        Player player = playerTablist.getPlayer();
        ProtocolVersion playerVersion = ProtocolSupportAPI.getProtocolVersion(player);

        String[] newStrings = PlayerTablist.splitStrings(text, tabEntry.getRawSlot());
        if (playerVersion.isBefore(ProtocolVersion.MINECRAFT_1_8)) {
            Team team = player.getScoreboard().getTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot()-1));
            if (team == null) {
                team = player.getScoreboard().registerNewTeam(LegacyClientUtils.teamNames.get(tabEntry.getRawSlot()-1));
            }
            team.setPrefix(ChatColor.translateAlternateColorCodes('&', newStrings[0]));
            if (newStrings.length > 1) {
                team.setSuffix(ChatColor.translateAlternateColorCodes('&', newStrings[1]));
            } else {
                team.setSuffix("");
            }
        } else {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
            packet.setAction(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);

            GameProfile profile = new GameProfile(tabEntry.getOfflinePlayer().getUniqueId(), tabEntry.getId());

            PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = new PacketPlayOutPlayerInfo.PlayerInfoData(
                    profile,
                    1,
                    WorldSettings.EnumGamemode.SURVIVAL,
                    new ChatMessage(ChatColor.translateAlternateColorCodes('&', newStrings.length > 1 ? newStrings[0] + newStrings[1] : newStrings[0]))
            );

            packet.getData().add(playerInfoData);
            player.sendPacket(packet);
        }
        tabEntry.setText(text);
    }

    @Override
    public void updateFakeLatency(PlayerTablist playerTablist, TabEntry tabEntry, Integer latency) {
        if (tabEntry.getLatency() == latency) {
            return;
        }

        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
        packet.setAction(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY);

        GameProfile profile = new GameProfile(
                tabEntry.getOfflinePlayer().getUniqueId(),
                tabEntry.getId()
        );

        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = new PacketPlayOutPlayerInfo.PlayerInfoData(
                profile,
                latency,
                WorldSettings.EnumGamemode.SURVIVAL,
                new ChatMessage(ChatColor.translateAlternateColorCodes('&', tabEntry.getText()))
        );

        packet.getData().add(playerInfoData);
        playerTablist.getPlayer().sendPacket(packet);
        tabEntry.setLatency(latency);
    }

    @Override
    public void updateFakeSkin(PlayerTablist playerTablist, TabEntry tabEntry, SkinTexture skinTexture) {
        if (tabEntry.getTexture() == skinTexture) {
            return;
        }

        Player player = playerTablist.getPlayer();
        ProtocolVersion playerVersion = ProtocolSupportAPI.getProtocolVersion(player);

        GameProfile profile = new GameProfile(tabEntry.getOfflinePlayer().getUniqueId(), playerVersion == ProtocolVersion.MINECRAFT_1_8 ? tabEntry.getId() : LegacyClientUtils.tabEntrys.get(tabEntry.getRawSlot() - 1) + "");
        PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = new PacketPlayOutPlayerInfo.PlayerInfoData(profile, 1, WorldSettings.EnumGamemode.SURVIVAL, new ChatMessage(playerVersion == ProtocolVersion.MINECRAFT_1_8 ?  "" : profile.getName()));

        if (playerVersion == ProtocolVersion.MINECRAFT_1_8) {
            playerInfoData.getProfile().getProperties().put("texture", new Property("textures", skinTexture.SKIN_VALUE, skinTexture.SKIN_SIGNATURE));
        }

        PacketPlayOutPlayerInfo remove = new PacketPlayOutPlayerInfo();
        remove.setAction(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
        remove.getData().add(playerInfoData);

        PacketPlayOutPlayerInfo add = new PacketPlayOutPlayerInfo();
        add.setAction(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
        add.getData().add(playerInfoData);

        player.sendPacket(remove);
        player.sendPacket(add);

        tabEntry.setTexture(skinTexture);
    }

    @Override
    public void updateHeaderAndFooter(PlayerTablist playerTablist, String header, String footer) {
        /*PacketContainer headerAndFooter = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

        final Player player = playerTablist.getPlayer();
        final PlayerVersion playerVersion = PlayerUtility.getPlayerVersion(player);

        if (playerVersion != PlayerVersion.v1_7) {
            headerAndFooter.getChatComponents().write(0, WrappedChatComponent.fromText(header));
            headerAndFooter.getChatComponents().write(1, WrappedChatComponent.fromText(footer));

            sendPacket(player, headerAndFooter);
        }*/
    }
}
