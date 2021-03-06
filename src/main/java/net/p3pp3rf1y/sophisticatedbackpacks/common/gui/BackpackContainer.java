package net.p3pp3rf1y.sophisticatedbackpacks.common.gui;

import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.blocks.tile.BackpackTileEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.items.ScreenProperties;
import net.p3pp3rf1y.sophisticatedbackpacks.util.BackpackInventoryHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.util.BackpackUpgradeHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.util.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.NoopBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import net.p3pp3rf1y.sophisticatedbackpacks.util.WorldHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK_BLOCK_CONTAINER_TYPE;
import static net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.BACKPACK_ITEM_CONTAINER_TYPE;

public class BackpackContainer extends Container {
	private static final int NUMBER_OF_PLAYER_SLOTS = 36;

	private final IBackpackWrapper backpackWrapper;
	private int backpackSlotNumber = -1;

	private final Map<Integer, UpgradeContainerBase<?, ?>> upgradeContainers = new LinkedHashMap<>();
	private Consumer<BackpackContainer> upgradeChangeListener = null;

	public BackpackContainer(int windowId, PlayerEntity player, String handlerName, int backpackSlot) {
		super(BACKPACK_ITEM_CONTAINER_TYPE.get(), windowId);
		Optional<PlayerInventoryHandler> h = PlayerInventoryProvider.getPlayerInventoryHandler(handlerName);

		if (!h.isPresent()) {
			backpackWrapper = NoopBackpackWrapper.INSTANCE;
			return;
		}
		PlayerInventoryHandler handler = h.get();
		backpackWrapper = handler.getStackInSlot(player, backpackSlot).getCapability(BackpackWrapper.BACKPACK_WRAPPER_CAPABILITY).orElse(NoopBackpackWrapper.INSTANCE);

		int yPosition = addBackpackInventorySlots();
		addBackpackUpgradeSlots(yPosition, player.world.isRemote);
		addPlayerInventorySlots(player.inventory, yPosition, backpackSlot, handler.isVisibleInGui());
		addUpgradeSettingsContainers(player.world.isRemote);
	}

	public BackpackContainer(int windowId, PlayerEntity player, BlockPos pos) {
		super(BACKPACK_BLOCK_CONTAINER_TYPE.get(), windowId);
		Optional<BackpackTileEntity> backpackTile = WorldHelper.getTile(player.world, pos, BackpackTileEntity.class);
		backpackWrapper = backpackTile.map(te -> te.getBackpackWrapper().orElse(NoopBackpackWrapper.INSTANCE)).orElse(NoopBackpackWrapper.INSTANCE);

		int yPosition = addBackpackInventorySlots();
		addBackpackUpgradeSlots(yPosition, player.world.isRemote);
		addPlayerInventorySlots(player.inventory, yPosition, -1, false);
		addUpgradeSettingsContainers(player.world.isRemote);

	}

	private void addUpgradeSettingsContainers(boolean isClientSide) {
		BackpackUpgradeHandler upgradeHandler = backpackWrapper.getUpgradeHandler();
		upgradeHandler.getSlotWrappers().forEach((slot, wrapper) -> UpgradeContainerRegistry.instantiateContainer(slot, wrapper, isClientSide)
				.ifPresent(container -> upgradeContainers.put(slot, container)));

		for (UpgradeContainerBase<?, ?> container : upgradeContainers.values()) {
			container.getSlots().forEach(this::addSlot);
		}
	}

	private void addBackpackUpgradeSlots(int lastInventoryRowY, boolean isClientSide) {
		BackpackUpgradeHandler upgradeHandler = backpackWrapper.getUpgradeHandler();

		int numberOfSlots = upgradeHandler.getSlots();

		if (numberOfSlots == 0) {
			return;
		}

		int slotIndex = 0;

		int yPosition = lastInventoryRowY - (22 + 22 * (numberOfSlots - 1));

		while (slotIndex < upgradeHandler.getSlots()) {
			addSlot(new BackpackUpgradeSlot(upgradeHandler, slotIndex, yPosition, isClientSide));

			slotIndex++;
			yPosition += 22;
		}
	}

	public void setUpgradeChangeListener(Consumer<BackpackContainer> upgradeChangeListener) {
		this.upgradeChangeListener = upgradeChangeListener;
	}

	private int addBackpackInventorySlots() {
		BackpackInventoryHandler inventoryHandler = backpackWrapper.getInventoryHandler();
		int slotIndex = 0;
		int yPosition = 18;

		while (slotIndex < inventoryHandler.getSlots()) {
			int lineIndex = slotIndex % getSlotsOnLine();
			int finalSlotIndex = slotIndex;
			addSlot(new BackpackInventorySlot(inventoryHandler, finalSlotIndex, lineIndex, yPosition));

			slotIndex++;
			if (slotIndex % getSlotsOnLine() == 0) {
				yPosition += 18;
			}
		}

		return yPosition;
	}

	private void addPlayerInventorySlots(PlayerInventory playerInventory, int yPosition, int slotIndex, boolean lockBackpackSlot) {
		int playerInventoryYOffset = backpackWrapper.getScreenProperties().getPlayerInventoryYOffset();

		yPosition += 14;

		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlot(new Slot(playerInventory, j + i * 9 + 9, playerInventoryYOffset + 8 + j * 18, yPosition));
			}
			yPosition += 18;
		}

		yPosition += 4;

		for (int k = 0; k < 9; ++k) {
			Slot slot = addSlot(new Slot(playerInventory, k, playerInventoryYOffset + 8 + k * 18, yPosition));
			if (lockBackpackSlot && k == slotIndex) {
				backpackSlotNumber = slot.slotNumber;
			}
		}
	}

	public int getNumberOfRows() {
		BackpackInventoryHandler invHandler = backpackWrapper.getInventoryHandler();
		return (int) Math.ceil((double) invHandler.getSlots() / getSlotsOnLine());
	}

	private int getSlotsOnLine() {
		return backpackWrapper.getScreenProperties().getSlotsOnLine();
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	public static BackpackContainer fromBufferItem(int windowId, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
		return new BackpackContainer(windowId, playerInventory.player, packetBuffer.readString(), packetBuffer.readInt());
	}

	public static BackpackContainer fromBufferBlock(int windowId, PlayerInventory playerInventory, PacketBuffer packetBuffer) {
		return new BackpackContainer(windowId, playerInventory.player, BlockPos.fromLong(packetBuffer.readLong()));
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();

			if (index == backpackSlotNumber) {
				return ItemStack.EMPTY;
			}
			itemstack = slotStack.copy();
			int backpackSlots = getBackpackSlotsCount();
			if (index < backpackSlots + getNumberOfUpgradeSlots()) {
				if (!mergeItemStack(slotStack, backpackSlots + getNumberOfUpgradeSlots(), getFirstUpgradeSettingsSlot(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(slotStack, backpackSlots, backpackSlots + getNumberOfUpgradeSlots(), false)
					&& !mergeItemStack(slotStack, 0, backpackSlots, false)) {
				return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	private int getBackpackSlotsCount() {
		return getNumberOfRows() * getSlotsOnLine();
	}

	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, PlayerEntity player) {
		if (slotId == backpackSlotNumber) {
			return ItemStack.EMPTY;
		} else if (slotId >= getFirstUpgradeSettingsSlot() && (getSlot(slotId) instanceof FilterSlotItemHandler) && getSlot(slotId).isItemValid(player.inventory.getItemStack())) {
			ItemStack currentStack = player.inventory.getItemStack().copy();
			if (currentStack.getCount() > 1) {
				currentStack.setCount(1);
			}

			getSlot(slotId).putStack(currentStack);
			return ItemStack.EMPTY;
		}
		return super.slotClick(slotId, dragType, clickType, player);
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.slotNumber < getFirstUpgradeSettingsSlot();
	}

	public int getNumberOfSlots() {
		return backpackWrapper.getInventoryHandler().getSlots();
	}

	public ScreenProperties getScreenProperties() {
		return backpackWrapper.getScreenProperties();
	}

	public int getNumberOfUpgradeSlots() {
		return backpackWrapper.getUpgradeHandler().getSlots();
	}

	public int getFirstUpgradeSettingsSlot() {
		return getNumberOfSlots() + getNumberOfUpgradeSlots() + NUMBER_OF_PLAYER_SLOTS;
	}

	public Collection<UpgradeContainerBase<?, ?>> getUpgradeContainers() {
		return upgradeContainers.values();
	}

	public void handleMessage(CompoundNBT data) {
		int containerId = data.getInt("containerId");
		if (upgradeContainers.containsKey(containerId)) {
			upgradeContainers.get(containerId).handleMessage(data);
		}
	}

	public class BackpackUpgradeSlot extends SlotItemHandler {
		private final boolean isClientSide;

		public BackpackUpgradeSlot(BackpackUpgradeHandler upgradeHandler, int slotIndex, int yPosition, boolean isClientSide) {
			super(upgradeHandler, slotIndex, -18, yPosition);
			this.isClientSide = isClientSide;
		}

		@Override
		public void onSlotChanged() {
			super.onSlotChanged();
			boolean upgradeControlNeedsReloading = false;
			if (upgradeContainers.size() != backpackWrapper.getUpgradeHandler().getSlotWrappers().size()) {
				upgradeControlNeedsReloading = true;
			} else {
				for (Map.Entry<Integer, IUpgradeWrapper> slotWrapper : backpackWrapper.getUpgradeHandler().getSlotWrappers().entrySet()) {
					UpgradeContainerBase<?, ?> container = upgradeContainers.get(slotWrapper.getKey());
					if (container == null || container.getUpgradeWrapper() != slotWrapper.getValue()) {
						if (container == null || container.getUpgradeWrapper().getUpgradeStack().getItem() != slotWrapper.getValue().getUpgradeStack().getItem()) {
							upgradeControlNeedsReloading = true;
							break;
						} else {
							container.setUpgradeWrapper(slotWrapper.getValue());
						}
					}
				}
			}
			if (upgradeControlNeedsReloading) {
				reloadUpgradeControl();
			}
		}

		private void reloadUpgradeControl() {
			removeUpgradeSettingsSlots();
			upgradeContainers.clear();
			addUpgradeSettingsContainers(isClientSide);
			onUpgradesChanged();
		}

		private void removeUpgradeSettingsSlots() {
			List<Integer> slotNumbersToRemove = new ArrayList<>();
			for (UpgradeContainerBase<?, ?> container : upgradeContainers.values()) {
				container.getSlots().forEach(slot -> {
					slotNumbersToRemove.add(slot.slotNumber);
					inventorySlots.remove(slot);
				});
			}
			slotNumbersToRemove.sort(IntComparators.OPPOSITE_COMPARATOR);
			for (int slotNumber : slotNumbersToRemove) {
				inventoryItemStacks.remove(slotNumber);
			}
		}

		private void onUpgradesChanged() {
			if (upgradeChangeListener != null) {
				upgradeChangeListener.accept(BackpackContainer.this);
			}
		}
	}

	private class BackpackInventorySlot extends SlotItemHandler {
		public BackpackInventorySlot(BackpackInventoryHandler inventoryHandler, int finalSlotIndex, int lineIndex, int yPosition) {super(inventoryHandler, finalSlotIndex, 8 + lineIndex * 18, yPosition);}

		@Override
		public void onSlotChanged() {
			super.onSlotChanged();
			// saving here as well because there are many cases where vanilla modifies stack directly without and inventory handler isn't aware of it
			// however it does notify the slot of change
			backpackWrapper.getInventoryHandler().saveInventory();
		}
	}
}
