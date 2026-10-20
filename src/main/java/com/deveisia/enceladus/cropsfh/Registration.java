package com.deveisia.enceladus.cropsfh;

import com.deveisia.enceladus.cropsfh.block.CropSupportBlock;
import com.deveisia.enceladus.cropsfh.block.CropSupportBlockEntity;
import com.deveisia.enceladus.cropsfh.block.CropSupportItem;
import com.deveisia.enceladus.cropsfh.block.CrossCropSupportBlock;
import com.deveisia.enceladus.cropsfh.block.StickreedBlock;
import com.deveisia.enceladus.cropsfh.loot.StickreedSeedLootModifier;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CropsFHMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CropsFHMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CropsFHMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CropsFHMod.MOD_ID);

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CropsFHMod.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> STICKREED_SEED_LOOT_MODIFIER =
            LOOT_MODIFIERS.register("stickreed_seed_from_cane", StickreedSeedLootModifier.CODEC);

    public static final RegistryObject<CropSupportBlock> CROP_SUPPORT = BLOCKS.register("crop_support", CropSupportBlock::new);
    public static final RegistryObject<Item> CROP_SUPPORT_ITEM = ITEMS.register("crop_support", () -> new CropSupportItem(CROP_SUPPORT.get()));

    public static final RegistryObject<CrossCropSupportBlock> CROSS_CROP_SUPPORT = BLOCKS.register("cross_crop_support", CrossCropSupportBlock::new);
    public static final RegistryObject<Item> CROSS_CROP_SUPPORT_ITEM = ITEMS.register("cross_crop_support", () -> new BlockItem(CROSS_CROP_SUPPORT.get(), new Item.Properties()));

    public static final RegistryObject<StickreedBlock> STICKREED = BLOCKS.register("stickreed", StickreedBlock::new);
    public static final RegistryObject<Item> STICKREED_SEED = ITEMS.register("stickreed_seed", () -> new Item(new Item.Properties()));

    public static final RegistryObject<BlockEntityType<CropSupportBlockEntity>> CROP_SUPPORT_BE = BLOCK_ENTITIES.register(
            "crop_support",
            () -> BlockEntityType.Builder.of(CropSupportBlockEntity::new, CROP_SUPPORT.get(), CROSS_CROP_SUPPORT.get()).build(null)
    );

    public static final RegistryObject<CreativeModeTab> CROPSFH_TAB = CREATIVE_MODE_TABS.register("cropsfh_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(CROP_SUPPORT_ITEM.get()))
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.cropsfh"))
                    .displayItems((parameters, output) -> {
                        output.accept(CROP_SUPPORT_ITEM.get());
                        output.accept(STICKREED_SEED.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        CREATIVE_MODE_TABS.register(bus);
        LOOT_MODIFIERS.register(bus);
    }
}
