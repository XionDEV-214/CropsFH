package com.deveisia.enceladus.cropsfh.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class CropsFHConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue BASE_GROWTH_TIME;
    public static final ForgeConfigSpec.IntValue MIN_GROWTH_TIME;
    public static final ForgeConfigSpec.IntValue BREEDING_CHANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOIL_LIST;

    static {
        BUILDER.push("growth");
        BASE_GROWTH_TIME = BUILDER
                .comment("Base growth time in seconds for normal CropsFH crops.")
                .defineInRange("baseGrowthTime", 1800, 1, Integer.MAX_VALUE);
        MIN_GROWTH_TIME = BUILDER
                .comment("Minimum possible growth time in seconds.")
                .defineInRange("minGrowthTime", 10, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("soils");
        SOIL_LIST = BUILDER
                .comment("List of valid CropsFH soils. Format: modid:block,growthSpeedModifier,gainMultiplier")
                .defineList(
                        "soilList",
                        Arrays.asList("minecraft:farmland,1.0,1.0"),
                        CropsFHConfig::isValidSoilEntry
                );
        BUILDER.pop();

        BUILDER.push("breeding");
        BREEDING_CHANCE = BUILDER
                .comment("Chance for a cross crop support to breed a child crop on each random tick. Lower is faster; 1 = always attempt.")
                .defineInRange("breedingChance", 3, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private static boolean isValidSoilEntry(Object obj) {
        if (!(obj instanceof String str)) return false;
        String[] parts = str.split(",");
        if (parts.length != 3) return false;
        try {
            Double.parseDouble(parts[1].trim());
            Double.parseDouble(parts[2].trim());
        } catch (NumberFormatException e) {
            return false;
        }
        return !parts[0].trim().isEmpty();
    }
}
