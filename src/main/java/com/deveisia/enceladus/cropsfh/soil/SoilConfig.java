package com.deveisia.enceladus.cropsfh.soil;

import net.minecraft.world.level.block.Block;

public record SoilConfig(Block block, double growthSpeedModifier, double gainMultiplier) {
    public static final double MIN_GROWTH_SPEED_MODIFIER = 0.01;
    public static final double MAX_GROWTH_SPEED_MODIFIER = Integer.MAX_VALUE;
    public static final double MIN_GAIN_MULTIPLIER = 0.01;
    public static final double MAX_GAIN_MULTIPLIER = Integer.MAX_VALUE;

    public SoilConfig {
        if (growthSpeedModifier <= 0) {
            growthSpeedModifier = 1.0;
        }
        growthSpeedModifier = Math.max(MIN_GROWTH_SPEED_MODIFIER, Math.min(growthSpeedModifier, MAX_GROWTH_SPEED_MODIFIER));
        gainMultiplier = Math.max(MIN_GAIN_MULTIPLIER, Math.min(gainMultiplier, MAX_GAIN_MULTIPLIER));
    }
}
