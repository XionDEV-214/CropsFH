package com.deveisia.enceladus.cropsfh;

import com.deveisia.enceladus.cropsfh.client.CropSupportBlockEntityRenderer;
import com.deveisia.enceladus.cropsfh.config.CropsFHConfig;
import com.deveisia.enceladus.cropsfh.machine.CropsFHGTAddon;
import com.deveisia.enceladus.cropsfh.machine.CropsFHMachines;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CropsFHMod.MOD_ID)
public class CropsFHMod {
    public static final String MOD_ID = "cropsfh";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CropsFHMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Registration.register(modEventBus);
        CropsFHGTAddon.REGISTRATE.registerRegistrate();
        modEventBus.register(CropsFHMachines.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CropsFHConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Common setup tasks can be enqueued here.
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        BlockEntityRenderers.register(Registration.CROP_SUPPORT_BE.get(), CropSupportBlockEntityRenderer::new);
    }
}
