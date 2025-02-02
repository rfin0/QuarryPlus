package com.yogpc.qp.machine;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Just a util method to support FakePlayer API
 */
public final class QuarryFakePlayerCommon {
    public static final GameProfile PROFILE = new GameProfile(UUID.fromString("ce6c3b8d-11ba-4b32-90d5-e5d30167fca7"), "[QuarryPlus]");

    public static void setDirection(ServerPlayer player, Direction direction) {
        player.setXRot(direction.getUnitVec3i().getY() * 90);
        player.setYRot(direction.toYRot());
    }

    public static ServerPlayer getOwnImplementation(ServerLevel serverLevel, Function<ServerLevel, MinecraftServer> serverGetter) {
        return new InternalFakeLikePlayer(serverLevel, PROFILE, ClientInformation.createDefault(), serverGetter);
    }

    private static final class InternalFakeLikePlayer extends ServerPlayer {
        private final Function<ServerLevel, MinecraftServer> serverGetter;

        private InternalFakeLikePlayer(ServerLevel level, GameProfile name, ClientInformation info, Function<ServerLevel, MinecraftServer> serverGetter) {
            super(level.getServer(), level, name, info);
            this.serverGetter = serverGetter;
            this.connection = new NetHandler(level.getServer(), this);
        }

        @Override
        public void displayClientMessage(Component chatComponent, boolean actionBar) {
        }

        @Override
        public void awardStat(Stat<?> stat, int amount) {
        }

        @Override
        public boolean isInvulnerableTo(ServerLevel level, DamageSource damageSource) {
            return true;
        }

        @Override
        public boolean isInvulnerable() {
            return true;
        }

        @Override
        public boolean canHarmPlayer(Player player) {
            return false;
        }

        @Override
        public void die(DamageSource source) {
        }

        @Override
        public void tick() {
        }

        @Override
        @Nullable
        public MinecraftServer getServer() {
            return serverGetter.apply(serverLevel());
        }

        static class NetHandler extends ServerGamePacketListenerImpl {
            private static final Connection DUMMY_CONNECTION = new Connection(PacketFlow.SERVERBOUND) {
                @Override
                public void setListenerForServerboundHandshake(PacketListener packetListener) {
                }
            };

            NetHandler(MinecraftServer server, ServerPlayer player) {
                super(server, DUMMY_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
            }

            // @formatter:off
            @Override public void tick() { }
            @Override public void resetPosition() { }
            @Override public void disconnect(Component message) { }
            @Override public void handlePlayerInput(ServerboundPlayerInputPacket packet) { }
            @Override public void handleMoveVehicle(ServerboundMoveVehiclePacket packet) { }
            @Override public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket packet) { }
            @Override public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket packet) { }
            @Override public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket packet) { }
            @Override public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket packet) { }
            @Override public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket packet) { }
            @Override public void handleSetCommandBlock(ServerboundSetCommandBlockPacket packet) { }
            @Override public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket packet) { }
            @Override public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket packet) { }
            @Override public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket packet) { }
            @Override public void handleRenameItem(ServerboundRenameItemPacket packet) { }
            @Override public void handleSetBeaconPacket(ServerboundSetBeaconPacket packet) { }
            @Override public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packet) { }
            @Override public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket packet) { }
            @Override public void handleJigsawGenerate(ServerboundJigsawGeneratePacket packet) { }
            @Override public void handleSelectTrade(ServerboundSelectTradePacket packet) { }
            @Override public void handleEditBook(ServerboundEditBookPacket packet) { }
            @Override public void handleEntityTagQuery(ServerboundEntityTagQueryPacket packet) { }
            @Override public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket packet) { }
            @Override public void handleMovePlayer(ServerboundMovePlayerPacket packet) { }
            @Override public void teleport(double x, double y, double z, float yaw, float pitch) { }
            @Override public void handlePlayerAction(ServerboundPlayerActionPacket packet) { }
            @Override public void handleUseItemOn(ServerboundUseItemOnPacket packet) { }
            @Override public void handleUseItem(ServerboundUseItemPacket packet) { }
            @Override public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packet) { }
            @Override public void handlePaddleBoat(ServerboundPaddleBoatPacket packet) { }
            @Override public void onDisconnect(DisconnectionDetails message) { }
            @Override public void send(Packet<?> packet) { }
            @Override public void send(Packet<?> packet, @Nullable PacketSendListener sendListener) { }
            @Override public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packet) { }
            @Override public void handleChat(ServerboundChatPacket packet) { }
            @Override public void handleAnimate(ServerboundSwingPacket packet) { }
            @Override public void handlePlayerCommand(ServerboundPlayerCommandPacket packet) { }
            @Override public void handleInteract(ServerboundInteractPacket packet) { }
            @Override public void handleClientCommand(ServerboundClientCommandPacket packet) { }
            @Override public void handleContainerClose(ServerboundContainerClosePacket packet) { }
            @Override public void handleContainerClick(ServerboundContainerClickPacket packet) { }
            @Override public void handlePlaceRecipe(ServerboundPlaceRecipePacket packet) { }
            @Override public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packet) { }
            @Override public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packet) { }
            @Override public void handleSignUpdate(ServerboundSignUpdatePacket packet) { }
            @Override public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packet) { }
            @Override public void handleChangeDifficulty(ServerboundChangeDifficultyPacket packet) { }
            @Override public void handleLockDifficulty(ServerboundLockDifficultyPacket packet) { }
            @Override public void teleport(PositionMoveRotation rotation, Set<Relative> relatives) { }
            @Override public void ackBlockChangesUpTo(int sequence) { }
            @Override public void handleChatCommand(ServerboundChatCommandPacket packet) { }
            @Override public void handleChatAck(ServerboundChatAckPacket packet) { }
            @Override public void addPendingMessage(PlayerChatMessage message) { }
            @Override public void sendPlayerChatMessage(PlayerChatMessage message, ChatType.Bound boundChatType) { }
            @Override public void sendDisguisedChatMessage(Component content, ChatType.Bound boundChatType) { }
            @Override public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket packet) { }
            // @formatter:on
        }
    }
}
