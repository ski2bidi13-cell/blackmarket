package com.blackmarket;

import net.minecraft.world.entity.player.Player;

/** Уровень розыска игрока (0–5 звёзд) и счётчик «затишья» для его снижения. */
public final class Heat {
    private static final String WANTED = "bm_wanted";
    private static final String CALM = "bm_calm";

    private Heat() {}

    public static int get(Player p) {
        return p.getPersistentData().getInt(WANTED);
    }

    public static void set(Player p, int value) {
        p.getPersistentData().putInt(WANTED, Math.max(0, Math.min(5, value)));
        p.getPersistentData().putInt(CALM, 0);
    }

    public static void add(Player p, int delta) {
        set(p, get(p) + delta);
    }

    public static int calm(Player p) {
        return p.getPersistentData().getInt(CALM);
    }

    public static void setCalm(Player p, int ticks) {
        p.getPersistentData().putInt(CALM, ticks);
    }
}
