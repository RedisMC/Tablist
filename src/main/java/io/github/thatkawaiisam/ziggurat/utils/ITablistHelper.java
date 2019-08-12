package io.github.thatkawaiisam.ziggurat.utils;

import io.github.thatkawaiisam.ziggurat.PlayerTablist;

public interface ITablistHelper {

    TabEntry createFakePlayer(
            PlayerTablist playerTablist, String string, TabColumn column, Integer slot, Integer rawSlot
    );

    void updateFakeName(
            PlayerTablist playerTablist, TabEntry tabEntry, String text
    );

    void updateFakeLatency(
            PlayerTablist playerTablist, TabEntry tabEntry, Integer latency
    );

    void updateFakeSkin(
            PlayerTablist playerTablist, TabEntry tabEntry, SkinTexture skinTexture
    );

    void updateHeaderAndFooter(
            PlayerTablist playerTablist, String header, String footer
    );

    void destoryFakePlayer(
            PlayerTablist playerTablist, TabEntry tabEntry, String customName
    );
}
