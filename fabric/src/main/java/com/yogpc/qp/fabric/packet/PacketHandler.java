package com.yogpc.qp.fabric.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.fabric.machine.quarry.QuarryConfigSyncMessage;
import com.yogpc.qp.machine.advquarry.AdvActionActionMessage;
import com.yogpc.qp.machine.advquarry.AdvActionSyncMessage;
import com.yogpc.qp.machine.advquarry.AdvQuarryInitialAskMessage;
import com.yogpc.qp.machine.marker.ChunkMarkerMessage;
import com.yogpc.qp.machine.marker.FlexibleMarkerMessage;
import com.yogpc.qp.machine.mover.MoverMessage;
import com.yogpc.qp.machine.placer.RemotePlacerMessage;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import com.yogpc.qp.packet.YSetterMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class PacketHandler implements PlatformAccess.Packet {
    public static class Server {
        public static void registerMessage() {
            PayloadTypeRegistry.playS2C().register(ClientSyncMessage.TYPE, ClientSyncMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(YSetterMessage.TYPE, YSetterMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(MoverMessage.TYPE, MoverMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(QuarryConfigSyncMessage.TYPE, QuarryConfigSyncMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(FlexibleMarkerMessage.TYPE, FlexibleMarkerMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(ChunkMarkerMessage.TYPE, ChunkMarkerMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(AdvActionActionMessage.TYPE, AdvActionActionMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(AdvActionSyncMessage.TYPE, AdvActionSyncMessage.STREAM_CODEC);
            PayloadTypeRegistry.playS2C().register(AdvQuarryInitialAskMessage.TYPE, AdvQuarryInitialAskMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(RemotePlacerMessage.TYPE, RemotePlacerMessage.STREAM_CODEC);
        }

        public static void initServer() {
            ServerPlayNetworking.registerGlobalReceiver(YSetterMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(MoverMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(QuarryConfigSyncMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(FlexibleMarkerMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(ChunkMarkerMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(AdvActionActionMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(AdvActionSyncMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(RemotePlacerMessage.TYPE, Server::onReceive);
        }

        private static void onReceive(OnReceiveWithLevel message, ServerPlayNetworking.Context context) {
            var level = context.player().level();
            context.server().execute(() -> message.onReceive(level, context.player()));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            ClientPlayNetworking.registerGlobalReceiver(ClientSyncMessage.TYPE, Client::onReceive);
            ClientPlayNetworking.registerGlobalReceiver(AdvQuarryInitialAskMessage.TYPE, Client::onReceive);
        }

        private static void onReceive(OnReceiveWithLevel message, ClientPlayNetworking.Context context) {
            var level = context.client().level;
            context.client().execute(() -> message.onReceive(level, context.player()));
        }
    }

    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
            ServerPlayNetworking.send(player, message);
        }
    }

    @Override
    public void sendToClientPlayer(@NotNull CustomPacketPayload message, @NotNull ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }

    @Override
    public void sendToServer(@NotNull CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }
}
