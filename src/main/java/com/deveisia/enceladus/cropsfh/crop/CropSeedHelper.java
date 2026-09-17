package com.deveisia.enceladus.cropsfh.crop;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class CropSeedHelper {
    public static final String TAG_ROOT = "cropsfh_stats";

    public static boolean hasStats(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains(TAG_ROOT);
    }

    public static CropStats getStats(ItemStack stack) {
        if (!hasStats(stack)) {
            return CropStats.DEFAULT_SEED;
        }
        return CropStatsHelper.read(Objects.requireNonNull(stack.getTag()).getCompound(TAG_ROOT));
    }

    public static void setStats(ItemStack stack, CropStats stats) {
        if (stack == null || stack.isEmpty()) return;
        stack.getOrCreateTag().put(TAG_ROOT, CropStatsHelper.write(stats));
    }
}
