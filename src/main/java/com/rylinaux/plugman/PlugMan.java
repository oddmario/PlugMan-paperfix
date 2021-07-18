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

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.rylinaux.plugman.messaging.MessageFormatter;
import com.rylinaux.plugman.util.BukkitCommandWrap;
import com.rylinaux.plugman.util.BukkitCommandWrap_Useless;
import com.rylinaux.plugman.util.PluginUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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

    /**
     * Stores all file names + hashes for auto (re/un)load
     */
    private final HashMap<String, String> fileHashMap = new HashMap<>();

    /**
     * Stores all file names + plugin names for auto unload
     */
    private final HashMap<String, String> filePluginMap = new HashMap<>();

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

        /*if (version.contains("1.17") || version.contains("1.16") || version.contains("1.15") || version.contains("1.14") || version.contains("1.13")) {
            bukkitCommandWrap = new BukkitCommandWrap();
        } else {
            bukkitCommandWrap = new BukkitCommandWrap_Useless();
        }*/
        try {
            Class.forName("com.mojang.brigadier.CommandDispatcher");
            bukkitCommandWrap = new BukkitCommandWrap();
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            bukkitCommandWrap = new BukkitCommandWrap_Useless();
        }

        for (File file : new File("plugins").listFiles()) {
            if (file.isDirectory()) continue;
            if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) continue;
            String hash = null;
            try {
                hash = Files.asByteSource(file).hash(Hashing.md5()).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileHashMap.put(file.getName(), hash);

            JarFile jarFile = null;
            try {
                jarFile = new JarFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            if (jarFile.getEntry("plugin.yml") == null) continue;

            InputStream stream;
            try {
                stream = jarFile.getInputStream(jarFile.getEntry("plugin.yml"));
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            if (stream == null) continue;

            PluginDescriptionFile descriptionFile = null;
            try {
                descriptionFile = new PluginDescriptionFile(stream);
            } catch (InvalidDescriptionException e) {
                e.printStackTrace();
                continue;
            }

            filePluginMap.put(file.getName(), descriptionFile.getName());
        }

        boolean alerted = false;

        if (getConfig().getBoolean("auto-load.enabled", false)) {
            Bukkit.getLogger().warning("!!! The auto (re/un)load feature can break plugins, use with caution !!!");
            Bukkit.getLogger().warning("If anything breaks, a restart will probably fix it!");
            alerted = true;
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
                if (!new File("plugins").isDirectory()) {
                    return;
                }
                for (File file : Arrays.stream(new File("plugins").listFiles()).filter(File::isFile).filter(file -> file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")).collect(Collectors.toList())) {
                    if (!fileHashMap.containsKey(file.getName())) {
                        Bukkit.getScheduler().runTask(this, () -> {
                            Bukkit.getConsoleSender().sendMessage(PluginUtil.load(file.getName().replace(".jar", "")));
                        });
                        String hash = null;
                        try {
                            hash = Files.asByteSource(file).hash(Hashing.md5()).toString();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fileHashMap.put(file.getName(), hash);
                    }
                }
            }, getConfig().getLong("auto-load.check-every-seconds") * 20, getConfig().getLong("auto-load.check-every-seconds") * 20);
        }

        if (getConfig().getBoolean("auto-unload.enabled", false)) {
            if (!alerted) {
                Bukkit.getLogger().warning("!!! The auto (re/un)load feature can break plugins, use with caution !!!");
                Bukkit.getLogger().warning("If anything breaks, a restart will probably fix it!");
                alerted = true;
            }
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
                if (!new File("plugins").isDirectory()) {
                    return;
                }
                for (String fileName : fileHashMap.keySet()) {
                    if (!new File("plugins", fileName).exists()) {
                        Plugin plugin = Bukkit.getPluginManager().getPlugin(filePluginMap.get(fileName));
                        if (plugin == null) {
                            fileHashMap.remove(fileName);
                            filePluginMap.remove(fileName);
                            continue;
                        }
                        if (PluginUtil.isIgnored(plugin)) continue;
                        fileHashMap.remove(fileName);
                        Bukkit.getScheduler().runTask(this, () -> {
                            Bukkit.getConsoleSender().sendMessage(PluginUtil.unload(plugin));
                        });
                    }
                }
            }, getConfig().getLong("auto-unload.check-every-seconds") * 20, getConfig().getLong("auto-unload.check-every-seconds") * 20);
        }

        if (getConfig().getBoolean("auto-reload.enabled", false)) {
            if (!alerted) {
                Bukkit.getLogger().warning("!!! The auto (re/un)load feature can break plugins, use with caution !!!");
                Bukkit.getLogger().warning("If anything breaks, a restart will probably fix it!");
                alerted = true;
            }
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
                if (!new File("plugins").isDirectory()) {
                    return;
                }
                for (File file : Arrays.stream(new File("plugins").listFiles()).filter(File::isFile).filter(file -> file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")).collect(Collectors.toList())) {
                    if (!fileHashMap.containsKey(file.getName())) {
                        continue;
                    }
                    String hash = null;
                    try {
                        hash = Files.asByteSource(file).hash(Hashing.md5()).toString();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }

                    if (!hash.equalsIgnoreCase(fileHashMap.get(file.getName()))) {
                        Plugin plugin = Bukkit.getPluginManager().getPlugin(filePluginMap.get(file.getName()));
                        if (plugin == null) {
                            fileHashMap.remove(file.getName());
                            filePluginMap.remove(file.getName());
                            continue;
                        }

                        if (PluginUtil.isIgnored(plugin)) continue;

                        fileHashMap.remove(file.getName());
                        fileHashMap.put(file.getName(), hash);

                        Bukkit.getScheduler().runTask(this, () -> {
                            Bukkit.getConsoleSender().sendMessage(PluginUtil.unload(plugin));
                            Bukkit.getConsoleSender().sendMessage(PluginUtil.load(plugin.getName()));
                        });
                    }
                }
            }, getConfig().getLong("auto-reload.check-every-seconds") * 20, getConfig().getLong("auto-reload.check-every-seconds") * 20);
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

        if (!getConfig().isSet("auto-load.enabled") || !getConfig().isSet("auto-unload.enabled") || !getConfig().isSet("auto-reload.enabled") || !getConfig().isSet("ignored-plugins")) {
            Bukkit.getLogger().severe("Invalid PlugMan config detected! Creating new one...");
            new File("plugins" + File.separator + "PlugMan", "config.yml").renameTo(new File("plugins" + File.separator + "PlugMan", "config.yml.old-" + System.currentTimeMillis()));
            saveDefaultConfig();
            Bukkit.getLogger().info("New config created!");
        }

        ignoredPlugins = this.getConfig().getStringList("ignored-plugins");

        File resourcemapFile = new File(getDataFolder(), "resourcemaps.yml");
        if (!resourcemapFile.exists()) {
            saveResource("resourcemaps.yml", true);
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(resourcemapFile);
        resourceMap.clear();
        for (String name : cfg.getConfigurationSection("Resources").getKeys(false)) {
            if (name.equalsIgnoreCase("PlugMan")) continue;
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

        resourceMap.put("plugman", new Map.Entry<Long, Boolean>() {
            @Override
            public Long getKey() {
                return 88135L;
            }

            @Override
            public Boolean getValue() {
                return true;
            }

            @Override
            public Boolean setValue(Boolean value) {
                return true;
            }
        });
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

    public HashMap<String, String> getFilePluginMap() {
        return filePluginMap;
    }
}
