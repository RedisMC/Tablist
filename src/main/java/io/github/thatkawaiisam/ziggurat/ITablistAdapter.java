package io.github.thatkawaiisam.ziggurat;

import io.github.thatkawaiisam.ziggurat.utils.BufferedTabObject;
import org.bukkit.entity.Player;

import java.util.Set;

public interface ITablistAdapter {

    Set<BufferedTabObject> getSlots(Player player);

    String getFooter();
    String getHeader();

}
