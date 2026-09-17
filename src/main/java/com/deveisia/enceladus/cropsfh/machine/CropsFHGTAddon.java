package com.deveisia.enceladus.cropsfh.machine;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

@GTAddon
public class CropsFHGTAddon implements IGTAddon {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(CropsFHMod.MOD_ID);

    public CropsFHGTAddon() {
        // 머신 등록은 GTCEu가 MACHINES 레지스트리 등록 이벤트를 발행할 때
        // CropsFHMachines.registerMachines()에서 수행합니다.
    }

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        // Machines are registered in the constructor so they are added before
        // GTRegistries.MACHINES is frozen.
    }

    @Override
    public String addonModId() {
        return CropsFHMod.MOD_ID;
    }
}
