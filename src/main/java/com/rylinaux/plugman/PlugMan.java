package com.rylinaux.plugman;

/*
 * #%L
 * PlugMan
 * %%
 * Copyright (C) 2010 - 2014 PlugMan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.rylinaux.plugman.messaging.MessageFormatter;
import com.rylinaux.plugman.util.BukkitCommandWrap;
import com.rylinaux.plugman.util.BukkitCommandWrap_Useless;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Plugin manager for Bukkit servers.
 *
 * @author rylinaux
 */
public class PlugMan extends JavaPlugin {
    /**
     * HashMap that contains all mappings from resourcemaps.yml
     */
    private final HashMap<String, Map.Entry<Long, Boolean>> resourceMap = new HashMap<>();

    /**
     * The command manager which adds all command we want so 1.13+ players can instantly tab-complete them
     */
    private BukkitCommandWrap bukkitCommandWrap = null;

    /**
     * The instance of the plugin
     */
    private static PlugMan instance = null;

    /**
     * List of plugins to ignore, partially.
     */
    private List<String> ignoredPlugins = null;

    /**
     * The message manager
     */
    private MessageFormatter messageFormatter = null;

    @Override
    public void onEnable() {

        instance = this;

        File messagesFile = new File("plugins" + File.separator + "PlugMan", "messages.yml");

        if (!messagesFile.exists()) {
            saveResource("messages.yml", true);
        }

        messageFormatter = new MessageFormatter();

        this.getCommand("plugman").setExecutor(new PlugManCommandHandler());
        this.getCommand("plugman").setTabCompleter(new PlugManTabCompleter());

        initConfig();

        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].replace("_", ".");
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }

        if (version.contains("1.17") || version.contains("1.16") || version.contains("1.15") || version.contains("1.14") || version.contains("1.13")) {
            bukkitCommandWrap = new BukkitCommandWrap();
        } else {
            bukkitCommandWrap = new BukkitCommandWrap_Useless();
        }
    }

    @Override
    public void onDisable() {
        instance = null;
        messageFormatter = null;
        ignoredPlugins = null;
    }

    /**
     * Copy default config values
     */
    private void initConfig() {
        this.saveDefaultConfig();
        ignoredPlugins = this.getConfig().getStringList("ignored-plugins");

        File resourcemapFile = new File(getDataFolder(), "resourcemaps.yml");
        if (!resourcemapFile.exists()) {
            saveResource("resourcemaps.yml", true);
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(resourcemapFile);
        resourceMap.clear();
        for (String name : cfg.getConfigurationSection("Resources").getKeys(false)) {
            try {
                long id = cfg.getLong("Resources." + name + ".ID");
                boolean spigotmc = cfg.getBoolean("Resources." + name + ".spigotmc");
                resourceMap.put(name.toLowerCase(Locale.ROOT), new Map.Entry<Long, Boolean>() {
                    @Override
                    public Long getKey() {
                        return id;
                    }

                    @Override
                    public Boolean getValue() {
                        return spigotmc;
                    }

                    @Override
                    public Boolean setValue(Boolean value) {
                        return spigotmc;
                    }
                });
            } catch (Exception e) {
                getLogger().severe("An error occurred while trying to load mappings for '" + name + "'");
                e.printStackTrace();
            }

        }
    }

    /**
     * Returns the instance of the plugin.
     *
     * @return the instance of the plugin
     */
    public static PlugMan getInstance() {
        return instance;
    }

    /**
     * Returns the list of ignored plugins.
     *
     * @return the ignored plugins
     */
    public List<String> getIgnoredPlugins() {
        return ignoredPlugins;
    }

    /**
     * Returns the message manager.
     *
     * @return the message manager
     */
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    /**
     * Returns the command manager.
     *
     * @return the command manager
     */
    public BukkitCommandWrap getBukkitCommandWrap() {
        return bukkitCommandWrap;
    }

    public HashMap<String, Map.Entry<Long, Boolean>> getResourceMap() {
        return resourceMap;
    }
}
