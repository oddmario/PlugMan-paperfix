package me.entity303.plugmanbungee.commands.cmd;

import me.entity303.plugmanbungee.util.BungeePluginUtil;
import me.entity303.plugmanbungee.util.PluginResult;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.PluginDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LoadCommand {

    public void execute(CommandSender sender, String[] args) {
        if (args.length <= 0) {
            sendMessage(sender, "§cSyntax: §4/PlugManBungee unload <Datei>");
            return;
        }

        File file = new File("plugins", args[0] + ".jar");

        if (!file.exists()) {
            sendMessage(sender, "§cEs gibt keine Plugin-Datei mit dem Namen §4" + args[0] + "§c!");
            return;
        }

        PluginResult pluginResult = BungeePluginUtil.loadPlugin(file);

        sendMessage(sender, pluginResult.getMessage());




        /*PluginManager pluginManager = ProxyServer.getInstance().getPluginManager();

        if (pluginManager.getPlugin(args[0]) != null) {
            System.out.println("Plugin not null!");
            return;
        }

        Field yamlField = null;
        try {
            yamlField = PluginManager.class.getDeclaredField("yaml");
            yamlField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        Yaml yaml = null;
        try {
            yaml = (Yaml) yamlField.get(pluginManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        Field toLoadField = null;
        try {
            toLoadField = PluginManager.class.getDeclaredField("toLoad");
            toLoadField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        HashMap<String, PluginDescription> toLoad = null;
        try {
            toLoad = (HashMap<String, PluginDescription>) toLoadField.get(pluginManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (toLoad == null) {
            toLoad = new HashMap<>();
        }

        File file = new File("plugins", args[0] + ".jar");

        if (file.isFile()) {
            PluginDescription desc;

            try (JarFile jar = new JarFile(file)) {
                JarEntry pdf = jar.getJarEntry("bungee.yml");
                if (pdf == null) {
                    pdf = jar.getJarEntry("plugin.yml");
                }
                Preconditions.checkNotNull(pdf, "Plugin must have a plugin.yml or bungee.yml");

                try (InputStream in = jar.getInputStream(pdf)) {
                    desc = yaml.loadAs(in, PluginDescription.class);

                    Preconditions.checkNotNull(desc.getName(), "Plugin from %s has no name", file);
                    Preconditions.checkNotNull(desc.getMain(), "Plugin from %s has no main", file);

                    desc.setFile(file);

                    toLoad.put(desc.getName(), desc);
                }

                toLoadField.set(pluginManager, toLoad);

                pluginManager.loadPlugins();

                Plugin plugin = pluginManager.getPlugin(desc.getName());
                if (plugin == null)
                    return;
                plugin.onEnable();
            } catch (Exception ex) {
                ProxyServer.getInstance().getLogger().log(Level.WARNING, "Could not load plugin from file " + file, ex);
            }
        }*/
    }

    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(new TextComponent("§8[§2PlugManBungee§8] §7" + message));
    }

    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (File file : new File("plugins").listFiles()) {
                if (file.isFile()) {
                    if (file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                        Yaml yaml = new Yaml();
                        try (JarFile jar = new JarFile(file)) {
                            JarEntry pdf = jar.getJarEntry("bungee.yml");
                            if (pdf == null) {
                                pdf = jar.getJarEntry("plugin.yml");
                            }

                            if (pdf == null)
                                continue;

                            try (InputStream in = jar.getInputStream(pdf)) {
                                PluginDescription desc = yaml.loadAs(in, PluginDescription.class);

                                if (desc.getName() == null)
                                    continue;

                                if (desc.getMain() == null)
                                    continue;

                                if (ProxyServer.getInstance().getPluginManager().getPlugin(desc.getName()) != null)
                                    continue;

                                completions.add(file.getName().substring(0, file.getName().length() - 4));
                            }
                        } catch (IOException ignored) {
                        }
                    }
                }
            }

            List<String> realCompletions = new ArrayList<>();

            for (String com : completions) {
                if (com.toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    realCompletions.add(com);
                }
            }

            return realCompletions.size() > 0 ? realCompletions : completions;
        }
        return new ArrayList<>();
    }
}
