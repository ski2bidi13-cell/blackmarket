package com.blackmarket;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Ящик чёрного рынка.
 * ПКМ предметом — продать всю стопку. ПКМ пустой рукой — прайс.
 * Shift+ПКМ с 10+ грязными деньгами — купить взятку.
 */
public class MarketBlock extends Block {
    public MarketBlock(Properties props) {
        super(props);
    }

    private static int price(ItemStack s) {
        if (s.is(BlackMarketMod.ORGAN.get())) return 6;
        if (s.is(BlackMarketMod.GREEN_SMOKE.get())) return 10;
        if (s.is(BlackMarketMod.RED_DUST.get())) return 25;
        if (s.is(BlackMarketMod.BLUE_CRYSTAL.get())) return 60;
        return 0;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.is(BlackMarketMod.DIRTY_CASH.get())) {
            if (held.getCount() >= 10) {
                held.shrink(10);
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BlackMarketMod.BRIBE.get()));
                player.sendSystemMessage(Component.literal("Купил взятку за 10 грязных денег."));
            } else {
                player.sendSystemMessage(Component.literal("Взятка стоит 10 грязных денег."));
            }
            return InteractionResult.CONSUME;
        }

        int each = price(held);
        if (each > 0) {
            int total = each * held.getCount();
            boolean organ = held.is(BlackMarketMod.ORGAN.get());
            held.setCount(0);
            while (total > 0) {
                int n = Math.min(64, total);
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(BlackMarketMod.DIRTY_CASH.get(), n));
                total -= n;
            }
            if (!organ || level.random.nextBoolean()) {
                Heat.add(player, 1);
            }
            player.sendSystemMessage(Component.literal("Сделка закрыта. Только не светись."));
            return InteractionResult.CONSUME;
        }

        player.sendSystemMessage(Component.literal("— Прайс скупщика (за штуку) —"));
        player.sendSystemMessage(Component.literal("Сомнительный орган: 6   Зелёный Дым: 10"));
        player.sendSystemMessage(Component.literal("Красная Пыль: 25   Синий Кристалл: 60"));
        player.sendSystemMessage(Component.literal("Shift+ПКМ с 10 грязными деньгами — взятка (сброс розыска)"));
        return InteractionResult.CONSUME;
    }
}
