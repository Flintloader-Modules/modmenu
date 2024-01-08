package com.terraformersmc.modmenu.util.mod.flint;

import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModrinthData;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import net.flintloader.loader.api.FlintModuleContainer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class FlintModule implements Mod {

	private final FlintModuleContainer metadata;

	public FlintModule(FlintModuleContainer metadata) {
		this.metadata = metadata;
	}

	@Override
	public @NotNull String getId() {
		return metadata.getMetadata().getId();
	}

	@Override
	public @NotNull String getName() {
		return metadata.getMetadata().getName();
	}

	@Override
	public @NotNull String getTranslatedName() {
		return metadata.getMetadata().getName();
	}

	@Override
	public @NotNull NativeImageBackedTexture getIcon(FabricIconHandler iconHandler, int i) {
		return iconHandler.createIcon(metadata, metadata.getMetadata().getIcon());
	}

	@Override
	public @NotNull String getSummary() {
		return "";
	}

	@Override
	public @NotNull String getTranslatedSummary() {
		return "";
	}

	@Override
	public @NotNull String getDescription() {
		return metadata.getMetadata().getDescription();
	}

	@Override
	public @NotNull String getTranslatedDescription() {
		return metadata.getMetadata().getDescription();
	}

	@Override
	public @NotNull String getVersion() {
		return metadata.getMetadata().getVersion();
	}

	@Override
	public @NotNull String getPrefixedVersion() {
		return "V" + getVersion();
	}

	@Override
	public @NotNull List<String> getAuthors() {
		return metadata.getMetadata().getAuthors();
	}

	@Override
	public @NotNull List<String> getContributors() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull List<String> getCredits() {
		return Collections.emptyList();
	}

	@Override
	public @NotNull Set<Badge> getBadges() {
		return Collections.emptySet();
	}

	@Override
	public @Nullable String getWebsite() {
		return null;
	}

	@Override
	public @Nullable String getIssueTracker() {
		return null;
	}

	@Override
	public @Nullable String getSource() {
		return null;
	}

	@Override
	public @Nullable String getParent() {
		return null;
	}

	@Override
	public @NotNull Set<String> getLicense() {
		return Collections.singleton(metadata.getMetadata().getLicense());
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return new HashMap<>();
	}

	@Override
	public boolean isReal() {
		return !metadata.getMetadata().isBuiltIn();
	}

	@Override
	public @Nullable ModrinthData getModrinthData() {
		return null;
	}

	@Override
	public boolean allowsUpdateChecks() {
		return false;
	}

	@Override
	public @Nullable String getSha512Hash() throws IOException {
		return Mod.super.getSha512Hash();
	}

	@Override
	public void setModrinthData(ModrinthData modrinthData) {

	}

	@Override
	public void setChildHasUpdate() {

	}

	@Override
	public boolean getChildHasUpdate() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}
}
