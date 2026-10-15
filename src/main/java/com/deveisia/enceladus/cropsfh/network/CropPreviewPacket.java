package com.deveisia.enceladus.cropsfh.network;

import com.deveisia.enceladus.cropsfh.client.CropPreviewClientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class CropPreviewPacket {
    public record Drop(ItemStack stack, int min, int max, float chance) {
    }

    public record Crop(ItemStack seed, ResourceLocation block, int growSeconds, List<Drop> drops) {
    }

    public record Soil(ResourceLocation block, double growthSpeed, double gainMultiplier) {
    }

    private final int baseGrowSeconds;
    private final int minGrowSeconds;
    private final List<Soil> soils;
    private final List<Crop> crops;

    public CropPreviewPacket(int baseGrowSeconds, int minGrowSeconds, List<Soil> soils, List<Crop> crops) {
        this.baseGrowSeconds = baseGrowSeconds;
        this.minGrowSeconds = minGrowSeconds;
        this.soils = soils;
        this.crops = crops;
    }

    public int baseGrowSeconds() {
        return baseGrowSeconds;
    }

    public int minGrowSeconds() {
        return minGrowSeconds;
    }

    public List<Soil> soils() {
        return soils;
    }

    public List<Crop> crops() {
        return crops;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(baseGrowSeconds);
        buf.writeVarInt(minGrowSeconds);

        buf.writeVarInt(soils.size());
        for (Soil soil : soils) {
            buf.writeResourceLocation(soil.block());
            buf.writeDouble(soil.growthSpeed());
            buf.writeDouble(soil.gainMultiplier());
        }

        buf.writeVarInt(crops.size());
        for (Crop crop : crops) {
            buf.writeItem(crop.seed());
            buf.writeResourceLocation(crop.block());
            buf.writeInt(crop.growSeconds());
            buf.writeVarInt(crop.drops().size());
            for (Drop drop : crop.drops()) {
                buf.writeItem(drop.stack());
                buf.writeVarInt(drop.min());
                buf.writeVarInt(drop.max());
                buf.writeFloat(drop.chance());
            }
        }
    }

    public static CropPreviewPacket decode(FriendlyByteBuf buf) {
        int base = buf.readVarInt();
        int min = buf.readVarInt();

        int soilCount = buf.readVarInt();
        List<Soil> soils = new ArrayList<>(soilCount);
        for (int i = 0; i < soilCount; i++) {
            soils.add(new Soil(buf.readResourceLocation(), buf.readDouble(), buf.readDouble()));
        }

        int cropCount = buf.readVarInt();
        List<Crop> crops = new ArrayList<>(cropCount);
        for (int i = 0; i < cropCount; i++) {
            ItemStack seed = buf.readItem();
            ResourceLocation block = buf.readResourceLocation();
            int grow = buf.readInt();
            int dropCount = buf.readVarInt();
            List<Drop> drops = new ArrayList<>(dropCount);
            for (int j = 0; j < dropCount; j++) {
                drops.add(new Drop(buf.readItem(), buf.readVarInt(), buf.readVarInt(), buf.readFloat()));
            }
            crops.add(new Crop(seed, block, grow, drops));
        }
        return new CropPreviewPacket(base, min, soils, crops);
    }

    public static void handle(CropPreviewPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> CropPreviewClientCache.update(packet));
        ctx.setPacketHandled(true);
    }
}
