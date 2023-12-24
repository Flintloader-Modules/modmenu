package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.util.ModrinthUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.flintloader.loader.api.FlintModule;
import net.flintloader.loader.api.FlintModuleContainer;
import net.flintloader.loader.modules.FlintModuleMetadata;
import net.flintloader.loader.modules.ModuleList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;

public class ModMenu implements FlintModule {
	public static final String MOD_ID = "modmenu";
	public static final String GITHUB_REF = "TerraformersMC/ModMenu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
	public static final Gson GSON_MINIFIED = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();
	public static boolean modUpdateAvailable = false;

	private static Map<String, ConfigScreenFactory<?>> configScreenFactories = new HashMap<>();
	private static List<Map<String, ConfigScreenFactory<?>>> delayedScreenFactoryProviders = new ArrayList<>();

	private static int cachedDisplayedModCount = -1;
	public static boolean runningQuilt = false;

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		if(!delayedScreenFactoryProviders.isEmpty()) {
			delayedScreenFactoryProviders.forEach(map -> map.forEach(configScreenFactories::putIfAbsent));
			delayedScreenFactoryProviders.clear();
		}
		if (ModMenuConfig.HIDDEN_CONFIGS.getValue().contains(modid)) {
			return null;
		}
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.create(menuScreen);
		}
		return null;
	}

	@Override
	public void initializeModule() {
		ModMenuConfigManager.initializeConfig();
		Set<String> modpackMods = new HashSet<>();
		/*FlintLoader.getEntryPointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			FlintModuleMetadata metadata = entrypoint.getProvider();
			String modId = metadata.getId();
			try {
				ModMenuApi api = (ModMenuApi) entrypoint.getEntryPoint();
				configScreenFactories.put(modId, api.getModConfigScreenFactory());
				delayedScreenFactoryProviders.add(api.getProvidedConfigScreenFactories());
				api.attachModpackBadges(modpackMods::add);
			} catch (Throwable e) {
				LOGGER.error("Mod {} provides a broken implementation of ModMenuApi", modId, e);
			}
		});*/

		// Fill mods map
		for (FlintModuleContainer container : ModuleList.getInstance().allModules()) {
			FlintModuleMetadata modContainer = container.getMetadata();
			if (!ModMenuConfig.HIDDEN_MODS.getValue().contains(modContainer.getId())) {
				com.terraformersmc.modmenu.util.mod.flint.FlintModule mod = new com.terraformersmc.modmenu.util.mod.flint.FlintModule(container);
				MODS.put(mod.getId(), mod);
			}
		}

		if (ModMenuConfig.UPDATE_CHECKER.getValue()) {
			ModrinthUtil.checkForUpdates();
		}

		ModMenuEventHandler.register();
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod ->
					(ModMenuConfig.COUNT_CHILDREN.getValue() || mod.getParent() == null) &&
							(ModMenuConfig.COUNT_LIBRARIES.getValue() || !mod.getBadges().contains(Mod.Badge.LIBRARY)) &&
							(ModMenuConfig.COUNT_HIDDEN_MODS.getValue() || !ModMenuConfig.HIDDEN_MODS.getValue().contains(mod.getId()))
			).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static Component createModsButtonText(boolean title) {
		var titleStyle = ModMenuConfig.MODS_BUTTON_STYLE.getValue();
		var gameMenuStyle = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
		var isIcon = title ? titleStyle == ModMenuConfig.TitleMenuButtonStyle.ICON : gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;
		var isShort = title ? titleStyle == ModMenuConfig.TitleMenuButtonStyle.SHRINK : gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.REPLACE_BUGS;
		MutableComponent modsText = Component.translatable("modmenu.title");
		if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && !isIcon) {
			String count = ModMenu.getDisplayedModCount();
			if (isShort) {
				modsText.append(Component.literal(" ")).append(Component.translatable("modmenu.loaded.short", count));
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = I18n.exists(specificKey) ? specificKey : "modmenu.loaded";
				if (ModMenuConfig.EASTER_EGGS.getValue() && I18n.exists(specificKey + ".secret")) {
					key = specificKey + ".secret";
				}
				modsText.append(Component.literal(" ")).append(Component.translatable(key, count));
			}
		}
		return modsText;
	}
}
