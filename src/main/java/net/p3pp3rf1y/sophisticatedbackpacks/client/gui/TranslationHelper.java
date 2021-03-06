package net.p3pp3rf1y.sophisticatedbackpacks.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TranslationHelper {
	private TranslationHelper() {}

	private static final String GUI_UPGRADE_PREFIX = "gui.sophisticatedbackpacks.upgrades.";
	private static final String ITEM_UPGRADE_PREFIX = "item.sophisticatedbackpacks.";
	private static final String UPGRADE_BUTTONS_PREFIX = GUI_UPGRADE_PREFIX + "buttons.";

	public static String translUpgrade(String upgradeName) {
		return GUI_UPGRADE_PREFIX + upgradeName;
	}

	public static String translUpgradeTooltip(String upgradeName) {
		return translUpgrade(upgradeName) + ".tooltip";
	}

	public static String translUpgradeButton(String buttonName) {
		return UPGRADE_BUTTONS_PREFIX + buttonName;
	}

	public static String translUpgradeItemTooltip(String upgradeName) {
		return ITEM_UPGRADE_PREFIX + upgradeName + ".tooltip";
	}

	public static List<StringTextComponent> getTranslatedLines(String translateKey, @Nullable Object parameters, TextFormatting... textFormattings) {
		List<StringTextComponent> ret = getTranslatedLines(translateKey, parameters);
		ret.forEach(l -> l.mergeStyle(textFormattings));
		return ret;
	}

	public static List<StringTextComponent> getTranslatedLines(String translateKey, @Nullable Object parameters) {
		String text = I18n.format(translateKey, parameters);

		String[] lines = text.split("\n");

		List<StringTextComponent> ret = new ArrayList<>();
		for (String line : lines) {
			ret.add(new StringTextComponent(line));
		}

		return ret;
	}
}
