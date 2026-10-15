package com.deveisia.enceladus.cropsfh.integration.jei;

import com.deveisia.enceladus.cropsfh.client.CropPreviewClientCache;
import com.deveisia.enceladus.cropsfh.network.CropPreviewPacket;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CropRecipeInfo {
    private final ItemStack seed;
    private final int growSecondsOverride;
    private final List<CropPreviewPacket.Drop> drops;

    private CropRecipeInfo(ItemStack seed, int growSecondsOverride, List<CropPreviewPacket.Drop> drops) {
        this.seed = seed;
        this.growSecondsOverride = growSecondsOverride;
        this.drops = drops;
    }

    public ItemStack seed() {
        return seed;
    }

    public int growSecondsOverride() {
        return growSecondsOverride;
    }

    public List<CropPreviewPacket.Drop> drops() {
        return drops;
    }

    public static List<CropRecipeInfo> buildAll() {
        List<CropRecipeInfo> result = new ArrayList<>();
        for (CropPreviewPacket.Crop crop : CropPreviewClientCache.get().crops()) {
            if (crop.seed().isEmpty() || crop.drops().isEmpty()) continue;
            result.add(new CropRecipeInfo(crop.seed(), crop.growSeconds(), crop.drops()));
        }
        return result;
    }
}
