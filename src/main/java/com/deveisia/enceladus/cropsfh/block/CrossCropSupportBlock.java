package com.deveisia.enceladus.cropsfh.block;

import com.deveisia.enceladus.cropsfh.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class CrossCropSupportBlock extends CropSupportBlock {
    public CrossCropSupportBlock() {
        super();
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder params) {
        return Collections.singletonList(new ItemStack(Registration.CROP_SUPPORT_ITEM.get(), 2));
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CropSupportBlockEntity support) {
            support.randomTick(random);
            if (!support.hasCrop()) {
                support.attemptBreed();
            }
        }
    }
}
