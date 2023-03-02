package com.yogpc.qp.machines.advquarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.Area;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

/**
 * To Server only
 */
public final class AdvActionMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final Area area;
    private final Actions action;
    private final boolean placeAreaFrame;
    private final boolean chunkByChunk;
    private final boolean startImmediately;

    AdvActionMessage(TileAdvQuarry quarry, Actions action, Area area, boolean placeAreaFrame, boolean chunkByChunk, boolean startImmediately) {
        this.pos = quarry.getBlockPos();
        this.dim = PacketHandler.getDimension(quarry);
        this.area = area;
        this.action = action;
        this.placeAreaFrame = placeAreaFrame;
        this.chunkByChunk = chunkByChunk;
        this.startImmediately = startImmediately;
        AdvQuarry.LOGGER.debug(AdvQuarry.MESSAGE, "Message is created. {} {} {} {}", this.pos, this.dim.location(), this.area, this.action);
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action, Area area) {
        this(quarry, action, area, quarry.placeAreaFrame, quarry.chunkByChunk, quarry.startImmediately);
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action) {
        this(quarry, action, quarry.getArea(), quarry.placeAreaFrame, quarry.chunkByChunk, quarry.startImmediately);
    }

    AdvActionMessage(TileAdvQuarry quarry, Actions action, boolean placeAreaFrame, boolean chunkByChunk, boolean startImmediately) {
        this(quarry, action, quarry.getArea(), placeAreaFrame, chunkByChunk, startImmediately);
    }

    public AdvActionMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        this.area = Area.fromNBT(buf.readNbt()).orElse(null);
        this.action = buf.readEnum(Actions.class);
        this.placeAreaFrame = buf.readBoolean();
        this.chunkByChunk = buf.readBoolean();
        this.startImmediately = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos).writeResourceLocation(this.dim.location());
        buf.writeNbt(this.area.toNBT());
        buf.writeEnum(this.action);
        buf.writeBoolean(this.placeAreaFrame).writeBoolean(this.chunkByChunk).writeBoolean(startImmediately);
    }

    enum Actions {
        QUICK_START, MODULE_INV, CHANGE_RANGE, SYNC
    }

    public static void onReceive(AdvActionMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world.map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(TileAdvQuarry.class))
                .ifPresent(quarry -> {
                    AdvQuarry.LOGGER.debug(AdvQuarry.MESSAGE, "onReceive. {}, {}", message.pos, message.action);
                    switch (message.action) {
                        case CHANGE_RANGE -> quarry.setArea(message.area);
                        case MODULE_INV -> PacketHandler.getPlayer(supplier.get())
                            .flatMap(MapMulti.optCast(ServerPlayer.class))
                            .ifPresent(quarry::openModuleGui);
                        case QUICK_START -> {
                            quarry.startImmediately = true;
                            if (quarry.canStartWork()) {
                                AdvQuarryAction.startQuarry(quarry);
                            }
                        }
                        case SYNC -> {
                            quarry.startImmediately = message.startImmediately;
                            quarry.placeAreaFrame = message.placeAreaFrame;
                            quarry.chunkByChunk = message.chunkByChunk;
                        }
                    }
                }));
    }
}
