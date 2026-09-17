package com.deveisia.enceladus.cropsfh.event;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.crop.CropSeedHelper;
import com.deveisia.enceladus.cropsfh.crop.CropStatsHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CropsFHMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {
    private static final TagKey<Item> SEEDS_TAG = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("forge", "seeds"));

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(SEEDS_TAG)) {
            CropStatsHelper.addTooltip(CropSeedHelper.getStats(stack), event.getToolTip());
        }
    }
}
