package me.entity303.plugmanbungee.main;

import me.entity303.plugmanbungee.commands.PlugManBungeeCommand;
import me.entity303.plugmanbungee.commands.PluginsCommand;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public final class PlugManBungee extends Plugin implements Listener {
    private static PlugManBungee instance;

    @Override
    public void onEnable() {
        instance = this;

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PluginsCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PlugManBungeeCommand());

        for (int i = 1; i < 4; i++) {
            ProxyServer.getInstance().getScheduler().schedule(this, () -> {
                getLogger().severe("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                getLogger().severe("This version of PlugMan is still in very *alpha*, please do not expect it to work perfectly!");
                getLogger().severe("Also do not expect for every feature to be implemented!");
                getLogger().severe("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }, i, TimeUnit.SECONDS);
        }
    }

    @Override
    public void onDisable() {
    }

    public static PlugManBungee getInstance() {
        return instance;
    }
}
