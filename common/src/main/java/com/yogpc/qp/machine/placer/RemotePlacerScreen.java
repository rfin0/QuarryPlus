package com.yogpc.qp.machine.placer;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class RemotePlacerScreen extends AbstractContainerScreen<PlacerContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/remote_replacer.png");

    public RemotePlacerScreen(PlacerContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        graphics.blit(LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        {
            int oneBox = 18;
            int x = getMenu().startX - 1 + (getMenu().tile.getLastPlacedIndex() % 3) * oneBox;
            int y = 16 + (getMenu().tile.getLastPlacedIndex() / 3) * oneBox;
            int pX = leftPos + x;
            int pY = topPos + y;
            graphics.blit(LOCATION, pX, pY, 176, 0, oneBox, oneBox);
        }
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < Direction.Axis.VALUES.length; i++) {
            var yPos = topPos + 21 + i * 18;
            this.addRenderableWidget(
                Button.builder(Component.literal("-"), onPress(Direction.Axis.VALUES[i], Direction.AxisDirection.NEGATIVE))
                    .pos(leftPos + 97, yPos)
                    .size(18, 9)
                    .build()
            );
            this.addRenderableWidget(
                Button.builder(Component.literal("+"), onPress(Direction.Axis.VALUES[i], Direction.AxisDirection.POSITIVE))
                    .pos(leftPos + 151, yPos)
                    .size(18, 9)
                    .build()
            );
        }
    }

    private Button.OnPress onPress(Direction.Axis axis, Direction.AxisDirection axisDirection) {
        return button -> {
            if (getMenu().tile instanceof RemotePlacerEntity remotePlacer) {
                BlockPos newPos = remotePlacer.getTargetPos().relative(axis, axisDirection.getStep());
                remotePlacer.targetPos = newPos;
                PlatformAccess.getAccess().packetHandler().sendToServer(new RemotePlacerMessage(remotePlacer, newPos));
            }
        };
    }
}
