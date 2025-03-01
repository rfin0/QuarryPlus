package com.yogpc.qp.gametest;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.machine.DebugStorageTest;
import com.yogpc.qp.machine.advquarry.PlaceAdvQuarryTest;
import com.yogpc.qp.machine.mover.PlaceMoverTest;
import com.yogpc.qp.machine.placer.PlacerTest;
import com.yogpc.qp.machine.placer.RemotePlacerTest;
import com.yogpc.qp.machine.quarry.PlaceQuarryTest;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class GameTestFunctions {
    public static List<TestFunction> createTestFunctionsNoPlace(String batchName, String structureName) {
        List<Class<?>> classes = List.of(
            LoadRecipeTest.class,
            EnableMapTest.class,
            ExpModuleItemTest.class,
            EnchantmentTest.class,
            MachineStorageTest.class,
            AccessFluidTest.class
        );
        var fromClass = getTestFunctionStream(batchName, structureName, classes, 3);
        return Stream.of(
            AccessItemTest.accessItems(batchName, structureName),
            AccessFluidTest.empty(batchName, structureName),
            EnableMapTest.test(batchName, structureName),
            ItemConverterGameTest.converterTests(batchName, structureName),
            fromClass
        ).flatMap(Function.identity()).toList();
    }

    public static List<TestFunction> createTestFunctionsPlace(String batchName, String structureName) {
        List<Class<?>> classes = List.of(
            PlaceQuarryTest.class,
            PlaceMoverTest.class,
            DebugStorageTest.class,
            PlaceAdvQuarryTest.class
        );
        var fromClass = getTestFunctionStream(batchName, structureName, classes, 100);
        return Stream.of(
            CheckBlockDropTest.checkDrops(batchName, structureName),
            PlacerTest.tests(batchName, structureName),
            RemotePlacerTest.tests(batchName, structureName),
            fromClass
        ).flatMap(Function.identity()).toList();
    }

    public static @NotNull Stream<TestFunction> getTestFunctionStream(String batchName, String structureName, List<Class<?>> classes, int maxTicks) {
        return classes.stream()
            .flatMap(c -> Stream.of(c.getDeclaredMethods()))
            .filter(Predicate.not(Method::isSynthetic))
            .filter(m -> (m.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
            .filter(m -> (m.getModifiers() & Modifier.PRIVATE) != Modifier.PRIVATE)
            .filter(m -> m.getParameterCount() == 1)
            .filter(m -> m.getParameterTypes()[0] == GameTestHelper.class)
            .filter(m -> m.getReturnType() == void.class)
            .peek(m -> m.setAccessible(true))
            .map(m ->
                new TestFunction(batchName, CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "%s_%s".formatted(m.getDeclaringClass().getSimpleName(), m.getName())), structureName, maxTicks, 0, true, g -> {
                    try {
                        m.invoke(null, g);
                    } catch (ReflectiveOperationException | AssertionError e) {
                        throw new RuntimeException(e);
                    }
                })
            );
    }

    public static Consumer<GameTestHelper> wrapper(Consumer<GameTestHelper> test) {
        return g -> {
            try {
                test.accept(g);
            } catch (AssertionError e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static Holder<Enchantment> getEnchantment(GameTestHelper helper, ResourceKey<Enchantment> key) {
        var reg = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return reg.getOrThrow(key);
    }
}
