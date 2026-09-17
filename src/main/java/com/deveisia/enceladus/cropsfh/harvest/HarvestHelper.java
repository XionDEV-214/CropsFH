package com.deveisia.enceladus.cropsfh.harvest;

import com.deveisia.enceladus.cropsfh.block.CropSupportBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class HarvestHelper {
    public static boolean harvestAt(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return false;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CropSupportBlockEntity support)) return false;
        if (!support.hasCrop() || !support.isMature()) return false;
        support.harvest(player);
        return true;
    }

    public static int harvestArea(Level level, BlockPos center, int radius, Player player) {
        int harvested = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos target = center.offset(x, 0, z);
                if (harvestAt(level, target, player)) {
                    harvested++;
                }
            }
        }
        if (harvested > 0 && !level.isClientSide) {
            level.playSound(null, center, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 2.0f);
        }
        return harvested;
    }
}
