package com.blackmarket;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BlackMarketMod.MODID)
public class GameEvents {

    /** Розыск: спавн агентов, снижение, строка статуса. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        if (p.tickCount % 20 != 0) return;

        int wanted = Heat.get(p);
        if (wanted <= 0) return;

        ServerLevel level = p.serverLevel();
        List<AgentEntity> near = level.getEntitiesOfClass(AgentEntity.class, p.getBoundingBox().inflate(48));

        // Побег: 60 секунд без агентов рядом = -1 звезда
        int calm = near.isEmpty() ? Heat.calm(p) + 20 : 0;
        if (calm >= 1200) {
            Heat.add(p, -1);
            calm = 0;
            if (Heat.get(p) == 0) {
                level.getEntitiesOfClass(AgentEntity.class, p.getBoundingBox().inflate(128)).forEach(AgentEntity::discard);
                p.displayClientMessage(Component.literal("Оторвался! Розыск снят."), true);
                return;
            }
        }
        Heat.setCalm(p, calm);
        wanted = Heat.get(p);

        // Спавн новых агентов каждые 5 секунд
        if (p.tickCount % 100 == 0
                && level.dimension() == Level.OVERWORLD
                && level.getDifficulty() != Difficulty.PEACEFUL
                && near.size() < wanted * 2) {
            spawnAgent(level, p, wanted);
        }

        // Статус в action bar
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) stars.append(i <= wanted ? "★" : "☆");
        int left = Math.max(0, (1200 - Heat.calm(p)) / 20);
        p.displayClientMessage(Component.literal("Розыск " + stars + "  агентов рядом: " + near.size()
                + (near.isEmpty() ? "  скрыт ещё " + left + " c" : "")), true);
    }

    private static void spawnAgent(ServerLevel level, ServerPlayer p, int wanted) {
        double ang = level.random.nextDouble() * Math.PI * 2;
        int dist = 24 + level.random.nextInt(8);
        int x = (int) (p.getX() + Math.cos(ang) * dist);
        int z = (int) (p.getZ() + Math.sin(ang) * dist);
        if (!level.hasChunkAt(new BlockPos(x, 64, z))) return;
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.getFluidState(pos.below()).isEmpty()) return;

        AgentEntity a = BlackMarketMod.AGENT.get().create(level);
        if (a == null) return;
        a.moveTo(x + 0.5, y, z + 0.5, level.random.nextFloat() * 360f, 0f);
        a.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null, null);
        if (wanted >= 3) {
            a.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, Integer.MAX_VALUE, wanted >= 5 ? 1 : 0, false, false));
        }
        if (wanted >= 5) {
            a.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        }
        a.setTarget(p);
        level.addFreshEntity(a);
    }

    /** Убил агента — розыск растёт, но из кармана падают грязные деньги. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof AgentEntity && e.getSource().getEntity() instanceof ServerPlayer p) {
            Heat.add(p, 1);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent e) {
        if (!(e.getSource().getEntity() instanceof Player)) return;
        Level level = e.getEntity().level();
        double x = e.getEntity().getX(), y = e.getEntity().getY(), z = e.getEntity().getZ();

        if (e.getEntity() instanceof AgentEntity) {
            int n = 2 + level.random.nextInt(4);
            e.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(BlackMarketMod.DIRTY_CASH.get(), n)));
        } else if (e.getEntity() instanceof Monster) {
            float chance = 0.2f + 0.1f * e.getLootingLevel();
            if (level.random.nextFloat() < chance) {
                e.getDrops().add(new ItemEntity(level, x, y, z, new ItemStack(BlackMarketMod.ORGAN.get())));
            }
        }
    }
}
