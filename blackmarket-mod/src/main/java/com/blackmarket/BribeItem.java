package com.blackmarket;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Взятка: обнуляет розыск и «отзывает» ближайших агентов. */
public class BribeItem extends Item {
    public BribeItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (Heat.get(player) <= 0) {
            player.displayClientMessage(Component.literal("Тебя никто не ищет. Взятка пока не нужна."), true);
            return InteractionResultHolder.fail(stack);
        }
        Heat.set(player, 0);
        level.getEntitiesOfClass(AgentEntity.class, player.getBoundingBox().inflate(96)).forEach(AgentEntity::discard);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (player instanceof ServerPlayer sp) {
            sp.displayClientMessage(Component.literal("Договорился. Агенты отозваны... ненадолго."), true);
        }
        return InteractionResultHolder.consume(stack);
    }
}
