package com.deveisia.enceladus.cropsfh.network;

import com.deveisia.enceladus.cropsfh.CropsFHMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class CropsFHNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CropsFHMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private CropsFHNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, CropPreviewPacket.class,
                CropPreviewPacket::encode,
                CropPreviewPacket::decode,
                CropPreviewPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAll(Object packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }
}
