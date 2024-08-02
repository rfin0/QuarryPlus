package com.yogpc.qp;

import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.misc.FrameBlock;
import com.yogpc.qp.machine.misc.GeneratorBlock;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface PlatformAccess {
    static PlatformAccess getAccess() {
        return PlatformAccessHolder.instance;
    }

    static QuarryConfig getConfig() {
        var access = getAccess();
        return QuarryConfig.load(access.configPath(), access::isInDevelopmentEnvironment);
    }

    String platformName();

    RegisterObjects registerObjects();

    interface RegisterObjects {
        Supplier<? extends QuarryBlock> quarryBlock();

        Supplier<? extends FrameBlock> frameBlock();

        Supplier<? extends GeneratorBlock> generatorBlock();

        Supplier<? extends NormalMarkerBlock> markerBlock();

        Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block);

        Stream<Supplier<? extends InCreativeTabs>> allItems();
    }

    Packet packetHandler();

    interface Packet {
        void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level);
    }

    Path configPath();

    boolean isInDevelopmentEnvironment();

    interface Transfer {
        /**
         * @return items that is not moved. In other words, the rest of item.
         */
        ItemStack transferItem(Level level, BlockPos pos, ItemStack stack, Direction side, boolean simulate);

        FluidStackLike transferFluid(Level level, BlockPos pos, FluidStackLike stack, Direction side, boolean simulate);
    }

    Transfer transfer();

    FluidStackLike getFluidInItem(ItemStack stack);
}

class PlatformAccessHolder {
    static final PlatformAccess instance;

    static {
        QuarryPlus.LOGGER.info("[PlatformAccess] loading");
        instance = ServiceLoader.load(PlatformAccess.class, PlatformAccess.class.getClassLoader()).findFirst().orElseThrow(() -> new IllegalStateException("PlatformAccess not found. It's a bug."));
        QuarryPlus.LOGGER.info("[PlatformAccess] loaded for {}, {}", instance.platformName(), instance.getClass().getSimpleName());
    }
}
