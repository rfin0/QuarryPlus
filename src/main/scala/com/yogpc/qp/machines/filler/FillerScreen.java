package com.yogpc.qp.machines.filler;

import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FillerScreen extends AbstractContainerScreen<FillerMenu> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/filler.png");

    public FillerScreen(FillerMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        var id = new AtomicInteger(0);
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + this.getXSize() - 60 - 8, this.getGuiTop() + 7,
            60, 20, Component.literal("FillAll"), this));
        this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + this.getXSize() - 60 - 8, this.getGuiTop() + 7 + 20,
            60, 20, Component.literal("FillBox"), this));
        // this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + this.getXSize() - 60 - 8, this.getGuiTop() + 7 + 40,
        //     60, 20, Component.literal("Pillar"), this));
        // this.addRenderableWidget(new IndexedButton(id.getAndIncrement(), this.getGuiLeft() + 110, this.getGuiTop() + this.getYSize() - 97,
        //     60, 14, Component.literal("Modules"), this));
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.blit(pPoseStack, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public void onPress(Button pButton) {
        if (pButton instanceof IndexedButton indexedButton) {
            switch (indexedButton.getIndex()) {
                case 0 -> PacketHandler.sendToServer(new FillerButtonMessage(menu.filler, FillerEntity.Action.BOX));
                case 1 -> PacketHandler.sendToServer(new FillerButtonMessage(menu.filler, FillerEntity.Action.WALL));
                case 2 -> PacketHandler.sendToServer(new FillerButtonMessage(menu.filler, FillerEntity.Action.PILLAR));
                default -> QuarryPlus.LOGGER.error("Unknown button({}, {}) is pushed in {}", indexedButton, indexedButton.getIndex(), this);
            }
        } else {
            QuarryPlus.LOGGER.error("Unknown button({}) is pushed in {}", pButton, this);
        }
    }
}
