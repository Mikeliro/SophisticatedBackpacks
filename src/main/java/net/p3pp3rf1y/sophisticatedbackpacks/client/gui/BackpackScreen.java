package net.p3pp3rf1y.sophisticatedbackpacks.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BackpackScreen extends ContainerScreen<BackpackContainer> {
	private static final Map<Integer, ResourceLocation> BACKPACK_TEXTURES = new HashMap<>();
	private static final ResourceLocation UPGRADE_CONTROLS = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/upgrade_controls.png");
	private static final int UPGRADE_TOP_HEIGHT = 7;
	private static final int UPGRADE_SLOT_HEIGHT = 18;
	private static final int UPGRADE_SPACE_BETWEEN_SLOTS = 4;
	private static final int UPGRADE_BOTTOM_HEIGHT = 7;
	private static final int TOTAL_UPGRADE_GUI_HEIGHT = 252;
	public static final int UPGRADE_INVENTORY_OFFSET = 26;
	private UpgradeSettingsControl upgradeControl;

	private final int slots;

	public BackpackScreen(BackpackContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		ySize = 114 + getContainer().getNumberOfRows() * 18;
		xSize = getContainer().getScreenProperties().getSlotsOnLine() * 18 + 14;
		playerInventoryTitleY = ySize - 94;
		playerInventoryTitleX = 8 + getContainer().getScreenProperties().getPlayerInventoryYOffset();
		slots = getContainer().getNumberOfUpgradeSlots();
	}

	@Override
	protected void init() {
		super.init();
		initUpgradeControl();
		getContainer().setUpgradeChangeListener(c -> {
			children.remove(upgradeControl);
			initUpgradeControl();
		});
	}

	private void initUpgradeControl() {
		upgradeControl = new UpgradeSettingsControl(new Position(guiLeft + xSize, guiTop + 4), this);
		addListener(upgradeControl);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		upgradeControl.render(matrixStack, mouseX, mouseY, partialTicks);
		matrixStack.translate(0, 0, 200);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		renderHoveredTooltip(matrixStack, mouseX, mouseY);
	}

	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
		drawInventoryBackground(matrixStack);
		drawUpgradeBackground(matrixStack);
	}

	@Override
	protected void renderHoveredTooltip(MatrixStack matrixStack, int x, int y) {
		super.renderHoveredTooltip(matrixStack, x, y);
		GuiHelper.renderToolTip(minecraft, matrixStack, x, y);
	}

	private void drawInventoryBackground(MatrixStack matrixStack) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(getBackpackTexture(container.getNumberOfSlots()));
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		int textureSize = container.getScreenProperties().getTextureSize();
		blit(matrixStack, i, j, 0, 0, xSize, ySize, textureSize, textureSize);
	}

	private void drawUpgradeBackground(MatrixStack matrixStack) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(UPGRADE_CONTROLS);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;

		int firstHalfHeight = getUpgradeHeightWithoutBottom();

		blit(matrixStack, i - UPGRADE_INVENTORY_OFFSET, j + getUpgradeTop(), 0, 0, 29, firstHalfHeight, 256, 256);
		blit(matrixStack, i - UPGRADE_INVENTORY_OFFSET, j + getUpgradeTop() + firstHalfHeight, 0, (float) TOTAL_UPGRADE_GUI_HEIGHT - UPGRADE_BOTTOM_HEIGHT, 29, UPGRADE_BOTTOM_HEIGHT, 256, 256);
	}

	public int getUpgradeTop() {
		return ySize - 94 - getUpgradeHeight();
	}

	public int getUpgradeHeight() {
		return getUpgradeHeightWithoutBottom() + UPGRADE_TOP_HEIGHT;
	}

	private int getUpgradeHeightWithoutBottom() {
		return UPGRADE_BOTTOM_HEIGHT + slots * UPGRADE_SLOT_HEIGHT + (slots - 1) * UPGRADE_SPACE_BETWEEN_SLOTS;
	}

	private static ResourceLocation getBackpackTexture(int numberOfSlots) {
		if (!BACKPACK_TEXTURES.containsKey(numberOfSlots)) {
			BACKPACK_TEXTURES.put(numberOfSlots, new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/backpack_" + numberOfSlots + ".png"));
		}

		return BACKPACK_TEXTURES.get(numberOfSlots);
	}

	public UpgradeSettingsControl getUpgradeControl() {
		if (upgradeControl == null) {
			upgradeControl = new UpgradeSettingsControl(new Position(guiLeft + xSize, guiTop + 4), this);
		}
		return upgradeControl;
	}
}
