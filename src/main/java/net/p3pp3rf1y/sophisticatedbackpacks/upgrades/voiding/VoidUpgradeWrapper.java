package net.p3pp3rf1y.sophisticatedbackpacks.upgrades.voiding;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IInsertResponseUpgrade;
import net.p3pp3rf1y.sophisticatedbackpacks.api.IPickupResponseUpgrade;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.IFilteredUpgrade;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.UpgradeWrapperBase;
import net.p3pp3rf1y.sophisticatedbackpacks.util.BackpackInventoryHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.util.IBackpackWrapper;

import java.util.function.Consumer;

public class VoidUpgradeWrapper extends UpgradeWrapperBase<VoidUpgradeWrapper, VoidUpgradeItem>
		implements IPickupResponseUpgrade, IInsertResponseUpgrade, IFilteredUpgrade {
	private final FilterLogic filterLogic;

	public VoidUpgradeWrapper(ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(upgrade, upgradeSaveHandler);
		filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount());
		filterLogic.setAllowByDefault();
	}

	@Override
	public ItemStack pickup(World world, ItemStack stack, IBackpackWrapper backpack, boolean simulate) {
		if (filterLogic.matchesFilter(stack)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	@Override
	public ItemStack onBeforeInsert(BackpackInventoryHandler inventoryHandler, int slot, ItemStack stack, boolean simulate) {
		if (filterLogic.matchesFilter(stack)) {
			return ItemStack.EMPTY;
		}
		return stack;
	}

	@Override
	public void onAfterInsert(BackpackInventoryHandler inventoryHandler, int slot) {
		//noop
	}

	@Override
	public FilterLogic getFilterLogic() {
		return filterLogic;
	}
}
