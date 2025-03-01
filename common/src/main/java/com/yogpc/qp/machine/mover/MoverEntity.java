package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpEntity;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class MoverEntity extends QpEntity implements ClientSync {
    final SimpleContainer inventory = new Inventory(2);

    public MoverEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
        inventory.addListener(container -> this.setChanged());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        inventory.fromTag(tag.getList("inventory", Tag.TAG_COMPOUND), registries);
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.createTag(registries));
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        var enchantmentRegistry = registries.lookupOrThrow(Registries.ENCHANTMENT);
        movableEnchantments = tag.getList("enchantments", Tag.TAG_STRING)
            .stream()
            .map(Tag::getAsString)
            .map(s -> ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(s)))
            .<Holder<Enchantment>>map(enchantmentRegistry::getOrThrow)
            .toList();
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        var list = movableEnchantments.stream()
            .map(Holder::unwrapKey)
            .flatMap(Optional::stream)
            .map(ResourceKey::location)
            .map(ResourceLocation::toString)
            .map(StringTag::valueOf)
            .collect(Collectors.toCollection(ListTag::new));
        tag.put("enchantments", list);
        return tag;
    }

    private static class Inventory extends SimpleContainer {
        public Inventory(int size) {
            super(size);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> {
                    if (stack.is(Items.ENCHANTED_BOOK)) {
                        yield true;
                    }
                    if (!stack.isEnchanted()) {
                        yield false;
                    }
                    yield switch (stack.getItem()) {
                        case DiggerItem ignore -> stack.getMaxDamage() >= ToolMaterial.DIAMOND.durability();
                        case SwordItem ignore -> stack.getMaxDamage() >= ToolMaterial.DIAMOND.durability();
                        case BowItem ignore -> true;
                        default -> false;
                    };
                }
                case 1 -> stack.is(PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem)
                    || stack.is(PlatformAccess.getAccess().registerObjects().advQuarryBlock().get().blockItem);
                default -> false;
            };
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateMovableEnchantments();
    }

    void updateMovableEnchantments() {
        if (level != null && !level.isClientSide() && enabled) {
            var pre = movableEnchantments;
            // Update in server only.
            this.movableEnchantments = getMovable(inventory.getItem(0), inventory.getItem(1), e -> true);
            if (!pre.equals(movableEnchantments)) {
                syncToClient();
            }
        }
    }

    List<Holder<Enchantment>> movableEnchantments = List.of();

    @VisibleForTesting
    static List<Holder<Enchantment>> getMovable(ItemStack from, ItemStack to, Predicate<Holder<Enchantment>> predicate) {
        if (from.isEmpty() || to.isEmpty()) {
            return List.of();
        }
        var given = EnchantmentHelper.getEnchantmentsForCrafting(to);
        return EnchantmentHelper.getEnchantmentsForCrafting(from).keySet().stream()
            .filter(e -> canMoveEnchantment(predicate, given, e))
            .sorted(Comparator.comparing(Holder::getRegisteredName))
            .toList();
    }

    @VisibleForTesting
    static boolean canMoveEnchantment(@Nullable Predicate<Holder<Enchantment>> predicate, ItemEnchantments given, Holder<Enchantment> toMove) {
        return
            (predicate == null || predicate.test(toMove)) &&
                given.getLevel(toMove) < toMove.value().getMaxLevel() &&
                given.keySet().stream().filter(Predicate.isEqual(toMove).negate()).allMatch(e -> Enchantment.areCompatible(e, toMove));
    }

    void moveEnchant(Holder<Enchantment> enchantment) {
        var moved = moveEnchantment(enchantment, inventory.getItem(0), inventory.getItem(1), this::updateMovableEnchantments);
        inventory.setItem(0, moved.getLeft());
        inventory.setItem(1, moved.getRight());
    }

    static Pair<ItemStack, ItemStack> moveEnchantment(@Nullable Holder<Enchantment> enchantment, ItemStack from, ItemStack to, Runnable after) {
        return moveEnchantment(enchantment, from, to, null, after);
    }

    @VisibleForTesting
    static Pair<ItemStack, ItemStack> moveEnchantment(@Nullable Holder<Enchantment> enchantment, ItemStack from, ItemStack to, @Nullable Predicate<Holder<Enchantment>> predicate, Runnable after) {
        if (enchantment == null || from.isEmpty() || to.isEmpty()) return Pair.of(from, to);
        if (canMoveEnchantment(predicate, EnchantmentHelper.getEnchantmentsForCrafting(to), enchantment)) {
            var right = upLevel(enchantment, to);
            var left = downLevel(enchantment, from);
            after.run();
            return Pair.of(left, right);
        }
        return Pair.of(from, to);
    }

    @VisibleForTesting
    static ItemStack downLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
            mutable.set(enchantment, mutable.getLevel(enchantment) - 1)
        );
        if (stack.is(Items.ENCHANTED_BOOK) && EnchantmentHelper.getEnchantmentsForCrafting(stack).isEmpty()) {
            // Remove empty enchanted book
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @VisibleForTesting
    static ItemStack upLevel(Holder<Enchantment> enchantment, ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
            mutable.set(enchantment, mutable.getLevel(enchantment) + 1)
        );
        return stack;
    }
}
