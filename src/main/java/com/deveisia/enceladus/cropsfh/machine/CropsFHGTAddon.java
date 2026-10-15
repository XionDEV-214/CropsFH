package com.deveisia.enceladus.cropsfh.machine;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

@GTAddon
public class CropsFHGTAddon implements IGTAddon {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(CropsFHMod.MOD_ID);

    public CropsFHGTAddon() {
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {
    }

    @Override
    public String addonModId() {
        return CropsFHMod.MOD_ID;
    }
}
