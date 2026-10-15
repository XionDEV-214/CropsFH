package com.deveisia.enceladus.cropsfh.event;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.deveisia.enceladus.cropsfh.crop.CropSeedHelper;
import com.deveisia.enceladus.cropsfh.crop.CropStatsHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CropsFHMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (CropPlantHelper.isValidSeed(stack)) {
            CropStatsHelper.addTooltip(CropSeedHelper.getStats(stack), event.getToolTip());
        }
    }
}
