package com.deveisia.enceladus.cropsfh.crop;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class CropPlantHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CropPlantHelper.class);

    private static final TagKey<Item> IGNORED_SEEDS =
            TagKey.create(Registries.ITEM, new ResourceLocation(CropsFHMod.MOD_ID, "ignored_seeds"));

    private static final TagKey<Item> SAPLINGS =
            TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "saplings"));

    public record SyncedCrop(Block block, int growSeconds) {
    }

    private static volatile Map<Item, Block> seedToCrop = Map.of();
    private static volatile Map<Item, CropOverride> overrides = Map.of();
    private static volatile Map<Item, SyncedCrop> synced = Map.of();

    private static final Method CROP_SEED_METHOD = findCropSeedMethod();

    private static final List<Field> STEM_FIELDS = findStemFields();

    public static void discoverCrops() {
        Map<Item, Block> found = new HashMap<>();

        for (Item item : ForgeRegistries.ITEMS) {
            if (isIgnored(item)) continue;
            Block cropBlock = deriveCropBlock(item);
            if (cropBlock != null) {
                found.put(item, cropBlock);
            }
        }

        int fromBlocks = 0;
        if (CROP_SEED_METHOD != null) {
            for (Block block : ForgeRegistries.BLOCKS) {
                if (!(block instanceof CropBlock)) continue;
                Item seed = getBaseSeed(block);
                if (seed == null || seed == Items.AIR || isIgnored(seed) || found.containsKey(seed)) continue;
                found.put(seed, block);
                fromBlocks++;
            }
        }

        seedToCrop = Collections.unmodifiableMap(found);
        LOGGER.info("Discovered {} compatible crop seeds ({} found via crop blocks).", found.size(), fromBlocks);
    }

    private static boolean isIgnored(Item item) {
        return isSapling(item) || new ItemStack(item).is(IGNORED_SEEDS);
    }

    public static boolean isSapling(Item item) {
        if (item == Items.AIR) return false;
        if (new ItemStack(item).is(SAPLINGS)) return true;
        Block block = Block.byItem(item);
        return block instanceof SaplingBlock;
    }

    public static Block deriveCropBlock(Item item) {
        if (isSapling(item)) return null;

        Block block = Block.byItem(item);
        if (block != Blocks.AIR && isCropLike(block)) {
            return block;
        }

        if (item instanceof IPlantable plantable) {
            try {
                BlockState plantState = plantable.getPlant(null, BlockPos.ZERO);
                Block plant = plantState.getBlock();
                if (plant != Blocks.AIR && isCropLike(plant)) {
                    return plant;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    public static boolean isCropLike(BlockState state) {
        return isCropLike(state.getBlock());
    }

    public static boolean isCropLike(Block block) {
        if (block instanceof SaplingBlock) return false;
        return block instanceof CropBlock
                || block instanceof BushBlock
                || block instanceof StemBlock
                || block instanceof GrowingPlantBlock
                || block instanceof SugarCaneBlock
                || block instanceof CactusBlock
                || block instanceof CocoaBlock
                || block instanceof BambooStalkBlock;
    }

    public static void setOverrides(Map<Item, CropOverride> newOverrides) {
        overrides = Collections.unmodifiableMap(new HashMap<>(newOverrides));
    }

    public static void setSyncedCrops(Map<Item, SyncedCrop> newSynced) {
        synced = Collections.unmodifiableMap(new HashMap<>(newSynced));
    }

    public static Set<Item> getAllSeeds() {
        Set<Item> result = new HashSet<>(seedToCrop.keySet());
        for (CropOverride override : overrides.values()) {
            if (override.disabled() || isSapling(override.seed())) {
                result.remove(override.seed());
            } else {
                result.add(override.seed());
            }
        }
        result.removeIf(CropPlantHelper::isSapling);
        return result;
    }

    public static int getGrowTimeOverrideSeconds(ItemStack seed) {
        if (seed.isEmpty()) return -1;
        Item item = seed.getItem();
        CropOverride override = overrides.get(item);
        if (override != null) {
            return override.disabled() ? -1 : override.growTimeSeconds();
        }
        SyncedCrop syncedCrop = synced.get(item);
        return syncedCrop != null ? syncedCrop.growSeconds() : -1;
    }

    public static boolean isValidSeed(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (isSapling(item)) return false;
        CropOverride override = overrides.get(item);
        if (override != null) {
            return !override.disabled();
        }
        return seedToCrop.containsKey(item);
    }

    public static Block getCropBlock(ItemStack seed) {
        if (seed.isEmpty()) return null;
        Item item = seed.getItem();
        if (isSapling(item)) return null;

        CropOverride override = overrides.get(item);
        if (override != null) {
            return override.disabled() ? null : override.block();
        }

        SyncedCrop syncedCrop = synced.get(item);
        if (syncedCrop != null) return syncedCrop.block();

        Block cached = seedToCrop.get(item);
        if (cached != null) return cached;

        return deriveCropBlock(item);
    }

    public static String getSeedId(ItemStack seed) {
        return Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(seed.getItem())).toString();
    }

    public static BlockState getMatureState(Block cropBlock) {
        if (cropBlock instanceof CropBlock crop) {
            return crop.getStateForAge(crop.getMaxAge());
        }

        BlockState state = cropBlock.defaultBlockState();
        if (cropBlock.getStateDefinition().getProperty("age") instanceof IntegerProperty ageProp) {
            state = state.setValue(ageProp, Collections.max(ageProp.getPossibleValues()));
        }
        if (cropBlock.getStateDefinition().getProperty("berries") instanceof BooleanProperty berries) {
            state = state.setValue(berries, true);
        }
        if (cropBlock instanceof MultifaceBlock) {
            state = state.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
        }
        return state;
    }

    public static List<ItemStack> rollBaseDrops(ServerLevel level, ItemStack seed, Vec3 origin) {
        CropOverride override = seed.isEmpty() ? null : overrides.get(seed.getItem());
        if (override != null && !override.disabled() && override.drops() != null) {
            List<ItemStack> result = new ArrayList<>();
            for (CropOverride.Drop drop : override.drops()) {
                if (drop.chance() < 1.0f && level.random.nextFloat() >= drop.chance()) continue;
                int count = drop.min() >= drop.max()
                        ? drop.min()
                        : drop.min() + level.random.nextInt(drop.max() - drop.min() + 1);
                if (count > 0) {
                    result.add(new ItemStack(drop.item(), count));
                }
            }
            return result;
        }

        Block cropBlock = getCropBlock(seed);
        if (cropBlock == null) return new ArrayList<>();

        BlockState matureState = getMatureState(cropBlock);
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin)
                .withParameter(LootContextParams.BLOCK_STATE, matureState)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        return matureState.getDrops(builder);
    }

    public static ItemStack getStemFruit(ItemStack seed) {
        if (seed.isEmpty()) return ItemStack.EMPTY;
        Block cropBlock = getCropBlock(seed);
        if (cropBlock instanceof StemBlock stem) {
            Block fruit = getStemFruitBlock(stem);
            if (fruit != null && fruit.asItem() != Items.AIR) {
                return new ItemStack(fruit.asItem());
            }
        }
        return ItemStack.EMPTY;
    }

    private static Method findCropSeedMethod() {
        try {
            for (Method method : CropBlock.class.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())
                        && method.getParameterCount() == 0
                        && method.getReturnType() == ItemLike.class) {
                    method.setAccessible(true);
                    return method;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not access CropBlock seed getter, crop blocks will not be used for seed discovery.", e);
        }
        return null;
    }

    private static Item getBaseSeed(Block block) {
        try {
            Object value = CROP_SEED_METHOD.invoke(block);
            if (value instanceof ItemLike itemLike) {
                return itemLike.asItem();
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to read seed of crop block {}", block, e);
        }
        return null;
    }

    private static List<Field> findStemFields() {
        List<Field> fields = new ArrayList<>();
        try {
            for (Field field : StemBlock.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                Class<?> type = field.getType();
                if (Block.class.isAssignableFrom(type) || Supplier.class.isAssignableFrom(type)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not access StemBlock fields, stem fruit drops will not be detected.", e);
        }
        return fields;
    }

    private static Block getStemFruitBlock(StemBlock stem) {
        for (Field field : STEM_FIELDS) {
            try {
                Object value = field.get(stem);
                if (value instanceof Supplier<?> supplier) {
                    value = supplier.get();
                }
                if (value instanceof Block block && block != Blocks.AIR) {
                    return block;
                }
            } catch (Exception e) {
                LOGGER.debug("Failed to extract fruit from StemBlock {}", stem, e);
            }
        }
        return null;
    }
}
