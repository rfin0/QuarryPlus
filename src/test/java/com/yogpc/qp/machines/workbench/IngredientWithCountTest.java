package com.yogpc.qp.machines.workbench;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.yogpc.qp.QuarryPlusTest;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientWithCountTest extends QuarryPlusTest {
    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Test
    void instance() {
        var i = new IngredientWithCount(new ItemStack(Items.APPLE));
        assertNotNull(i);
        assertTrue(i.test(new ItemStack(Items.APPLE)));
        assertFalse(i.test(ItemStack.EMPTY));
    }

    @Test
    void testCount1() {
        var i = new IngredientWithCount(new ItemStack(Items.APPLE, 5));
        assertAll(
            () -> assertFalse(i.test(new ItemStack(Items.APPLE, 1))),
            () -> assertFalse(i.test(new ItemStack(Items.APPLE, 2))),
            () -> assertFalse(i.test(new ItemStack(Items.APPLE, 3))),
            () -> assertFalse(i.test(new ItemStack(Items.APPLE, 4))),
            () -> assertTrue(i.test(new ItemStack(Items.APPLE, 5))),
            () -> assertTrue(i.test(new ItemStack(Items.APPLE, 6))),
            () -> {
            }
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 64})
    void toJson1(int count) {
        var i = new IngredientWithCount(new ItemStack(Items.APPLE, count));
        // This is json, but includes formatter literal.
        var expected = GSON.fromJson("""
            {
              "item": "minecraft:apple",
              "count": %d
            }
            """.formatted(count), JsonObject.class);
        assertAll(
            () -> assertEquals(expected, i.toJson()),
            () -> assertEquals(i.toJson(), IngredientWithCount.getSeq(expected).get(0).toJson()),
            () -> {
            }
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 64})
    void toNetwork(int count) {
        var i = new IngredientWithCount(new ItemStack(Items.APPLE, count));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        i.toPacket(buffer);
        var fromNetwork = IngredientWithCount.fromPacket(buffer);
        assertEquals(i.toJson(), fromNetwork.toJson());
    }

    @Test
    void fromJson1() {
        // language=json
        var json = GSON.fromJson("""
            [
              {"item": "minecraft:apple", "count": 6},
              {"item": "minecraft:diamond", "count": 20}
            ]
            """, JsonArray.class);
        var i = IngredientWithCount.getSeq(json);
        assertEquals(2, i.size());
        var a1 = new IngredientWithCount(new ItemStack(Items.APPLE, 6));
        var a2 = new IngredientWithCount(new ItemStack(Items.DIAMOND, 20));
        assertAll(
            () -> assertEquals(a1.toJson(), i.get(0).toJson()),
            () -> assertEquals(a2.toJson(), i.get(1).toJson())
        );
    }
}
