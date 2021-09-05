package com.yogpc.qp.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class QuarryChunkLoadUtil {
    public static boolean isChunkLoaded(Level world, BlockPos pos) {
        return isChunkLoaded(world, pos.getX(), pos.getZ());
    }

    public static boolean isChunkLoaded(Level world, int x, int z) {
        if (world instanceof ServerLevel serverWorld) {
            var key = new ChunkPos(SectionPos.sectionToBlockCoord(x), SectionPos.sectionToBlockCoord(z)).toLong();
            return serverWorld.getForcedChunks().contains(key);
        } else {
            return false;
        }
    }

    /**
     * @return whether the chunk is already loaded.
     */
    public static boolean makeChunkLoaded(Level world, BlockPos pos, boolean machineEnabled) {
        if (!machineEnabled) return false;
        if (world instanceof ServerLevel serverWorld) {
            if (isChunkLoaded(world, pos)) {
                return true;
            } else {
                serverWorld.setChunkForced(SectionPos.sectionToBlockCoord(pos.getX()), SectionPos.sectionToBlockCoord(pos.getZ()), true);
                return false;
            }
        } else {
            return false;
        }
    }

    public static void makeChunkUnloaded(Level world, BlockPos pos, boolean preLoaded) {
        if (!preLoaded && world instanceof ServerLevel serverWorld) {
            serverWorld.setChunkForced(SectionPos.sectionToBlockCoord(pos.getX()), SectionPos.sectionToBlockCoord(pos.getZ()), false);
        }
    }
}
