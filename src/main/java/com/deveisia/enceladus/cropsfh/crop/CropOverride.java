package com.deveisia.enceladus.cropsfh.crop;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record CropOverride(Item seed, @Nullable Block block, int growTimeSeconds,
                           @Nullable List<Drop> drops, boolean disabled) {
    public record Drop(Item item, int min, int max, float chance) {
    }
}
