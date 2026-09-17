package com.deveisia.enceladus.cropsfh.block;

import com.deveisia.enceladus.cropsfh.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CropSupportItem extends BlockItem {
    public CropSupportItem(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            BlockPos pos = context.getClickedPos();
            Level level = context.getLevel();
            BlockState existing = level.getBlockState(pos);
            if (existing.is(Registration.CROP_SUPPORT.get())) {
                level.setBlockAndUpdate(pos, Registration.CROSS_CROP_SUPPORT.get().defaultBlockState());
                if (!level.isClientSide) {
                    level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 2.0f);
                }
                if (!context.getPlayer().isCreative()) {
                    context.getItemInHand().shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            BlockPlaceContext modified = new BlockPlaceContext(
                    context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(),
                    new net.minecraft.world.phys.BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()));
            InteractionResult result = ((BlockItem) Registration.CROSS_CROP_SUPPORT_ITEM.get()).place(modified);
            if (result.consumesAction() && !level.isClientSide) {
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 2.0f);
            }
            return result;
        }
        return super.place(context);
    }
}
