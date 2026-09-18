package com.deveisia.enceladus.cropsfh.block;

import com.deveisia.enceladus.cropsfh.Registration;
import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.deveisia.enceladus.cropsfh.soil.SoilRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class CropSupportBlock extends BaseEntityBlock {
    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public CropSupportBlock() {
        super(Properties.of().noCollission().strength(0.5f).sound(SoundType.WOOD).noOcclusion().randomTicks());
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder params) {
        return Collections.singletonList(new ItemStack(Registration.CROP_SUPPORT_ITEM.get()));
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        return SoilRegistry.isValidSoil(level.getBlockState(pos.below()).getBlock());
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide && !canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CropSupportBlockEntity support)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && held.isEmpty()) {
            support.removeCrop(false);
            level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 2.0f);
            return InteractionResult.SUCCESS;
        }

        if (state.is(Registration.CROP_SUPPORT.get()) && !support.hasCrop() && held.is(Registration.CROP_SUPPORT_ITEM.get())) {
            level.setBlockAndUpdate(pos, Registration.CROSS_CROP_SUPPORT.get().defaultBlockState());
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 2.0f);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        if (!support.hasCrop() && !held.isEmpty()) {
            if (state.is(Registration.CROSS_CROP_SUPPORT.get())) return InteractionResult.PASS;
            if (CropPlantHelper.getCropBlock(held) != null) {
                support.plantSeed(held);
                level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 2.0f);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (support.hasCrop() && support.isMature()) {
            support.harvest(player);
            level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 2.0f);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull net.minecraft.util.RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CropSupportBlockEntity support) {
            support.randomTick(random);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CropSupportBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, Registration.CROP_SUPPORT_BE.get(), (lvl, pos, st, be) -> be.serverTick());
    }
}
