package com.yogpc.qp.forge;

import com.yogpc.qp.*;
import com.yogpc.qp.config.ConfigHolder;
import com.yogpc.qp.config.EnableMap;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.forge.machine.advquarry.AdvQuarryEntityForge;
import com.yogpc.qp.forge.machine.marker.ChunkMarkerEntityForge;
import com.yogpc.qp.forge.machine.marker.FlexibleMarkerEntityForge;
import com.yogpc.qp.forge.machine.marker.NormalMarkerEntityForge;
import com.yogpc.qp.forge.machine.misc.CheckerItemForge;
import com.yogpc.qp.forge.machine.misc.YSetterItemForge;
import com.yogpc.qp.forge.machine.quarry.QuarryBlockForge;
import com.yogpc.qp.forge.machine.quarry.QuarryEntityForge;
import com.yogpc.qp.forge.packet.PacketHandler;
import com.yogpc.qp.machine.GeneralScreenHandler;
import com.yogpc.qp.machine.MachineLootFunction;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.advquarry.AdvQuarryBlock;
import com.yogpc.qp.machine.advquarry.AdvQuarryContainer;
import com.yogpc.qp.machine.exp.ExpModuleItem;
import com.yogpc.qp.machine.marker.ChunkMarkerBlock;
import com.yogpc.qp.machine.marker.FlexibleMarkerBlock;
import com.yogpc.qp.machine.marker.MarkerContainer;
import com.yogpc.qp.machine.marker.NormalMarkerBlock;
import com.yogpc.qp.machine.misc.*;
import com.yogpc.qp.machine.module.*;
import com.yogpc.qp.machine.mover.MoverBlock;
import com.yogpc.qp.machine.mover.MoverContainer;
import com.yogpc.qp.machine.mover.MoverEntity;
import com.yogpc.qp.machine.placer.*;
import com.yogpc.qp.machine.quarry.QuarryBlock;
import com.yogpc.qp.machine.storage.DebugStorageBlock;
import com.yogpc.qp.machine.storage.DebugStorageContainer;
import com.yogpc.qp.machine.storage.DebugStorageEntity;
import com.yogpc.qp.recipe.InstallBedrockModuleRecipe;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.crafting.ingredients.IIngredientSerializer;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.util.Lazy;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class PlatformAccessForge implements PlatformAccess {
    private final Lazy<RegisterObjects> itemsLazy = Lazy.lazy(RegisterObjectsForge::new);
    private final Lazy<PacketHandler> packetHandlerLazy = Lazy.lazy(PacketHandler::new);
    private final Lazy<TransferForge> transferLazy = Lazy.lazy(TransferForge::new);
    private final ConfigHolder configLazy = new ConfigHolder(() ->
        QuarryConfig.load(configPath(), this::isInDevelopmentEnvironment)
    );
    private final Lazy<Mining> miningLazy = Lazy.lazy(MiningForge::new);

    public static class RegisterObjectsForge implements PlatformAccess.RegisterObjects {
        private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCKS, QuarryPlus.modID);
        private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, QuarryPlus.modID);
        private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, QuarryPlus.modID);
        private static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTER = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, QuarryPlus.modID);
        private static final DeferredRegister<IIngredientSerializer<?>> INGREDIENT_REGISTER = DeferredRegister.create(ForgeRegistries.INGREDIENT_SERIALIZERS, QuarryPlus.modID);
        private static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, QuarryPlus.modID);
        private static final DeferredRegister<LootItemFunctionType<?>> LOOT_TYPE_REGISTER = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPE_REGISTER = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, QuarryPlus.modID);
        private static final DeferredRegister<MenuType<?>> MENU_TYPE_REGISTER = DeferredRegister.create(Registries.MENU, QuarryPlus.modID);
        static final List<DeferredRegister<?>> REGISTER_LIST = List.of(
            BLOCK_REGISTER, ITEM_REGISTER, BLOCK_ENTITY_REGISTER, RECIPE_REGISTER, INGREDIENT_REGISTER, CREATIVE_TAB_REGISTER, LOOT_TYPE_REGISTER, DATA_COMPONENT_TYPE_REGISTER, MENU_TYPE_REGISTER
        );
        private static final List<Supplier<? extends InCreativeTabs>> TAB_ITEMS = new ArrayList<>();
        private static final Map<String, EnableMap.EnableOrNot> ENABLE_MAP = new HashMap<>();

        // Machine
        public static final RegistryObject<QuarryBlockForge> BLOCK_QUARRY = registerBlock(QuarryBlockForge.NAME, QuarryBlockForge::new);
        public static final RegistryObject<AdvQuarryBlock> BLOCK_ADV_QUARRY = registerBlock(AdvQuarryBlock.NAME, AdvQuarryBlock::new);
        public static final RegistryObject<GeneratorBlock> BLOCK_GENERATOR = registerBlock(GeneratorBlock.NAME, GeneratorBlock::new);
        public static final RegistryObject<MoverBlock> BLOCK_MOVER = registerBlock(MoverBlock.NAME, MoverBlock::new);
        public static final RegistryObject<PlacerBlock> BLOCK_PLACER = registerBlock(PlacerBlock.NAME, PlacerBlock::new);
        public static final RegistryObject<RemotePlacerBlock> BLOCK_REMOTE_PLACER = registerBlock(RemotePlacerBlock.NAME, RemotePlacerBlock::new);
        // Marker
        public static final RegistryObject<NormalMarkerBlock> BLOCK_MARKER = registerBlock(NormalMarkerBlock.NAME, NormalMarkerBlock::new);
        public static final RegistryObject<FlexibleMarkerBlock> BLOCK_FLEXIBLE_MARKER = registerBlock(FlexibleMarkerBlock.NAME, FlexibleMarkerBlock::new);
        public static final RegistryObject<ChunkMarkerBlock> BLOCK_CHUNK_MARKER = registerBlock(ChunkMarkerBlock.NAME, ChunkMarkerBlock::new);
        // Module
        public static final RegistryObject<PumpModuleItem> ITEM_PUMP_MODULE = registerItem(PumpModuleItem.NAME, PumpModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BedrockModuleItem> ITEM_BEDROCK_MODULE = registerItem(BedrockModuleItem.NAME, BedrockModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<ExpModuleItem> ITEM_EXP_MODULE = registerItem(ExpModuleItem.NAME, ExpModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<RepeatTickModuleItem> ITEM_REPEAT_MODULE = registerItem(RepeatTickModuleItem.NAME, RepeatTickModuleItem::new, EnableMap.EnableOrNot.CONFIG_OFF);
        public static final RegistryObject<FilterModuleItem> ITEM_FILTER_MODULE = registerItem(FilterModuleItem.NAME, FilterModuleItem::new, EnableMap.EnableOrNot.CONFIG_ON);
        // Misc
        public static final RegistryObject<CheckerItemForge> ITEM_CHECKER = registerItem(CheckerItemForge.NAME, CheckerItemForge::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final RegistryObject<YSetterItemForge> ITEM_Y_SET = registerItem(YSetterItemForge.NAME, YSetterItemForge::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final RegistryObject<FrameBlock> BLOCK_FRAME = registerBlock(FrameBlock.NAME, FrameBlock::new);
        public static final RegistryObject<SoftBlock> BLOCK_SOFT = registerBlock(SoftBlock.NAME, SoftBlock::new, b -> b.blockItem);
        public static final RegistryObject<DebugStorageBlock> BLOCK_DEBUG_STORAGE = registerBlock(DebugStorageBlock.NAME, DebugStorageBlock::new);

        private static final Map<Class<? extends QpBlock>, Supplier<BlockEntityType<?>>> BLOCK_ENTITY_TYPES = new HashMap<>();
        public static final RegistryObject<BlockEntityType<QuarryEntityForge>> QUARRY_ENTITY_TYPE = registerBlockEntity(QuarryBlockForge.NAME, BLOCK_QUARRY, QuarryEntityForge::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BlockEntityType<GeneratorEntity>> GENERATOR_ENTITY_TYPE = registerBlockEntity(GeneratorBlock.NAME, BLOCK_GENERATOR, GeneratorEntity::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final RegistryObject<BlockEntityType<NormalMarkerEntityForge>> MARKER_ENTITY_TYPE = registerBlockEntity(NormalMarkerBlock.NAME, BLOCK_MARKER, NormalMarkerEntityForge::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final RegistryObject<BlockEntityType<MoverEntity>> MOVER_ENTITY_TYPE = registerBlockEntity(MoverBlock.NAME, BLOCK_MOVER, MoverEntity::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BlockEntityType<FlexibleMarkerEntityForge>> FLEXIBLE_MARKER_ENTITY_TYPE = registerBlockEntity(FlexibleMarkerBlock.NAME, BLOCK_FLEXIBLE_MARKER, FlexibleMarkerEntityForge::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BlockEntityType<ChunkMarkerEntityForge>> CHUNK_MARKER_ENTITY_TYPE = registerBlockEntity(ChunkMarkerBlock.NAME, BLOCK_CHUNK_MARKER, ChunkMarkerEntityForge::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BlockEntityType<DebugStorageEntity>> DEBUG_STORAGE_TYPE = registerBlockEntity(DebugStorageBlock.NAME, BLOCK_DEBUG_STORAGE, DebugStorageEntity::new, EnableMap.EnableOrNot.ALWAYS_ON);
        public static final RegistryObject<BlockEntityType<AdvQuarryEntityForge>> ADV_QUARRY_ENTITY_TYPE = registerBlockEntity(AdvQuarryBlock.NAME, BLOCK_ADV_QUARRY, AdvQuarryEntityForge::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BlockEntityType<PlacerEntity>> PLACER_ENTITY_TYPE = registerBlockEntity(PlacerBlock.NAME, BLOCK_PLACER, PlacerEntity::new, EnableMap.EnableOrNot.CONFIG_ON);
        public static final RegistryObject<BlockEntityType<RemotePlacerEntity>> REMOTE_PLACER_ENTITY_TYPE = registerBlockEntity(RemotePlacerBlock.NAME, BLOCK_REMOTE_PLACER, RemotePlacerEntity::new, EnableMap.EnableOrNot.CONFIG_OFF);

        public static final RegistryObject<MenuType<? extends YSetterContainer>> Y_SET_MENU_TYPE = registerMenu("gui_y_setter", YSetterContainer::new);
        public static final RegistryObject<MenuType<? extends MoverContainer>> MOVER_MENU_TYPE = registerMenu("gui_mover", MoverContainer::new);
        public static final RegistryObject<MenuType<? extends ModuleContainer>> MODULE_MENU_TYPE = registerMenu("gui_quarry_module", ModuleContainer::new);
        public static final RegistryObject<MenuType<? extends MarkerContainer>> FLEXIBLE_MARKER_MENU_TYPE = registerMenu(MarkerContainer.FLEXIBLE_NAME, MarkerContainer::createFlexibleMarkerContainer);
        public static final RegistryObject<MenuType<? extends MarkerContainer>> CHUNK_MARKER_MENU_TYPE = registerMenu(MarkerContainer.CHUNK_NAME, MarkerContainer::createChunkMarkerContainer);
        public static final RegistryObject<MenuType<? extends DebugStorageContainer>> DEBUG_STORAGE_MENU_TYPE = registerMenu(DebugStorageContainer.NAME, DebugStorageContainer::new);
        public static final RegistryObject<MenuType<? extends AdvQuarryContainer>> ADV_QUARRY_MENU_TYPE = registerMenu(AdvQuarryContainer.NAME, AdvQuarryContainer::new);
        public static final RegistryObject<MenuType<? extends FilterModuleContainer>> FILTER_MODULE_MENU_TYPE = MENU_TYPE_REGISTER.register(FilterModuleContainer.NAME, () ->
            IForgeMenuType.create((windowId, inv, data) -> new FilterModuleContainer(windowId, inv, inv.getSelected())));
        public static final RegistryObject<MenuType<? extends PlacerContainer>> PLACER_MENU_TYPE = registerMenu(PlacerContainer.PLACER_GUI_NAME, PlacerContainer::createPlacerContainer);
        public static final RegistryObject<MenuType<? extends PlacerContainer>> REMOTE_PLACER_MENU_TYPE = registerMenu(PlacerContainer.REMOTE_PLACER_GUI_NAME, PlacerContainer::createRemotePlacerContainer);

        public static final RegistryObject<LootItemFunctionType<? extends MachineLootFunction>> MACHINE_LOOT_FUNCTION = LOOT_TYPE_REGISTER.register(MachineLootFunction.NAME, () -> new LootItemFunctionType<>(MachineLootFunction.SERIALIZER));

        public static final RegistryObject<CreativeModeTab> CREATIVE_MODE_TAB = CREATIVE_TAB_REGISTER.register(QuarryPlus.modID, () -> QuarryPlus.buildCreativeModeTab(CreativeModeTab.builder()).build());
        public static final RegistryObject<RecipeSerializer<InstallBedrockModuleRecipe>> INSTALL_BEDROCK_MODULE_RECIPE = RECIPE_REGISTER.register(InstallBedrockModuleRecipe.NAME, () -> InstallBedrockModuleRecipe.SERIALIZER);

        static {
            for (Map.Entry<ResourceLocation, DataComponentType<?>> e : QuarryDataComponents.ALL.entrySet()) {
                DATA_COMPONENT_TYPE_REGISTER.register(e.getKey().getPath(), e::getValue);
            }
        }

        private static <T extends QpBlock> RegistryObject<T> registerBlock(String name, Supplier<T> supplier) {
            return registerBlock(name, supplier, b -> b.blockItem);
        }

        private static <T extends Block & InCreativeTabs> RegistryObject<T> registerBlock(String name, Supplier<T> supplier, Function<T, ? extends BlockItem> itemGetter) {
            var block = BLOCK_REGISTER.register(name, supplier);
            ITEM_REGISTER.register(name, () -> itemGetter.apply(block.get()));
            TAB_ITEMS.add(block);
            return block;
        }

        private static <T extends Item & InCreativeTabs> RegistryObject<T> registerItem(String name, Supplier<T> supplier, EnableMap.EnableOrNot enableOrNot) {
            var item = ITEM_REGISTER.register(name, supplier);
            TAB_ITEMS.add(item);
            ENABLE_MAP.put(name, enableOrNot);
            return item;
        }

        @SuppressWarnings("unchecked")
        @SafeVarargs
        private static <T extends QpBlock, U extends BlockEntity> RegistryObject<BlockEntityType<U>> registerBlockEntity(String name, RegistryObject<T> block, BlockEntityType.BlockEntitySupplier<U> factory, EnableMap.EnableOrNot enableOrNot, T... dummy) {
            var entityType = BLOCK_ENTITY_REGISTER.register(name, () -> new BlockEntityType<>(factory, Set.of(block.get())));
            BLOCK_ENTITY_TYPES.put((Class<? extends QpBlock>) dummy.getClass().componentType(), (Supplier<BlockEntityType<?>>) (Object) entityType);
            ENABLE_MAP.put(name, enableOrNot);
            return entityType;
        }

        private static <T extends AbstractContainerMenu> RegistryObject<MenuType<? extends T>> registerMenu(String name, GeneralScreenHandler.ContainerFactory<T> factory) {
            return MENU_TYPE_REGISTER.register(name, () ->
                IForgeMenuType.create((i, inventory, friendlyByteBuf) -> factory.create(i, inventory, friendlyByteBuf.readBlockPos()))
            );
        }

        @Override
        public Supplier<? extends QuarryBlock> quarryBlock() {
            return BLOCK_QUARRY;
        }

        @Override
        public Supplier<? extends FrameBlock> frameBlock() {
            return BLOCK_FRAME;
        }

        @Override
        public Supplier<? extends GeneratorBlock> generatorBlock() {
            return BLOCK_GENERATOR;
        }

        @Override
        public Supplier<? extends NormalMarkerBlock> markerBlock() {
            return BLOCK_MARKER;
        }

        @Override
        public Supplier<? extends MoverBlock> moverBlock() {
            return BLOCK_MOVER;
        }

        @Override
        public Supplier<? extends FlexibleMarkerBlock> flexibleMarkerBlock() {
            return BLOCK_FLEXIBLE_MARKER;
        }

        @Override
        public Supplier<? extends ChunkMarkerBlock> chunkMarkerBlock() {
            return BLOCK_CHUNK_MARKER;
        }

        @Override
        public Supplier<? extends DebugStorageBlock> debugStorageBlock() {
            return BLOCK_DEBUG_STORAGE;
        }

        @Override
        public Supplier<? extends AdvQuarryBlock> advQuarryBlock() {
            return BLOCK_ADV_QUARRY;
        }

        @Override
        public Supplier<? extends SoftBlock> softBlock() {
            return BLOCK_SOFT;
        }

        @Override
        public Supplier<? extends PlacerBlock> placerBlock() {
            return BLOCK_PLACER;
        }

        @Override
        public Supplier<? extends RemotePlacerBlock> remotePlacerBlock() {
            return BLOCK_REMOTE_PLACER;
        }

        @Override
        public Optional<BlockEntityType<?>> getBlockEntityType(QpBlock block) {
            var t = BLOCK_ENTITY_TYPES.get(block.getClass());
            if (t == null) {
                QuarryPlus.LOGGER.warn("Unknown block type: {}", block.name);
                return Optional.empty();
            }
            return Optional.of(t.get());
        }

        @Override
        public Map<String, EnableMap.EnableOrNot> defaultEnableSetting() {
            return ENABLE_MAP;
        }

        @Override
        public Supplier<? extends BedrockModuleItem> bedrockModuleItem() {
            return ITEM_BEDROCK_MODULE;
        }

        @Override
        public Stream<Supplier<? extends InCreativeTabs>> allItems() {
            return TAB_ITEMS.stream();
        }

        @Override
        public Supplier<MenuType<? extends YSetterContainer>> ySetterContainer() {
            return Y_SET_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends MoverContainer>> moverContainer() {
            return MOVER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends ModuleContainer>> moduleContainer() {
            return MODULE_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends MarkerContainer>> flexibleMarkerContainer() {
            return FLEXIBLE_MARKER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends MarkerContainer>> chunkMarkerContainer() {
            return CHUNK_MARKER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends DebugStorageContainer>> debugStorageContainer() {
            return DEBUG_STORAGE_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends AdvQuarryContainer>> advQuarryContainer() {
            return ADV_QUARRY_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends FilterModuleContainer>> filterModuleContainer() {
            return FILTER_MODULE_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends PlacerContainer>> placerContainer() {
            return PLACER_MENU_TYPE;
        }

        @Override
        public Supplier<MenuType<? extends PlacerContainer>> remotePlacerContainer() {
            return REMOTE_PLACER_MENU_TYPE;
        }

        @Override
        public Supplier<LootItemFunctionType<? extends MachineLootFunction>> machineLootFunction() {
            return MACHINE_LOOT_FUNCTION;
        }
    }

    @Override
    public String platformName() {
        return "Forge";
    }

    @Override
    public RegisterObjects registerObjects() {
        return itemsLazy.get();
    }

    @Override
    public Packet packetHandler() {
        return packetHandlerLazy.get();
    }

    @Override
    public Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve("%s.toml".formatted(QuarryPlus.modID));
    }

    @Override
    public Supplier<? extends QuarryConfig> getConfig() {
        return configLazy;
    }

    @Override
    public boolean isInDevelopmentEnvironment() {
        return !FMLEnvironment.production;
    }

    @Override
    public Transfer transfer() {
        return transferLazy.get();
    }

    @Override
    public FluidStackLike getFluidInItem(ItemStack stack) {
        if (stack.getItem() instanceof BucketItem bucketItem && !stack.is(Items.BUCKET)) {
            return new FluidStackLike(bucketItem.getFluid(), MachineStorage.ONE_BUCKET, DataComponentPatch.EMPTY);
        }
        return FluidStackLike.EMPTY;
    }

    @Override
    public Component getFluidName(FluidStackLike stack) {
        var s = new FluidStack(stack.fluid(), Math.clamp(stack.amount(), 0, Integer.MAX_VALUE));
        return s.getDisplayName();
    }

    @Override
    public <T extends AbstractContainerMenu> void openGui(ServerPlayer player, GeneralScreenHandler<T> handler) {
        player.openMenu(handler, handler.pos());
    }

    @Override
    public Mining mining() {
        return miningLazy.get();
    }

    @SubscribeEvent
    public void onWorldUnload(ServerStoppedEvent event) {
        configLazy.reset();
    }
}
