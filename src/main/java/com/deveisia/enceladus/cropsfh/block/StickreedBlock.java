package com.deveisia.enceladus.cropsfh.block;

import com.deveisia.enceladus.cropsfh.Registration;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Stickreed (고무수수). Crop block used only for rendering/drop lookup inside the
 * CropsFH crop support; it has no item form and is not meant to be placed in the world.
 * Four growth stages (age 0-3), cross-shaped model.
 */
public class StickreedBlock extends CropBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public StickreedBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.SUGAR_CANE));
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return Registration.STICKREED_SEED.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AGE);
    }
}
