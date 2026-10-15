package com.deveisia.enceladus.cropsfh.integration.jei;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import com.deveisia.enceladus.cropsfh.Registration;
import com.deveisia.enceladus.cropsfh.client.CropPreviewClientCache;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class CropsFHJeiPlugin implements IModPlugin {
    public static final ResourceLocation UID = new ResourceLocation(CropsFHMod.MOD_ID, "jei_plugin");
    public static final RecipeType<CropRecipeInfo> CROP = RecipeType.create(CropsFHMod.MOD_ID, "crop", CropRecipeInfo.class);

    private static IJeiRuntime runtime;
    private static List<CropRecipeInfo> shown = List.of();
    private static int shownVersion = -1;

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CropRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Registration.CROP_SUPPORT_ITEM.get()), CROP);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        shownVersion = CropPreviewClientCache.version();
        shown = CropRecipeInfo.buildAll();
        registration.addRecipes(CROP, shown);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        CropPreviewClientCache.setListener(CropsFHJeiPlugin::refresh);

        if (shownVersion != CropPreviewClientCache.version()) {
            refresh();
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        CropPreviewClientCache.setListener(null);
    }

    private static void refresh() {
        IJeiRuntime jei = runtime;
        if (jei == null) return;

        IRecipeManager recipeManager = jei.getRecipeManager();
        if (!shown.isEmpty()) {
            recipeManager.hideRecipes(CROP, shown);
        }
        shown = CropRecipeInfo.buildAll();
        shownVersion = CropPreviewClientCache.version();
        if (!shown.isEmpty()) {
            recipeManager.addRecipes(CROP, shown);
        }
    }
}
