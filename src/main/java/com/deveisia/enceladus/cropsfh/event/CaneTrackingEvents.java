package com.deveisia.enceladus.cropsfh.event;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.world.PlantedCaneData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Tracks player-planted sugar cane so natural cane can be told apart. */
@Mod.EventBusSubscriber(modid = CropsFHMod.MOD_ID)
public class CaneTrackingEvents {
    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getPlacedBlock().is(Blocks.SUGAR_CANE)) return;
        if (event.getLevel() instanceof ServerLevel level) {
            PlantedCaneData.get(level).mark(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onGrow(BlockEvent.CropGrowEvent.Post event) {
        if (!event.getState().is(Blocks.SUGAR_CANE)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();
        BlockPos above = pos.above();
        if (!level.getBlockState(above).is(Blocks.SUGAR_CANE)) return;

        PlantedCaneData data = PlantedCaneData.get(level);
        if (data.isMarked(pos)) {
            data.mark(above);
        }
    }
}
