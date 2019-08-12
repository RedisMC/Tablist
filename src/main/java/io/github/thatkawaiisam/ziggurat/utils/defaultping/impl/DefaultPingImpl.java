package io.github.thatkawaiisam.ziggurat.utils.defaultping.impl;

import io.github.thatkawaiisam.ziggurat.utils.defaultping.IPingProvider;
import org.bukkit.entity.Player;

public class DefaultPingImpl implements IPingProvider {

    @Override
    public int getDefaultPing(Player player) {
        return 0;
    }
}
