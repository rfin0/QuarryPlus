package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniQuarryTileTest extends QuarryPlusTest {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6})
    void intervalTest(int level) {
        int interval1 = MiniQuarryTile.interval(level - 1);
        int interval2 = MiniQuarryTile.interval(level);
        assertTrue(interval1 > interval2);
    }

    @Nested
    class PredicateSetting {
        @Test
        void disallowAirInAllowList() {
            assertFalse(MiniQuarryTile.canAddInList(true, BlockStatePredicate.air()));
        }

        @Test
        void disallowAllInAllowList() {
            assertFalse(MiniQuarryTile.canAddInList(true, BlockStatePredicate.all()));
        }

        @Test
        void disallowAirInDenyList() {
            assertFalse(MiniQuarryTile.canAddInList(false, BlockStatePredicate.air()));
        }

        @Test
        void disallowFluidInDenyList() {
            assertFalse(MiniQuarryTile.canAddInList(false, BlockStatePredicate.fluid()));
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void allowNameInList(boolean isAllowList) {
            assertTrue(MiniQuarryTile.canAddInList(isAllowList, BlockStatePredicate.name(new ResourceLocation("minecraft", "stone"))));
        }
    }
}