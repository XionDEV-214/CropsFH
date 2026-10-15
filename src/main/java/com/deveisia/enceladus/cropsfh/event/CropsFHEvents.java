package com.deveisia.enceladus.cropsfh.event;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.command.CropsFHCommands;
import com.deveisia.enceladus.cropsfh.crop.CropOverrideLoader;
import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.deveisia.enceladus.cropsfh.crop.CropPreviewBuilder;
import com.deveisia.enceladus.cropsfh.network.CropPreviewPacket;
import com.deveisia.enceladus.cropsfh.network.CropsFHNetwork;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CropsFHMod.MOD_ID)
public class CropsFHEvents {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CropOverrideLoader());
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        CropPlantHelper.discoverCrops();
        CropPreviewBuilder.invalidate();
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        MinecraftServer server = event.getPlayerList().getServer();
        CropPreviewPacket packet = CropPreviewBuilder.get(server);
        if (event.getPlayer() != null) {
            CropsFHNetwork.sendTo(event.getPlayer(), packet);
        } else {
            CropsFHNetwork.sendToAll(packet);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CropsFHCommands.register(event.getDispatcher());
    }
}
