package com.deveisia.enceladus.cropsfh.harvest;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CropsFHMod.MOD_ID)
public class ScytheHarvestHandler {
    private static final int RADIUS = 2;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack stack = player.getItemInHand(hand);

        if (!isScythe(stack)) return;

        BlockPos clickedPos = event.getPos();
        if (HarvestHelper.harvestArea(player.level(), clickedPos, RADIUS, player) > 0) {
            event.setCanceled(true);
        }
    }

    private static boolean isScythe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof com.gregtechceu.gtceu.api.item.IGTTool gtTool) {
            return gtTool.getToolType() == GTToolType.SCYTHE;
        }
        return false;
    }
}
