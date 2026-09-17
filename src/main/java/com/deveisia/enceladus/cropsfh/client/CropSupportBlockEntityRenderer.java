package com.deveisia.enceladus.cropsfh.client;

import com.deveisia.enceladus.cropsfh.block.CropSupportBlockEntity;
import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class CropSupportBlockEntityRenderer implements BlockEntityRenderer<CropSupportBlockEntity> {

    public CropSupportBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull CropSupportBlockEntity be, float partialTicks, @NotNull PoseStack pose,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack seed = be.getSeed();
        if (seed.isEmpty()) return;

        Block cropBlock = CropPlantHelper.getCropBlock(seed);
        if (cropBlock == null || cropBlock == Blocks.AIR) return;

        BlockState state = getRenderedState(cropBlock, be.getGrowthProgress());

        pose.pushPose();
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, pose, buffer, packedLight, packedOverlay
        );
        pose.popPose();
    }

    private BlockState getRenderedState(Block cropBlock, float progress) {
        if (cropBlock instanceof CropBlock crop) {
            int maxAge = crop.getMaxAge();
            int age = Math.min(maxAge, Math.round(progress * maxAge));
            return crop.getStateForAge(age);
        }

        BlockState defaultState = cropBlock.defaultBlockState();
        for (Property<?> property : defaultState.getProperties()) {
            if (property instanceof IntegerProperty intProp && property.getName().equals("age")) {
                int max = intProp.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
                int age = Math.min(max, Math.round(progress * max));
                return defaultState.setValue(intProp, age);
            }
        }

        return defaultState;
    }
}
