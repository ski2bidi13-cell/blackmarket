package com.blackmarket;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(BlackMarketMod.MODID)
public class BlackMarketMod {
    public static final String MODID = "blackmarket";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Валюта и «товар»
    public static final RegistryObject<Item> DIRTY_CASH = ITEMS.register("dirty_cash", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ORGAN = ITEMS.register("organ", () -> new Item(new Item.Properties().stacksTo(16)));

    // Выдуманные вещества (никакой реальной химии — чисто игровые эффекты)
    public static final RegistryObject<Item> GREEN_SMOKE = ITEMS.register("green_smoke", () ->
            new DrugItem(new Item.Properties().stacksTo(16), 0.4f, () -> List.of(
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 1),
                    new MobEffectInstance(MobEffects.JUMP, 2400, 1),
                    new MobEffectInstance(MobEffects.CONFUSION, 200, 0))));
    public static final RegistryObject<Item> RED_DUST = ITEMS.register("red_dust", () ->
            new DrugItem(new Item.Properties().stacksTo(16), 0.7f, () -> List.of(
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 1),
                    new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1),
                    new MobEffectInstance(MobEffects.HUNGER, 600, 1))));
    public static final RegistryObject<Item> BLUE_CRYSTAL = ITEMS.register("blue_crystal", () ->
            new DrugItem(new Item.Properties().stacksTo(16), 1.0f, () -> List.of(
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 2),
                    new MobEffectInstance(MobEffects.NIGHT_VISION, 2400, 0),
                    new MobEffectInstance(MobEffects.REGENERATION, 200, 1),
                    new MobEffectInstance(MobEffects.WEAKNESS, 1200, 0))));

    public static final RegistryObject<Item> BRIBE = ITEMS.register("bribe", () -> new BribeItem(new Item.Properties().stacksTo(8)));

    // Блок «чёрный рынок»
    public static final RegistryObject<Block> MARKET_CRATE = BLOCKS.register("market_crate", () ->
            new MarketBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(2.0f).sound(SoundType.WOOD)));
    public static final RegistryObject<Item> MARKET_CRATE_ITEM = ITEMS.register("market_crate", () ->
            new BlockItem(MARKET_CRATE.get(), new Item.Properties()));

    // Агент ГНК (внешне — обычный страж, внутри — наш класс)
    public static final RegistryObject<EntityType<AgentEntity>> AGENT = ENTITIES.register("agent", () ->
            EntityType.Builder.<AgentEntity>of(AgentEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f).clientTrackingRange(8).build("agent"));

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.blackmarket"))
                    .icon(() -> new ItemStack(DIRTY_CASH.get()))
                    .displayItems((params, out) -> ITEMS.getEntries().forEach(e -> out.accept(e.get())))
                    .build());

    public BlackMarketMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        BLOCKS.register(bus);
        ENTITIES.register(bus);
        TABS.register(bus);
        bus.addListener(this::attributes);
    }

    private void attributes(EntityAttributeCreationEvent event) {
        event.put(AGENT.get(), Vindicator.createAttributes().build());
    }
}
