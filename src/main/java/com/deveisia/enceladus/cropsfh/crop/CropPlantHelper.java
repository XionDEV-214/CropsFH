package com.deveisia.enceladus.cropsfh.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CropPlantHelper {
    private static final Map<Item, Block> VANILLA_SEED_TO_CROP = new HashMap<>();

    static {
        VANILLA_SEED_TO_CROP.put(Items.WHEAT_SEEDS, Blocks.WHEAT);
        VANILLA_SEED_TO_CROP.put(Items.CARROT, Blocks.CARROTS);
        VANILLA_SEED_TO_CROP.put(Items.POTATO, Blocks.POTATOES);
        VANILLA_SEED_TO_CROP.put(Items.BEETROOT_SEEDS, Blocks.BEETROOTS);
        VANILLA_SEED_TO_CROP.put(Items.MELON_SEEDS, Blocks.MELON_STEM);
        VANILLA_SEED_TO_CROP.put(Items.PUMPKIN_SEEDS, Blocks.PUMPKIN_STEM);
        VANILLA_SEED_TO_CROP.put(Items.TORCHFLOWER_SEEDS, Blocks.TORCHFLOWER_CROP);
        VANILLA_SEED_TO_CROP.put(Items.PITCHER_POD, Blocks.PITCHER_CROP);
    }

    public static Block getCropBlock(ItemStack seed) {
        if (seed.isEmpty()) return null;
        Item item = seed.getItem();

        Block fromItem = Block.byItem(item);
        if (fromItem != Blocks.AIR && isCropLike(fromItem.defaultBlockState())) {
            return fromItem;
        }

        return VANILLA_SEED_TO_CROP.get(item);
    }

    public static boolean isCropLike(BlockState state) {
        return state.getBlock() instanceof CropBlock || state.getBlock() instanceof BushBlock;
    }

    public static BlockState getMatureState(Block cropBlock) {
        if (cropBlock instanceof CropBlock crop) {
            return crop.getStateForAge(crop.getMaxAge());
        }
        return cropBlock.defaultBlockState();
    }

    public static String getSeedId(ItemStack seed) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(seed.getItem())).toString();
    }
}
