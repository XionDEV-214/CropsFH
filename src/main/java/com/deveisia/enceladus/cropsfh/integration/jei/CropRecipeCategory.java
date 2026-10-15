package com.deveisia.enceladus.cropsfh.integration.jei;

import com.deveisia.enceladus.cropsfh.Registration;
import com.deveisia.enceladus.cropsfh.client.CropPreviewClientCache;
import com.deveisia.enceladus.cropsfh.network.CropPreviewPacket;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CropRecipeCategory implements IRecipeCategory<CropRecipeInfo> {
    private static final int MAX_DROP_SLOTS = 12;
    private static final DecimalFormat NUMBER = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private final Component title;
    private final IDrawable icon;
    private final IDrawableStatic background;
    private final IDrawableStatic slot;

    public CropRecipeCategory(IGuiHelper gui) {
        this.title = Component.translatable("jei.cropsfh.category.crop");
        this.icon = gui.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.CROP_SUPPORT_ITEM.get()));
        this.background = gui.createBlankDrawable(155, 57);
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<CropRecipeInfo> getRecipeType() {
        return CropsFHJeiPlugin.CROP;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CropRecipeInfo recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 31, 11).addItemStack(recipe.seed());

        List<ItemStack> soils = CropPreviewClientCache.soilStacks();
        if (!soils.isEmpty()) {
            IRecipeSlotBuilder soilSlot = builder.addSlot(RecipeIngredientRole.INPUT, 31, 29);
            soilSlot.addItemStacks(soils);
            soilSlot.addTooltipCallback(soilTooltip(recipe));
        }

        List<CropPreviewPacket.Drop> drops = recipe.drops();
        for (int i = 0; i < Math.min(MAX_DROP_SLOTS, drops.size()); i++) {
            CropPreviewPacket.Drop drop = drops.get(i);
            IRecipeSlotBuilder dropSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 81 + 18 * (i % 4), 1 + 18 * (i / 4));
            dropSlot.addItemStack(drop.stack());
            dropSlot.addTooltipCallback(dropTooltip(drop));
        }
    }

    @Override
    public void draw(CropRecipeInfo recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        slot.draw(graphics, 30, 10);
        slot.draw(graphics, 30, 28);
        for (int i = 0; i < MAX_DROP_SLOTS; i++) {
            slot.draw(graphics, 80 + 18 * (i % 4), 18 * (i / 4));
        }
    }

    private static IRecipeSlotTooltipCallback soilTooltip(CropRecipeInfo recipe) {
        return (view, tooltip) -> view.getDisplayedItemStack().ifPresent(stack -> {
            CropPreviewPacket.Soil soil = CropPreviewClientCache.findSoil(stack);
            if (soil == null) return;

            CropPreviewPacket packet = CropPreviewClientCache.get();
            int base = recipe.growSecondsOverride() > 0 ? recipe.growSecondsOverride() : packet.baseGrowSeconds();
            double seconds = Math.max(packet.minGrowSeconds(), base / soil.growthSpeed());

            tooltip.add(Component.translatable("jei.cropsfh.tooltip.grow_time", formatTime((int) Math.round(seconds))).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jei.cropsfh.tooltip.soil_speed", NUMBER.format(soil.growthSpeed())).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jei.cropsfh.tooltip.soil_gain", NUMBER.format(soil.gainMultiplier())).withStyle(ChatFormatting.GRAY));
        });
    }

    private static IRecipeSlotTooltipCallback dropTooltip(CropPreviewPacket.Drop drop) {
        return (view, tooltip) -> {
            tooltip.add(Component.translatable("jei.cropsfh.tooltip.chance", NUMBER.format(drop.chance() * 100.0)).withStyle(ChatFormatting.GRAY));
            if (drop.min() == drop.max()) {
                tooltip.add(Component.translatable("jei.cropsfh.tooltip.amount", drop.min()).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("jei.cropsfh.tooltip.amount_range", drop.min(), drop.max()).withStyle(ChatFormatting.GRAY));
            }
        };
    }

    private static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
}
