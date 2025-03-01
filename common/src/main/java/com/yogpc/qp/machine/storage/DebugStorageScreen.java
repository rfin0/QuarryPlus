package com.yogpc.qp.machine.storage;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class DebugStorageScreen extends AbstractContainerScreen<DebugStorageContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/marker.png");
    ItemCountList itemCountList;

    public DebugStorageScreen(DebugStorageContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 188;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(RenderType::guiTextured, LOCATION, pX, pY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void init() {
        super.init();

        itemCountList = new ItemCountList(this.minecraft, 205, inventoryLabelY - 20, topPos + 18, getMenu().storage);
        itemCountList.setX(leftPos + (imageWidth - 205) / 2);
        addRenderableWidget(itemCountList);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (itemCountList.isMouseOver(mouseX, mouseY)) {
            if (itemCountList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
