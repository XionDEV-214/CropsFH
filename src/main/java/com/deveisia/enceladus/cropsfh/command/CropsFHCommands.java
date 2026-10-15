package com.deveisia.enceladus.cropsfh.command;

import com.deveisia.enceladus.cropsfh.crop.CropPlantHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CropsFHCommands {
    private static final TagKey<Item> FORGE_SEEDS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "seeds"));
    private static final int MAX_LISTED = 40;

    private CropsFHCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cropsfh")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("missing")
                        .executes(context -> listMissing(context.getSource()))));
    }

    private static int listMissing(CommandSourceStack source) {
        List<String> missing = new ArrayList<>();
        ITagManager<Item> tags = ForgeRegistries.ITEMS.tags();
        if (tags != null) {
            for (Item item : tags.getTag(FORGE_SEEDS)) {
                if (!CropPlantHelper.isValidSeed(new ItemStack(item))) {
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                    if (id != null) {
                        missing.add(id.toString());
                    }
                }
            }
        }
        Collections.sort(missing);

        int total = missing.size();
        source.sendSuccess(() -> Component.translatable("commands.cropsfh.missing.header", total), false);
        for (int i = 0; i < Math.min(total, MAX_LISTED); i++) {
            String id = missing.get(i);
            source.sendSuccess(() -> Component.literal(" - " + id), false);
        }
        if (total > MAX_LISTED) {
            int more = total - MAX_LISTED;
            source.sendSuccess(() -> Component.translatable("commands.cropsfh.missing.more", more), false);
        }
        return total;
    }
}
