package com.deveisia.enceladus.cropsfh.crop;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.config.CropsFHConfig;
import com.deveisia.enceladus.cropsfh.network.CropPreviewPacket;
import com.deveisia.enceladus.cropsfh.soil.SoilConfig;
import com.deveisia.enceladus.cropsfh.soil.SoilRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CropPreviewBuilder {
    private static final int SAMPLES = 200;

    private static volatile CropPreviewPacket cached;

    private CropPreviewBuilder() {
    }

    public static void invalidate() {
        cached = null;
    }

    public static CropPreviewPacket get(MinecraftServer server) {
        CropPreviewPacket packet = cached;
        if (packet == null) {
            packet = build(server);
            cached = packet;
        }
        return packet;
    }

    private static CropPreviewPacket build(MinecraftServer server) {
        ServerLevel level = server.overworld();

        List<CropPreviewPacket.Soil> soils = new ArrayList<>();
        for (SoilConfig soil : SoilRegistry.all()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(soil.block());
            if (id != null) {
                soils.add(new CropPreviewPacket.Soil(id, soil.growthSpeedModifier(), soil.gainMultiplier()));
            }
        }

        List<Item> seeds = new ArrayList<>(CropPlantHelper.getAllSeeds());
        seeds.sort(Comparator.comparing(item -> String.valueOf(ForgeRegistries.ITEMS.getKey(item))));

        List<CropPreviewPacket.Crop> crops = new ArrayList<>();
        for (Item seed : seeds) {
            try {
                ItemStack seedStack = new ItemStack(seed);
                Block block = CropPlantHelper.getCropBlock(seedStack);
                ResourceLocation blockId = block == null ? null : ForgeRegistries.BLOCKS.getKey(block);
                if (blockId == null) continue;

                List<CropPreviewPacket.Drop> drops = sampleDrops(level, seedStack);
                if (drops.isEmpty()) continue;

                crops.add(new CropPreviewPacket.Crop(seedStack, blockId, CropPlantHelper.getGrowTimeOverrideSeconds(seedStack), drops));
            } catch (Exception e) {
                CropsFHMod.LOGGER.debug("Skipping JEI preview for seed {}: {}", seed, e.getMessage());
            }
        }

        CropsFHMod.LOGGER.info("Built JEI crop preview for {} seeds.", crops.size());
        return new CropPreviewPacket(CropsFHConfig.BASE_GROWTH_TIME.get(), CropsFHConfig.MIN_GROWTH_TIME.get(), soils, crops);
    }

    private static List<CropPreviewPacket.Drop> sampleDrops(ServerLevel level, ItemStack seed) {
        Map<Item, int[]> stats = new HashMap<>();

        for (int i = 0; i < SAMPLES; i++) {
            Map<Item, Integer> roll = new HashMap<>();
            for (ItemStack drop : CropPlantHelper.rollBaseDrops(level, seed, Vec3.ZERO)) {
                if (!drop.isEmpty()) {
                    roll.merge(drop.getItem(), drop.getCount(), Integer::sum);
                }
            }
            for (Map.Entry<Item, Integer> entry : roll.entrySet()) {
                int[] stat = stats.computeIfAbsent(entry.getKey(), k -> new int[]{0, Integer.MAX_VALUE, 0});
                stat[0]++;
                stat[1] = Math.min(stat[1], entry.getValue());
                stat[2] = Math.max(stat[2], entry.getValue());
            }
        }

        List<CropPreviewPacket.Drop> drops = new ArrayList<>();
        for (Map.Entry<Item, int[]> entry : stats.entrySet()) {
            int[] stat = entry.getValue();
            drops.add(new CropPreviewPacket.Drop(new ItemStack(entry.getKey()), stat[1], stat[2], stat[0] / (float) SAMPLES));
        }

        ItemStack fruit = CropPlantHelper.getStemFruit(seed);
        if (!fruit.isEmpty() && drops.stream().noneMatch(d -> d.stack().is(fruit.getItem()))) {
            drops.add(new CropPreviewPacket.Drop(fruit.copy(), 1, 1, 1.0f));
        }

        drops.sort(Comparator.<CropPreviewPacket.Drop>comparingDouble(d -> -d.chance())
                .thenComparing(d -> String.valueOf(ForgeRegistries.ITEMS.getKey(d.stack().getItem()))));
        return drops;
    }
}
