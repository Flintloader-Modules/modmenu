package com.terraformersmc.modmenu.util.mod.fabric;

import com.mojang.blaze3d.platform.NativeImage;
import net.flintloader.loader.api.FlintModuleContainer;
import net.flintloader.loader.modules.FlintModuleMetadata;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FabricIconHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricIconHandler");

	private final Map<Path, DynamicTexture> modIconCache = new HashMap<>();

	public DynamicTexture createIcon(FlintModuleContainer iconSource, String iconPath) {
		try {
			Optional<Path> icn = iconSource.findPath(iconPath);
			if (icn.isEmpty())
				return null;

			Path path = icn.get();
			DynamicTexture cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				DynamicTexture tex = new DynamicTexture(image);
				cacheModIcon(path, tex);
				return tex;
			}

		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getMetadata().getId() + "/icon.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getMetadata().getId(), iconPath, t);
			}
			return null;
		}
	}

	@Override
	public void close() {
		for (DynamicTexture tex : modIconCache.values()) {
			tex.close();
		}
	}

	DynamicTexture getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, DynamicTexture tex) {
		modIconCache.put(path, tex);
	}
}
