package com.blackmarket;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Выдуманное «вещество»: даёт эффекты и с некоторым шансом повышает розыск. */
public class DrugItem extends Item {
    private final float heatChance;
    private final Supplier<List<MobEffectInstance>> effects;

    public DrugItem(Properties props, float heatChance, Supplier<List<MobEffectInstance>> effects) {
        super(props);
        this.heatChance = heatChance;
        this.effects = effects;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            for (MobEffectInstance e : effects.get()) {
                player.addEffect(new MobEffectInstance(e));
            }
            if (level.random.nextFloat() < heatChance) {
                Heat.add(player, 1);
            }
        }
        if (!(entity instanceof Player p && p.getAbilities().instabuild)) {
            stack.shrink(1);
        }
        return stack;
    }
}
