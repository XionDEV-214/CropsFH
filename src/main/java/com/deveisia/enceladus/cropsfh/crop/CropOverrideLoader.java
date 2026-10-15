package com.deveisia.enceladus.cropsfh.crop;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropOverrideLoader extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "cropsfh_crops";
    private static final Gson GSON = new GsonBuilder().create();

    public CropOverrideLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Item, CropOverride> result = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
            try {
                CropOverride override = parse(entry.getKey(), entry.getValue());
                if (override != null) {
                    result.put(override.seed(), override);
                }
            } catch (Exception e) {
                CropsFHMod.LOGGER.error("Failed to load CropsFH crop definition {}: {}", entry.getKey(), e.getMessage());
            }
        }
        CropPlantHelper.setOverrides(result);
        CropPreviewBuilder.invalidate();
        CropsFHMod.LOGGER.info("Loaded {} CropsFH crop definitions.", result.size());
    }

    private static CropOverride parse(ResourceLocation fileId, JsonElement json) {
        JsonObject obj = GsonHelper.convertToJsonObject(json, "crop definition");

        Item seed = lookupItem(GsonHelper.getAsString(obj, "seed"));
        if (seed == null) {
            return null;
        }

        boolean disabled = GsonHelper.getAsBoolean(obj, "disabled", false);
        if (disabled) {
            return new CropOverride(seed, null, -1, null, true);
        }

        Block block;
        if (obj.has("block")) {
            block = lookupBlock(GsonHelper.getAsString(obj, "block"));
            if (block == null) {
                return null;
            }
        } else {
            block = CropPlantHelper.deriveCropBlock(seed);
            if (block == null) {
                CropsFHMod.LOGGER.warn("Crop definition {}: cannot derive a crop block from seed {}, add a \"block\" field.", fileId, obj.get("seed"));
                return null;
            }
        }

        int growTime = GsonHelper.getAsInt(obj, "grow_time", -1);

        List<CropOverride.Drop> drops = null;
        if (obj.has("drops")) {
            drops = new ArrayList<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(obj, "drops")) {
                JsonObject dropObj = GsonHelper.convertToJsonObject(element, "drop");
                Item item = lookupItem(GsonHelper.getAsString(dropObj, "item"));
                if (item == null) {
                    continue;
                }
                int min = Math.max(1, GsonHelper.getAsInt(dropObj, "min", 1));
                int max = Math.max(min, GsonHelper.getAsInt(dropObj, "max", min));
                float chance = Math.max(0.0f, Math.min(1.0f, GsonHelper.getAsFloat(dropObj, "chance", 1.0f)));
                drops.add(new CropOverride.Drop(item, min, max, chance));
            }
            if (drops.isEmpty()) {
                CropsFHMod.LOGGER.debug("Crop definition {} has no resolvable drops, skipping.", fileId);
                return null;
            }
        }

        return new CropOverride(seed, block, growTime, drops, false);
    }

    private static Item lookupItem(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.ITEMS.containsKey(rl)) return null;
        return ForgeRegistries.ITEMS.getValue(rl);
    }

    private static Block lookupBlock(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !ForgeRegistries.BLOCKS.containsKey(rl)) return null;
        Block block = ForgeRegistries.BLOCKS.getValue(rl);
        return block == Blocks.AIR ? null : block;
    }
}
