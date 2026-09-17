package com.deveisia.enceladus.cropsfh.machine;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.network.chat.Component;

import java.util.Locale;

import static com.gregtechceu.gtceu.api.GTValues.*;

public class CropsFHMachines {
    public static final int[] HARVESTER_TIERS = new int[]{LV, MV, HV, EV, IV};

    public static MachineDefinition[] CROP_HARVESTER = new MachineDefinition[GTValues.TIER_COUNT];
    private static boolean initialized = false;

    @SubscribeEvent
    public static void registerMachines(GTCEuAPI.RegisterEvent event) {
        if (initialized || event.getGenericType() != MachineDefinition.class) return;
        init();
        initialized = true;
    }

    public static void init() {
        for (int tier : HARVESTER_TIERS) {
            int range = 2 * (tier + 1) + 1; // LV=5, MV=7, HV=9, EV=11, IV=13
            int slots = (tier + 4) * (tier + 4); // LV=5x5, MV=6x6, HV=7x7, EV=8x8, IV=9x9
            String name = GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_crop_harvester";

            MachineBuilder<MachineDefinition, ?> builder = CropsFHGTAddon.REGISTRATE.machine(name, info -> new CropHarvesterMachine(info, tier, range, slots));
            CROP_HARVESTER[tier] = builder
                    .tier(tier)
                    .rotationState(RotationState.NON_Y_AXIS)
                    .recipeType(GTRecipeTypes.DUMMY_RECIPES)
                    .workableTieredHullModel(com.gregtechceu.gtceu.GTCEu.id("block/machines/extractor"))
                    .itemBuilder(itemBuilder -> itemBuilder.tab(GTCreativeModeTabs.MACHINE.getKey()))
                    .tooltips(
                            Component.translatable("block.cropsfh.crop_harvester.tooltip"),
                            Component.translatable("block.cropsfh.crop_harvester.range", range, range),
                            Component.translatable("block.cropsfh.crop_harvester.slots", slots),
                            Component.translatable("gtceu.universal.tooltip.voltage_in", FormattingUtil.formatNumbers(V[tier]), VNF[tier])
                    )
                    .register();
        }
    }
}
