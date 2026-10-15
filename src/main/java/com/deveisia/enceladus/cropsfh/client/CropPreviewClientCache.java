package com.deveisia.enceladus.cropsfh.client;

import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.deveisia.enceladus.cropsfh.network.CropPreviewPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CropPreviewClientCache {
    private static volatile CropPreviewPacket current = new CropPreviewPacket(1800, 10, List.of(), List.of());
    private static volatile int version = 0;
    private static volatile Runnable listener;

    private CropPreviewClientCache() {
    }

    public static void update(CropPreviewPacket packet) {
        current = packet;
        version++;

        Map<Item, CropPlantHelper.SyncedCrop> synced = new HashMap<>();
        for (CropPreviewPacket.Crop crop : packet.crops()) {
            Block block = ForgeRegistries.BLOCKS.getValue(crop.block());
            if (block != null && !crop.seed().isEmpty()) {
                synced.put(crop.seed().getItem(), new CropPlantHelper.SyncedCrop(block, crop.growSeconds()));
            }
        }
        CropPlantHelper.setSyncedCrops(synced);

        Runnable callback = listener;
        if (callback != null) {
            callback.run();
        }
    }

    public static CropPreviewPacket get() {
        return current;
    }

    public static int version() {
        return version;
    }

    public static void setListener(Runnable newListener) {
        listener = newListener;
    }

    public static List<ItemStack> soilStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (CropPreviewPacket.Soil soil : current.soils()) {
            Block block = ForgeRegistries.BLOCKS.getValue(soil.block());
            if (block != null) {
                ItemStack stack = new ItemStack(block);
                if (!stack.isEmpty()) {
                    stacks.add(stack);
                }
            }
        }
        return stacks;
    }

    public static CropPreviewPacket.Soil findSoil(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (CropPreviewPacket.Soil soil : current.soils()) {
            Block block = ForgeRegistries.BLOCKS.getValue(soil.block());
            if (block != null && block.asItem() == stack.getItem()) {
                return soil;
            }
        }
        return null;
    }
}
