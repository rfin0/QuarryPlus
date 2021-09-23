package com.yogpc.qp.machines.advquarry;

import java.util.Iterator;

import com.yogpc.qp.machines.Area;

abstract class TargetIterator implements Iterator<TargetIterator.XZPair> {
    protected XZPair current;
    protected final Area area;

    TargetIterator(Area area) {
        this.area = area;
    }

    static TargetIterator of(Area area) {
        return switch (area.direction()) {
            case NORTH, UP, DOWN -> new North(area);
            case SOUTH -> new South(area);
            case WEST -> new West(area);
            case EAST -> new East(area);
        };
    }

    @Override
    public final XZPair next() {
        var c = current;
        current = update();
        return c;
    }

    abstract XZPair update();

    @Override
    public final boolean hasNext() {
        return area.minX() < current.x() && current.x() < area.maxX() &&
            area.minZ() < current.z() && current.z() < area.maxZ();
    }

    public final XZPair peek() {
        return current;
    }

    void setCurrent(XZPair current) {
        this.current = current;
    }

    final record XZPair(int x, int z) {
    }

    private static final class North extends TargetIterator {

        North(Area area) {
            super(area);
            current = new XZPair(area.maxX() - 1, area.minZ() + 1);
        }

        @Override
        XZPair update() {
            if (current.z() + 1 >= area.maxZ()) {
                // Next x
                return new XZPair(current.x() - 1, area.minZ() + 1);
            } else {
                return new XZPair(current.x(), current.z() + 1);
            }
        }
    }

    private static final class South extends TargetIterator {

        South(Area area) {
            super(area);
            current = new XZPair(area.minX() + 1, area.maxZ() - 1);
        }

        @Override
        XZPair update() {
            if (current.z() - 1 <= area.minZ()) {
                // Next x
                return new XZPair(current.x() + 1, area.maxZ() - 1);
            } else {
                return new XZPair(current.x(), current.z() - 1);
            }
        }
    }

    private static final class West extends TargetIterator {

        West(Area area) {
            super(area);
            current = new XZPair(area.minX() + 1, area.minZ() + 1);
        }

        @Override
        XZPair update() {
            if (current.x() + 1 >= area.maxX()) {
                // Next z
                return new XZPair(area.minX() + 1, current.z() + 1);
            } else {
                return new XZPair(current.x() + 1, current.z());
            }
        }
    }

    private static final class East extends TargetIterator {

        East(Area area) {
            super(area);
            current = new XZPair(area.maxX() - 1, area.maxZ() - 1);
        }

        @Override
        XZPair update() {
            if (current.x() - 1 <= area.minX()) {
                // Next z
                return new XZPair(area.maxX() - 1, current.z() - 1);
            } else {
                return new XZPair(current.x() - 1, current.z());
            }
        }
    }
}
