package com.blackmarket;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.level.Level;

/** Агент ГНК. Поведение стража-Vindicator: гонится за игроком и бьёт топором. */
public class AgentEntity extends Vindicator {
    public AgentEntity(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
    }
}
