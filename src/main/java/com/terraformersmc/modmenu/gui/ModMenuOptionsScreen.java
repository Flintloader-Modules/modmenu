package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public class ModMenuOptionsScreen extends OptionsScreen {

	private Screen previous;
	private OptionsList list;

	@SuppressWarnings("resource")
	public ModMenuOptionsScreen(Screen previous) {
		super(previous, Minecraft.getInstance().options);
		this.previous = previous;
	}


	protected void init() {
		this.list = new OptionsList(this.minecraft, this.width, this.height - 64, 32, 25);
		this.list.addSmall(ModMenuConfig.asOptions());
		this.addWidget(this.list);
		this.addRenderableWidget(
				Button.builder(CommonComponents.GUI_DONE, (button) -> {
							ModMenuConfigManager.save();
							this.minecraft.setScreen(this.previous);
						}).pos(this.width / 2 - 100, this.height - 27)
						.size(200, 20)
						.build());
	}

	@Override
	public void render(GuiGraphics DrawContext, int mouseX, int mouseY, float delta) {
		super.render(DrawContext, mouseX, mouseY, delta);
		this.list.render(DrawContext, mouseX, mouseY, delta);
		DrawContext.drawString(this.font, this.title, this.width / 2, 5, 0xffffff);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
	}

	public void removed() {
		ModMenuConfigManager.save();
	}
}
