package com.terraformersmc.modmenu.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.terraformersmc.modmenu.util.ModrinthUtil;
import net.flintloader.loader.api.event.client.ClientTickEvent;
import net.flintloader.loader.api.event.client.ScreenEvent;
import net.flintloader.loader.api.event.client.ScreenEvents;
import net.flintloader.loader.api.keybinding.KeyMappingHelper;
import net.flintloader.loader.api.screens.Screens;
import net.flintloader.loader.core.event.FlintEventBus;
import net.flintloader.loader.core.event.annot.EventBusListener;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ModMenuEventHandler {
	public static final ResourceLocation FABRIC_ICON_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/mods_button.png");
	private static KeyMapping MENU_KEY_BIND;

	public static void register() {
		MENU_KEY_BIND = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.modmenu.open_menu",
				InputConstants.Type.KEYSYM,
			    InputConstants.UNKNOWN.getValue(),
				"key.categories.misc"
		));
		FlintEventBus.INSTANCE.registerEventListener(ModMenuEventHandler.class);
	}

	@EventBusListener
	public static void afterScreenInit(ScreenEvents.AfterInit event) {
		if (event.getScreen() instanceof TitleScreen titleScreen) {
			afterTitleScreenInit(titleScreen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<AbstractWidget> buttons = Screens.getButtons(screen);
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				AbstractWidget widget = buttons.get(i);
				if (widget instanceof Button button) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
						if (button.visible) {
							shiftButtons(button, modsButtonIndex == -1, spacing);
							if (modsButtonIndex == -1) {
								buttonsY = button.getY();
							}
						}
					}
					if (buttonHasText(button, "menu.online")) {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
							buttons.set(i, new ModMenuButtonWidget(button.getX(), button.getY(), button.getWidth(), button.getHeight(), ModMenuApi.createModsButtonText(), screen));
						} else {
							if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
								button.setWidth(98);
							}
							modsButtonIndex = i + 1;
							if (button.visible) {
								buttonsY = button.getY();
							}
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100, buttonsY + spacing, 200, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 + 2, buttonsY, 98, 20, ModMenuApi.createModsButtonText(), screen));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(screen.width / 2 + 104, buttonsY, 20, 20, 0, 0, 20, FABRIC_ICON_BUTTON_LOCATION, 32, 64, button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)), ModMenuApi.createModsButtonText()));
				}
			}
		}
		ModrinthUtil.triggerV2DeprecatedToast();
	}

	@EventBusListener
	private static void onClientEndTick(ClientTickEvent.TickEnd event) {
		while (MENU_KEY_BIND.isDown()) {
			//event.getMinecraft().setScreen(new ModsScreen(event.getMinecraft().screen));
		}
	}

	public static boolean buttonHasText(LayoutElement widget, String translationKey) {
		if (widget instanceof Button button) {
			Component text = button.getMessage();
			ComponentContents textContent = text.getContents();
			return textContent instanceof TranslatableContents && ((TranslatableContents) textContent).getKey().equals(translationKey);
		}
		return false;
	}

	public static void shiftButtons(LayoutElement widget, boolean shiftUp, int spacing) {
		if (shiftUp) {
			widget.setY(widget.getY() - spacing / 2);
		} else if (!(widget instanceof AbstractButton button && button.getMessage().equals(TitleScreen.COPYRIGHT_TEXT))) {
			widget.setY(widget.getY() + spacing / 2);
		}
	}
}
