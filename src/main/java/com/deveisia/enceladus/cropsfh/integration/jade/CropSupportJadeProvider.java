package com.deveisia.enceladus.cropsfh.integration.jade;

import com.deveisia.enceladus.cropsfh.block.CropSupportBlock;
import com.deveisia.enceladus.cropsfh.block.CropSupportBlockEntity;
import com.deveisia.enceladus.cropsfh.crop.CropStats;
import com.deveisia.enceladus.cropsfh.crop.CropStatsHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class CropSupportJadeProvider implements IBlockComponentProvider {

    @Override
    public void appendTooltip(@NotNull ITooltip tooltip, @NotNull BlockAccessor accessor, @NotNull IPluginConfig config) {
        if (!(accessor.getBlock() instanceof CropSupportBlock)) return;

        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof CropSupportBlockEntity support)) return;
        if (!support.hasCrop()) return;

        ItemStack seed = support.getSeed();
        CropStats stats = support.getStats();

        tooltip.add(Component.translatable("cropsfh.jade.seed", seed.getHoverName()).withStyle(ChatFormatting.YELLOW));

        if (support.isMature()) {
            tooltip.add(Component.translatable("cropsfh.jade.mature").withStyle(ChatFormatting.GREEN));
        } else {
            int remainingTicks = Math.max(0, support.getRequiredGrowthTicks() - support.getGrowthTicks());
            int seconds = remainingTicks / 20;
            tooltip.add(Component.translatable("cropsfh.jade.time_remaining", seconds).withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable("cropsfh.jade.stats",
                stats.growth(), stats.gain(), (int) stats.resistance()).withStyle(ChatFormatting.AQUA));

        int degChance = (int) Math.round(CropStatsHelper.getDegenerationChance(stats) * 100);
        tooltip.add(Component.translatable("cropsfh.jade.degeneration", degChance).withStyle(ChatFormatting.RED));
    }

    @Override
    public @Nullable ResourceLocation getUid() {
        return new ResourceLocation("cropsfh", "crop_support");
    }
}
