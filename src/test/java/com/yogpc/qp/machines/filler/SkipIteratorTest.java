package com.yogpc.qp.machines.filler;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkipIteratorTest extends QuarryPlusTest {
    static final Area AREA = new Area(-3, 5, 10, 2, 8, 13, Direction.WEST);
    public static final Predicate<BlockPos> TRUE_PREDICATE = p -> true;

    @Test
    void peek() {
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var first = i.peek(TRUE_PREDICATE);
        assertEquals(first, i.peek(TRUE_PREDICATE));
        assertEquals(first, i.peek(TRUE_PREDICATE));
        var first2 = i.next(TRUE_PREDICATE);
        assertEquals(first2, first);
        var second = i.peek(TRUE_PREDICATE);
        assertNotEquals(first, second);
        assertEquals(second, i.peek(TRUE_PREDICATE));
    }

    @Test
    void noCondition() {
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var list = assertTimeoutPreemptively(Duration.ofMillis(3000L), () -> Stream.generate(() -> i.next(TRUE_PREDICATE))
            .takeWhile(Objects::nonNull).toList());
        assertEquals(96, list.size());
        assertTrue(i.skipped.isEmpty());
    }

    @Test
    void falseCondition() {
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var list = assertTimeoutPreemptively(Duration.ofMillis(3000L), () -> Stream.generate(() -> i.next(p -> false))
            .takeWhile(Objects::nonNull).toList());
        assertTrue(list.isEmpty());
    }

    @Test
    void falseCondition2() {
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        assertTimeoutPreemptively(Duration.ofMillis(3000L), () -> Stream.generate(() -> i.next(p -> false))
            .takeWhile(Objects::nonNull).toList());
        assertFalse(i.skipped.contains(null));
        assertEquals(96, i.skipped.size());
    }

    @Test
    void commitSkips() {
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var set = new HashSet<BlockPos>();
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> {
            BlockPos pos;
            // Use predicate to prevent infinite loop.
            while ((pos = i.peek(Predicate.not(set::contains))) != null) {
                set.add(pos);
                i.commit(pos, true);
            }
        });
        assertEquals(96, set.size());
        assertEquals(set, Set.copyOf(i.skipped));
    }

    @Test
    void condition1() {
        Predicate<BlockPos> condition = pos -> pos.getY() % 2 == 0;
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var list = assertTimeoutPreemptively(Duration.ofMillis(3000L),
            () -> Stream.generate(() -> i.next(condition)).takeWhile(Objects::nonNull).toList());
        assertAll(
            () -> assertEquals(48, list.size()),
            () -> assertEquals(48, i.skipped.size()),
            () -> assertTrue(list.stream().allMatch(condition)),
            () -> assertTrue(i.skipped.stream().allMatch(p -> p.getY() % 2 == 1))
        );
    }

    @Test
    void setCurrent1() {
        var i = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        i.posIterator.setCurrent(new BlockPos(-3, 6, 10));
        assertEquals(new BlockPos(-3, 6, 10), i.next(TRUE_PREDICATE));
        assertEquals(new BlockPos(-2, 6, 10), i.peek(TRUE_PREDICATE));
    }

    @Test
    void setCurrent2() {
        var i1 = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var list = assertTimeoutPreemptively(Duration.ofMillis(3000L), () -> Stream.generate(() -> i1.next(TRUE_PREDICATE))
            .takeWhile(Objects::nonNull).toList());
        var i2 = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        for (int i = 0; i < 4; i++) {
            i2.next(TRUE_PREDICATE);
        }
        assertEquals(list.get(4), i2.peek(TRUE_PREDICATE));
        var tag = i2.toNbt();
        var i3 = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        i3.fromNbt(tag);
        assertEquals(list.get(4), i3.next(TRUE_PREDICATE));
        assertEquals(list.get(5), i3.next(TRUE_PREDICATE));
        assertEquals(list.get(6), i3.next(TRUE_PREDICATE));
    }

    @Test
    void reuseSkipped() {
        var i1 = new SkipIterator(AREA, FillerTargetPosIterator.Box::new);
        var y6 = i1.peek(p -> p.getY() > 5);
        assertEquals(new BlockPos(-3, 6, 10), y6);
        var y5 = i1.next(p -> p.getY() < 6);
        assertEquals(new BlockPos(-3, 5, 10), y5);
    }
}
