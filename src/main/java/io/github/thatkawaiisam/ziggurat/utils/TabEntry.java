package io.github.thatkawaiisam.ziggurat.utils;

import io.github.thatkawaiisam.ziggurat.PlayerTablist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

@Getter
@Setter
@AllArgsConstructor
public class TabEntry {

    private String id;
    private OfflinePlayer offlinePlayer;
    private String text;
    private PlayerTablist tab;
    private SkinTexture texture;
    private TabColumn column;
    private int slot, rawSlot, latency;

}
