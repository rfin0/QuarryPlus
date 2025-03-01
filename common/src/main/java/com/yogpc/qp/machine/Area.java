package com.yogpc.qp.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Area(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Direction direction) {
    public static final MapCodec<Area> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.INT.fieldOf("minX").forGetter(Area::minX),
        Codec.INT.fieldOf("minY").forGetter(Area::minY),
        Codec.INT.fieldOf("minZ").forGetter(Area::minZ),
        Codec.INT.fieldOf("maxX").forGetter(Area::maxX),
        Codec.INT.fieldOf("maxY").forGetter(Area::maxY),
        Codec.INT.fieldOf("maxZ").forGetter(Area::maxZ),
        Direction.CODEC.fieldOf("direction").forGetter(Area::direction)
    ).apply(i, Area::new));

    public Area {
        if (minX > maxX)
            throw new IllegalArgumentException("MinX(%d) must be less than or equal to MaxX(%d)".formatted(minX, maxX));
        if (minY > maxY)
            throw new IllegalArgumentException("MinY(%d) must be less than or equal to MaxY(%d)".formatted(minY, maxY));
        if (minZ > maxZ)
            throw new IllegalArgumentException("MinZ(%d) must be less than or equal to MaxZ(%d)".formatted(minZ, maxZ));
    }

    public Area(Vec3i pos1, Vec3i pos2, Direction direction) {
        this(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()),
            Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()), direction);
    }

    public static Area assumeY(Area area) {
        int distanceY = area.maxY() - area.minY();
        if (distanceY >= 4) {
            return area;
        }
        return new Area(
            area.minX(), area.minY(), area.minZ(),
            area.maxX(), area.minY() + 4, area.maxZ(),
            area.direction()
        );
    }

    public boolean inAreaX(int x) {
        return this.minX < x && x < this.maxX;
    }

    public boolean inAreaZ(int z) {
        return this.minZ < z && z < this.maxZ;
    }

    public boolean inAreaXZ(Vec3i pos) {
        return inAreaX(pos.getX()) && inAreaZ(pos.getZ());
    }

    public boolean isEdge(Vec3i pos) {
        var xCondition = pos.getX() == minX || pos.getX() == maxX;
        var zCondition = pos.getZ() == minZ || pos.getZ() == maxZ;
        if (xCondition && zCondition) {
            return true;
        }
        return xCondition && inAreaZ(pos.getZ()) || zCondition && inAreaX(pos.getX());
    }

    @VisibleForTesting
    public Area shrink(int x, int y, int z) {
        int x1 = minX + x;
        int x2 = maxX - x;
        int y1 = minY + y;
        int y2 = maxY - y;
        int z1 = minZ + z;
        int z2 = maxZ - z;
        return new Area(
            Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
            Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2),
            direction);
    }

    public Set<BlockPos> getChainBlocks(BlockPos start, Predicate<BlockPos> filter, int maxY) {
        Set<BlockPos> counted = new HashSet<>((maxX - minX) * (maxZ - minZ));
        Set<Direction> directions = EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP);
        Set<BlockPos> search = Set.of(start);
        Set<BlockPos> checked = new HashSet<>((maxX - minX) * (maxZ - minZ));
        while (!search.isEmpty()) {
            Set<BlockPos> nextSearch = new HashSet<>();
            for (BlockPos pos : search) {
                checked.add(pos);
                if (filter.test(pos)) {
                    if (counted.add(pos)) {
                        directions.stream()
                            .map(pos::relative)
                            .filter(this::inAreaXZ)
                            .filter(p -> p.getY() <= maxY)
                            .filter(Predicate.not(checked::contains))
                            .forEach(nextSearch::add);
                    }
                }
            }
            search = nextSearch;
        }
        return counted;
    }

    public Set<BlockPos> getEdgeForPos(BlockPos pos) {
        var xCondition = pos.getX() == minX + 1 || pos.getX() == maxX - 1;
        var zCondition = pos.getZ() == minZ + 1 || pos.getZ() == maxZ - 1;
        if (!xCondition && !zCondition) {
            return Set.of();
        }

        return Stream.of(
            pos.offset(1, 0, 0),
            pos.offset(-1, 0, 0),
            pos.offset(0, 0, 1),
            pos.offset(0, 0, -1),
            pos.offset(1, 0, 1),
            pos.offset(-1, 0, 1),
            pos.offset(-1, 0, -1),
            pos.offset(1, 0, -1)
        ).filter(this::isEdge).collect(Collectors.toSet());
    }

    public PickIterator<BlockPos> quarryFramePosIterator() {
        return new QuarryFramePosIterator(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public PickIterator<BlockPos> quarryDigPosIterator(int y) {
        if (maxX - minX < 2 || maxZ - minZ < 2) {
            return PickIterator.empty();
        }
        return new QuarryDigPosIterator(this, y);
    }

    static class QuarryFramePosIterator extends PickIterator<BlockPos> {
        private final int minX, minY, minZ, maxX, maxY, maxZ;

        public QuarryFramePosIterator(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        @NotNull
        @Override
        protected BlockPos update() {
            // Initial
            if (lastReturned == null) {
                return new BlockPos(minX, minY, minZ);
            }
            if (lastReturned.getY() == minY || lastReturned.getY() == maxY) {
                // minX -> maxX, minZ
                if (lastReturned.getZ() == minZ && lastReturned.getX() < maxX) {
                    return lastReturned.offset(1, 0, 0);
                }
                // maxX, minZ -> maxZ
                if (lastReturned.getX() == maxX && lastReturned.getZ() < maxZ) {
                    return lastReturned.offset(0, 0, 1);
                }
                // maxX -> minX, maxZ, care minZ = maxZ
                if (lastReturned.getZ() == maxZ && lastReturned.getX() > minX && lastReturned.getZ() != minZ) {
                    return lastReturned.offset(-1, 0, 0);
                }
                // minX, maxZ -> minZ, care minX = maxX
                if (lastReturned.getX() == minX && lastReturned.getZ() > minZ + 1 && lastReturned.getX() != maxX) {
                    return lastReturned.offset(0, 0, -1);
                }
                if (lastReturned.getY() == minY) {
                    // Next Y
                    return new BlockPos(minX, minY + 1, minZ);
                } else if (lastReturned.getY() == maxY) {
                    throw new NoSuchElementException("Iterator end: " + lastReturned);
                } else {
                    // Unreachable
                    throw new IllegalStateException("Unexpected value: " + lastReturned);
                }
            }
            if (minY < lastReturned.getY() && lastReturned.getY() < maxY) {
                if (minX == maxX && minZ == maxZ) {
                    // go to next Y
                    return new BlockPos(minX, lastReturned.getY() + 1, maxZ);
                }
                if (minX == maxX) {
                    if (lastReturned.getZ() == minZ) {
                        // next Z
                        return new BlockPos(minX, lastReturned.getY(), maxZ);
                    }
                    // go to next Y
                    return new BlockPos(minX, lastReturned.getY() + 1, minZ);
                }
                if (minZ == maxZ) {
                    if (lastReturned.getX() == minX) {
                        // next X
                        return new BlockPos(maxX, lastReturned.getY(), minZ);
                    }
                    // go to next Y
                    return new BlockPos(minX, lastReturned.getY() + 1, minZ);
                }

                if (lastReturned.getX() == minX && lastReturned.getZ() == minZ) {
                    return new BlockPos(maxX, lastReturned.getY(), minZ);
                }
                if (lastReturned.getX() == maxX && lastReturned.getZ() == minZ) {
                    return new BlockPos(maxX, lastReturned.getY(), maxZ);
                }
                if (lastReturned.getX() == maxX && lastReturned.getZ() == maxZ) {
                    return new BlockPos(minX, lastReturned.getY(), maxZ);
                }
                if (lastReturned.getX() == minX && lastReturned.getZ() == maxZ) {
                    return new BlockPos(minX, lastReturned.getY() + 1, minZ);
                }
            }
            // Unreachable
            throw new IllegalStateException("Unexpected value: " + lastReturned);
        }

        @Override
        public boolean hasNext() {
            if (lastReturned == null) {
                return true;
            }
            if (minX == maxX) {
                return !(lastReturned.getZ() == maxZ && lastReturned.getY() == maxY);
            }
            if (minZ == maxZ) {
                return !(lastReturned.getX() == maxX && lastReturned.getY() == maxY);
            }
            return !(lastReturned.getX() == minX && lastReturned.getY() == maxY && lastReturned.getZ() == minZ + 1);
        }
    }

    static class QuarryDigPosIterator extends PickIterator<BlockPos> {
        private final Area area;
        private final int y;

        QuarryDigPosIterator(Area area, int y) {
            this.area = area;
            this.y = y;
        }

        @Override
        protected BlockPos update() {
            if (lastReturned == null) {
                var x = y % 2 == 0 ? area.minX() + 1 : area.maxX() - 1;
                var z = initZ(x, y, area.minZ, area.maxZ);
                return new BlockPos(x, y, z);
            }
            var nextZ = lastReturned.getX() % 2 == 0 ^ this.y % 2 == 0 ? lastReturned.getZ() + 1 : lastReturned.getZ() - 1;
            if (area.inAreaZ(nextZ)) {
                return new BlockPos(lastReturned.getX(), y, nextZ);
            }
            // Change X
            var nextX = this.y % 2 == 0 ? lastReturned.getX() + 1 : lastReturned.getX() - 1;
            if (area.inAreaX(nextX)) {
                return new BlockPos(nextX, y, initZ(nextX, y, area.minZ, area.maxZ));
            }
            // Finished this y
            throw new NoSuchElementException("Iterator end: " + lastReturned);
        }

        @Override
        public boolean hasNext() {
            if (lastReturned == null) {
                return true;
            }
            var endX = y % 2 == 0 ? area.maxX() - 1 : area.minX() + 1;
            var endZ = (lastReturned.getX() % 2 == 0 ^ y % 2 == 0) ? area.maxZ() - 1 : area.minZ() + 1;
            return !(lastReturned.getX() == endX && lastReturned.getZ() == endZ);
        }

        static int initZ(int x, int y, int minZ, int maxZ) {
            return (x % 2 == 0 ^ y % 2 == 0) ? minZ + 1 : maxZ - 1;
        }
    }
}
