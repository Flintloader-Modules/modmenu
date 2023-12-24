package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptionListWidget extends AbstractSelectionList<DescriptionListWidget.DescriptionEntry> {

	private final ModsScreen parent;
	private final Font textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(Minecraft client, int width, int height, int top, int entryHeight, ModsScreen parent) {
		super(client, width, height, top, entryHeight);
		this.parent = parent;
		this.textRenderer = client.font;
	}

	@Nullable
	@Override
	public DescriptionEntry getSelected() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + getX();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
		Mod mod = parent.getSelectedEntry().getMod();
		narrationElementOutput.add(NarratedElementType.TITLE, mod.getTranslatedName() + " " + mod.getPrefixedVersion());
	}

	@Override
	public void renderWidget(GuiGraphics DrawContext, int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			if (lastSelected != null) {
				Mod mod = lastSelected.getMod();
				String description = mod.getTranslatedDescription();
				if (!description.isEmpty()) {
					for (FormattedCharSequence line : textRenderer.split(Component.literal(description.replaceAll("\n", "\n\n")), getRowWidth() - 5)) {
						children().add(new DescriptionEntry(line));
					}
				}

				if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(mod.getId())) {
					if (mod.getModrinthData() != null) {
						children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
						children().add(new DescriptionEntry(Component.translatable("modmenu.hasUpdate").getVisualOrderText()).setUpdateTextEntry());
						children().add(new DescriptionEntry(Component.translatable("modmenu.experimental").withStyle(ChatFormatting.GOLD).getVisualOrderText(), 8));
						children().add(new LinkEntry(
							Component.translatable("modmenu.updateText", mod.getModrinthData().versionNumber(), Component.translatable("modmenu.modrinth"))
								.withStyle(ChatFormatting.BLUE)
								.withStyle(ChatFormatting.UNDERLINE)
								.getVisualOrderText(), "https://modrinth.com/project/%s/version/%s".formatted(mod.getModrinthData().projectId(), mod.getModrinthData().versionId()), 8));
					}
					if (mod.getChildHasUpdate()) {
						children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
						children().add(new DescriptionEntry(Component.translatable("modmenu.childHasUpdate").getVisualOrderText()).setUpdateTextEntry());
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
					children().add(new DescriptionEntry(Component.translatable("modmenu.links").getVisualOrderText()));

					if (sourceLink != null) {
						children().add(new LinkEntry(Component.translatable("modmenu.source").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE).getVisualOrderText(), sourceLink, 8));
					}

					links.forEach((key, value) -> {
						children().add(new LinkEntry(Component.translatable(key).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE).getVisualOrderText(), value, 8));
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
					children().add(new DescriptionEntry(Component.translatable("modmenu.license").getVisualOrderText()));

					for (String license : licenses) {
						children().add(new DescriptionEntry(Component.literal(license).getVisualOrderText(), 8));
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
						children().add(new MojangCreditsEntry(Component.translatable("modmenu.viewCredits").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.UNDERLINE).getVisualOrderText()));
					} else if ("java".equals(mod.getId())) {
						children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
					} else {
						List<String> credits = mod.getCredits();
						if (!credits.isEmpty()) {
							children().add(new DescriptionEntry(FormattedCharSequence.EMPTY));
							children().add(new DescriptionEntry(Component.translatable("modmenu.credits").getVisualOrderText()));
							for (String credit : credits) {
								int indent = 8;
								for (FormattedCharSequence line : textRenderer.split(Component.literal(credit), getRowWidth() - 5 - 16)) {
									children().add(new DescriptionEntry(line, indent));
									indent = 16;
								}
							}
						}
					}
				}
			}
		}

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();

		{
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(this.getX(), this.getBottom(), 0.0D).uv(this.getX() / 32.0F, (this.getBottom() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
			bufferBuilder.vertex(this.getRight(), this.getBottom(), 0.0D).uv(this.getRight() / 32.0F, (this.getBottom() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
			bufferBuilder.vertex(this.getRight(), this.getY(), 0.0D).uv(this.getRight() / 32.0F, (this.getY() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
			bufferBuilder.vertex(this.getX(), this.getY(), 0.0D).uv(this.getX() / 32.0F, (this.getY() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
			tessellator.end();
		}

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(this.getX(), (this.getY() + 4), 0.0D).

			color(0, 0, 0, 0).

			endVertex();
		bufferBuilder.vertex(this.getRight(), (this.getY() + 4), 0.0D).

			color(0, 0, 0, 0).

			endVertex();
		bufferBuilder.vertex(this.getRight(), this.getY(), 0.0D).

			color(0, 0, 0, 255).

			endVertex();
		bufferBuilder.vertex(this.getX(), this.getY(), 0.0D).

			color(0, 0, 0, 255).

			endVertex();
		bufferBuilder.vertex(this.getX(), this.getBottom(), 0.0D).

			color(0, 0, 0, 255).

			endVertex();
		bufferBuilder.vertex(this.getRight(), this.getBottom(), 0.0D).

			color(0, 0, 0, 255).

			endVertex();
		bufferBuilder.vertex(this.getRight(), (this.getBottom() - 4), 0.0D).

			color(0, 0, 0, 0).

			endVertex();
		bufferBuilder.vertex(this.getX(), (this.getBottom() - 4), 0.0D).

			color(0, 0, 0, 0).

			endVertex();
		tessellator.end();

		this.enableScissor(DrawContext);
		this.renderList(DrawContext, mouseX, mouseY, delta);
		DrawContext.disableScissor();

		this.renderScrollBar(bufferBuilder, tessellator);

		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tesselator tessellator) {
		int scrollbarStartX = this.getScrollbarPosition();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			int p = (int) ((float) ((this.getBottom() - this.getY()) * (this.getBottom() - this.getY())) / (float) this.getMaxPosition());
			p = Mth.clamp(p, 32, this.getBottom() - this.getY() - 8);
			int q = (int) this.getScrollAmount() * (this.getBottom() - this.getY() - p) / maxScroll + this.getY();
			if (q < this.getY()) {
				q = this.getY();
			}

			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(scrollbarStartX, this.getBottom(), 0.0D).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, this.getBottom(), 0.0D).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, this.getY(), 0.0D).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, this.getY(), 0.0D).color(0, 0, 0, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q + p, 0.0D).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, q + p, 0.0D).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX, q, 0.0D).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).color(128, 128, 128, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q + p - 1, 0.0D).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX - 1, q + p - 1, 0.0D).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex(scrollbarEndX - 1, q, 0.0D).color(192, 192, 192, 255).endVertex();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).color(192, 192, 192, 255).endVertex();
			tessellator.end();
		}
	}

	protected class DescriptionEntry extends ContainerObjectSelectionList.Entry<DescriptionEntry> {
		protected FormattedCharSequence text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(FormattedCharSequence text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(FormattedCharSequence text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(GuiGraphics DrawContext, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(DrawContext, x + indent, y);
				x += 11;
			}
			DrawContext.drawString(textRenderer, text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return null;
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(FormattedCharSequence text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new MinecraftCredits());
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		class MinecraftCredits extends CreditsAndAttributionScreen {
			public MinecraftCredits() {
				super(parent);
			}
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(FormattedCharSequence text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(FormattedCharSequence text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getPlatform().openUri(link);
					}
					minecraft.setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}
