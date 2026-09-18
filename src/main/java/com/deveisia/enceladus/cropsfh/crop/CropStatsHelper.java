package com.deveisia.enceladus.cropsfh.crop;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CropStatsHelper {
    private static final String TAG_GROWTH = "cropsfh_growth";
    private static final String TAG_GAIN = "cropsfh_gain";
    private static final String TAG_RESISTANCE = "cropsfh_resistance";

    private static final DecimalFormat RESISTANCE_FORMAT = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ROOT));

    public static CompoundTag write(CropStats stats) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_GROWTH, stats.growth());
        tag.putInt(TAG_GAIN, stats.gain());
        tag.putDouble(TAG_RESISTANCE, stats.resistance());
        return tag;
    }

    public static CropStats read(CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_GAIN)) {
            return CropStats.DEFAULT_SEED;
        }
        return new CropStats(
                tag.getInt(TAG_GROWTH),
                tag.getInt(TAG_GAIN),
                tag.getDouble(TAG_RESISTANCE)
        );
    }

    public static void addTooltip(CropStats stats, List<Component> tooltip) {
        tooltip.add(Component.literal("> ").withStyle(ChatFormatting.GREEN)
                .append(Component.translatable("tooltip.cropsfh.growth").append(" - " + stats.growth()).withStyle(ChatFormatting.GREEN)));
        tooltip.add(Component.literal("> ").withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("tooltip.cropsfh.gain").append(" - " + stats.gain()).withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("> ").withStyle(ChatFormatting.AQUA)
                .append(Component.translatable("tooltip.cropsfh.resistance").append(" - " + formatResistance(stats.resistance())).withStyle(ChatFormatting.AQUA)));
    }

    public static String formatResistance(double resistance) {
        return RESISTANCE_FORMAT.format(resistance);
    }

    public static double getGrowthMultiplier(CropStats stats) {
        return 1.0 + stats.growth() * 0.005;
    }

    public static double getDegenerationChance(CropStats stats) {
        return Math.max(0.0, 0.1 * (1.0 - stats.resistance() / CropStats.MAX_RESISTANCE));
    }
}
