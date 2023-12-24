package com.terraformersmc.modmenu.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Random;

public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void drawRandomVersionBackground(Mod mod, GuiGraphics DrawContext, int x, int y, int width, int height) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | Mth.hsvToRgb(random.nextFloat(1f), random.nextFloat(0.7f, 0.8f), 0.9f);
		if (!ModMenuConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		DrawContext.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(GuiGraphics DrawContext, String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<FormattedText> strings = CLIENT.font.getSplitter().splitLines(Component.literal(string), wrapWidth, Style.EMPTY);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			FormattedText renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = FormattedText.composite(strings.get(i), FormattedText.of("..."));
			}
			FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
			int x1 = x;
			if (CLIENT.font.isBidirectional()) {
				int width = CLIENT.font.width(line);
				x1 += (float) (wrapWidth - width);
			}
			DrawContext.drawString(CLIENT.font, line, x1, y + i * CLIENT.font.lineHeight, color, false);
		}
	}

	public static void drawBadge(GuiGraphics DrawContext, int x, int y, int tagWidth, FormattedCharSequence text, int outlineColor, int fillColor, int textColor) {
		DrawContext.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		DrawContext.fill(x, y, x + 1, y + CLIENT.font.lineHeight, outlineColor);
		DrawContext.fill(x + 1, y + 1 + CLIENT.font.lineHeight - 1, x + tagWidth, y + CLIENT.font.lineHeight + 1, outlineColor);
		DrawContext.fill( x + tagWidth, y, x + tagWidth + 1, y + CLIENT.font.lineHeight, outlineColor);
		DrawContext.fill( x + 1, y, x + tagWidth, y + CLIENT.font.lineHeight, fillColor);
		DrawContext.drawString(CLIENT.font, text, (int) (x + 1 + (tagWidth - CLIENT.font.width(text)) / (float) 2), y + 1, textColor, false);
	}
}
