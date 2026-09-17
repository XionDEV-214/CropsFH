package com.deveisia.enceladus.cropsfh.soil;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.config.CropsFHConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = CropsFHMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoilRegistry {
    private static final Map<Block, SoilConfig> SOILS = new HashMap<>();

    public static final SoilConfig DEFAULT = new SoilConfig(Blocks.AIR, 1.0, 1.0);

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CropsFHConfig.SPEC) {
            reload();
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CropsFHConfig.SPEC) {
            reload();
        }
    }

    public static void reload() {
        SOILS.clear();
        for (String entry : CropsFHConfig.SOIL_LIST.get()) {
            try {
                SoilConfig config = parse(entry);
                SOILS.put(config.block(), config);
            } catch (Exception e) {
                CropsFHMod.LOGGER.error("Failed to load soil entry '{}': {}", entry, e.getMessage());
            }
        }

        if (SOILS.isEmpty()) {
            CropsFHMod.LOGGER.warn("No valid CropsFH soil configs loaded. Crop supports will not be placeable.");
        }
    }

    private static SoilConfig parse(String entry) {
        String[] parts = entry.split(",", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Expected format modid:block,growthSpeedModifier,gainMultiplier");
        }

        String blockId = parts[0].trim();
        double growthSpeedModifier = Double.parseDouble(parts[1].trim());
        double gainMultiplier = Double.parseDouble(parts[2].trim());

        ResourceLocation id = ResourceLocation.tryParse(blockId);
        if (id == null) {
            throw new IllegalArgumentException("Invalid block id: " + blockId);
        }

        Block block = ForgeRegistries.BLOCKS.getValue(id);
        if (block == null || block == Blocks.AIR) {
            throw new IllegalArgumentException("Unknown block: " + blockId);
        }

        return new SoilConfig(block, growthSpeedModifier, gainMultiplier);
    }

    public static boolean isValidSoil(Block block) {
        return SOILS.containsKey(block);
    }

    public static SoilConfig get(Block block) {
        return SOILS.getOrDefault(block, DEFAULT);
    }
}
