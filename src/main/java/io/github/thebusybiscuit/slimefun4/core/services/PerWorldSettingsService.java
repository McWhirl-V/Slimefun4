package io.github.thebusybiscuit.slimefun4.core.services;

import io.github.thebusybiscuit.cscorelib2.collections.OptionalMap;
import io.github.thebusybiscuit.cscorelib2.config.Config;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

/**
 * This Service is responsible for disabling a {@link SlimefunItem} in a certain {@link World}.
 *
 * @author TheBusyBiscuit
 */
public class PerWorldSettingsService {

    private final SlimefunPlugin plugin;

    private final OptionalMap<String, Set<String>> disabledItems = new OptionalMap<>(HashMap::new);
    private final Map<SlimefunAddon, Set<String>> disabledAddons = new HashMap<>();
    private final Set<String> disabledWorlds = new HashSet<>();

    public PerWorldSettingsService(SlimefunPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * This method will forcefully load all currently active Worlds to load up their settings.
     */
    public void load(Iterable<World> worlds) {
        try {
            migrate();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "An error occured while migrating old world settings", e);
        }

        for (World world : worlds) {
            load(world);
        }
    }

    /**
     * This method loads the given {@link World} if it was not loaded before.
     *
     * @param world
     *            The {@link World} to load
     */
    public void load(World world) {
        disabledItems.putIfAbsent(world.getName(), loadWorld(world.getName()));
    }

    /**
     * Temporary migration method for the old system
     *
     * @throws IOException
     *             This will be thrown if we failed to delete the old {@link File}
     */
    private void migrate() throws IOException {
        Config oldConfig = new Config(plugin, "whitelist.yml");

        if (oldConfig.getFile().exists()) {
            for (String world : oldConfig.getKeys()) {
                Config newConfig = new Config(plugin, "world-settings/" + world + ".yml");
                newConfig.setDefaultValue("enabled", oldConfig.getBoolean(world + ".enabled"));

                for (String id : oldConfig.getKeys(world + ".enabled-items")) {
                    SlimefunItem item = SlimefunItem.getByID(id);

                    if (item != null) {
                        String addon = item.getAddon().getName().toLowerCase(Locale.ROOT);
                        newConfig.setDefaultValue(addon + ".enabled", true);
                        newConfig.setDefaultValue(addon + '.' + id, oldConfig.getBoolean(world + ".enabled-items." + id));
                    }
                }

                newConfig.save();
            }

            Files.delete(oldConfig.getFile().toPath());
        }
    }

    /**
     * This method checks whether the given {@link SlimefunItem} is enabled in the given {@link World}.
     *
     * @param world
     *            The {@link World} to check
     * @param item
     *            The {@link SlimefunItem} that should be checked
     *
     * @return Whether the given {@link SlimefunItem} is enabled in that {@link World}
     */
    public boolean isEnabled(World world, SlimefunItem item) {
        Set<String> items = disabledItems.computeIfAbsent(world.getName(), this::loadWorld);

        if (disabledWorlds.contains(world.getName())) {
            return false;
        }

        return !items.contains(item.getID());
    }

    /**
     * This checks whether the given {@link World} is enabled or not.
     *
     * @param world
     *            The {@link World} to check
     *
     * @return Whether this {@link World} is enabled
     */
    public boolean isWorldEnabled(World world) {
        loadWorld(world.getName());
        return !disabledWorlds.contains(world.getName());
    }

    /**
     * This method checks whether the given {@link SlimefunAddon} is enabled in that {@link World}.
     *
     * @param world
     *            The {@link World} to check
     * @param addon
     *            The {@link SlimefunAddon} to check
     *
     * @return Whether this addon is enabled in that {@link World}
     */
    public boolean isAddonEnabled(World world, SlimefunAddon addon) {
        return isWorldEnabled(world) && disabledAddons.getOrDefault(addon, Collections.emptySet()).contains(world.getName());
    }

    private Set<String> loadWorld(String name) {
        Optional<Set<String>> optional = disabledItems.get(name);

        if (optional.isPresent()) {
            return optional.get();
        } else {
            Set<String> items = new LinkedHashSet<>();
            Config config = new Config(plugin, "world-settings/" + name + ".yml");

            config.getConfiguration().options().header("This file is used to disable certain items in a particular world.\nYou can set any item to 'false' to disable it in the world '" + name + "'.\nYou can also disable an entire addon from Slimefun by setting the respective\nvalue of 'enabled' for that Addon.\n\nItems which are disabled in this world will not show up in the Slimefun Guide.\nYou won't be able to use these items either. Using them will result in a warning message.");
            config.getConfiguration().options().copyHeader(true);
            config.setDefaultValue("enabled", true);

            if (config.getBoolean("enabled")) {
                for (SlimefunItem item : SlimefunPlugin.getRegistry().getEnabledSlimefunItems()) {
                    if (item != null && item.getID() != null) {
                        String addon = item.getAddon().getName().toLowerCase(Locale.ROOT);
                        config.setDefaultValue(addon + ".enabled", true);
                        config.setDefaultValue(addon + '.' + item.getID(), true);

                        boolean isAddonDisabled = config.getBoolean(addon + ".enabled");

                        if (isAddonDisabled) {
                            Set<String> blacklist = disabledAddons.computeIfAbsent(plugin, key -> new HashSet<>());
                            blacklist.add(name);
                        }

                        if (!isAddonDisabled || !config.getBoolean(addon + '.' + item.getID())) {
                            items.add(item.getID());
                        }
                    }
                }

                config.save();
            } else {
                disabledWorlds.add(name);
            }

            return items;
        }
    }

}
