package io.github.alphahelixdev.alpary.reflection.nms.enums;

import io.github.alphahelixdev.alpary.utils.Utils;

import java.io.Serializable;

public enum REnumPlayerInfoAction implements Serializable {

    ADD_PLAYER(0), UPDATE_GAME_MODE(1), UPDATE_LATENCY(2), UPDATE_DISPLAY_NAME(3), REMOVE_PLAYER(4);

    private final int index;

    REnumPlayerInfoAction(int enumIndex) {
        this.index = enumIndex;
    }

    public Object getPlayerInfoAction() {
        return Utils.nms().getNMSEnumConstant("PacketPlayOutPlayerInfo$EnumPlayerInfoAction", index);
    }

    @Override
    public String toString() {
        return "REnumPlayerInfoAction{" +
                "index=" + this.index +
                '}';
    }
}