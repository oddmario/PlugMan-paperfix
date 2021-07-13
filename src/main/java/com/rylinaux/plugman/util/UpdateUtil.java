package com.rylinaux.plugman.util;

import com.rylinaux.plugman.PlugMan;
import com.rylinaux.plugman.pojo.UpdateResult;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class UpdateUtil {

    /**
     * The base URL for the SpiGet API.
     */
    public static final String API_BASE_URL = "https://api.spiget.org/v2/";

    /**
     * Check which plugins are up-to-date or not.
     *
     * @return a map of the plugins and the results.
     */
    public static Map<String, UpdateResult> checkUpToDate() {
        Map<String, UpdateResult> results = new TreeMap<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            results.put(plugin.getName(), checkUpToDate(plugin.getName()));
        }
        return results;
    }

    /**
     * Check if the installed plugin version is up-to-date with the Spigot version.
     *
     * @param pluginName the plugin name.
     * @return the reflective UpdateResult.
     */
    public static UpdateResult checkUpToDate(String pluginName) {
        if (PlugMan.getInstance().getResourceMap().containsKey(pluginName.toLowerCase(Locale.ROOT))) {
            Map.Entry<Long, Boolean> entry = PlugMan.getInstance().getResourceMap().get(pluginName.toLowerCase(Locale.ROOT));

            if (entry.getValue()) {
                return SpiGetUtil.checkUpToDate(pluginName, entry.getKey());
            } else {
                return CurseForgeUtil.checkUpToDate(pluginName, entry.getKey());
            }
        }

        long id = SpiGetUtil.getPluginId(pluginName);
        if (id < 0) {
            id = CurseForgeUtil.getPluginId(pluginName);
            if (id < 0) {
                Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (plugin == null) {
                    return new UpdateResult(UpdateResult.ResultType.INVALID_PLUGIN, pluginName);
                }
                return new UpdateResult(UpdateResult.ResultType.INVALID_PLUGIN, plugin.getDescription().getVersion());
            }
            return CurseForgeUtil.checkUpToDate(pluginName);
        }
        return SpiGetUtil.checkUpToDate(pluginName);
    }

    /**
     * Get the id of the plugin.
     *
     * @param name the name of the plugin.
     * @return the id of the plugin.
     */
    public static long getPluginId(String name) {
        if (PlugMan.getInstance().getResourceMap().containsKey(name.toLowerCase(Locale.ROOT))) {
            Map.Entry<Long, Boolean> entry = PlugMan.getInstance().getResourceMap().get(name.toLowerCase(Locale.ROOT));

            if (entry.getValue()) {
                return SpiGetUtil.getPluginId(name);
            } else {
                return CurseForgeUtil.getPluginId(name);
            }
        }

        long id = SpiGetUtil.getPluginId(name);
        if (id < 0) {
            id = CurseForgeUtil.getPluginId(name);
        }
        return id;

    }

    /**
     * Get the versions for a given plugin.
     *
     * @param id the plugin id.
     * @return the JSON encoded data.
     */
    public static JSONArray getPluginVersions(long id) {
        for (Map.Entry<Long, Boolean> entry : PlugMan.getInstance().getResourceMap().values()) {
            if (entry.getKey() == id) {
                if (entry.getValue()) {
                    return SpiGetUtil.getPluginVersions(id);
                } else {
                    return CurseForgeUtil.getPluginVersions(id);
                }
            }
        }

        JSONArray jsonArray = SpiGetUtil.getPluginVersions(id);
        if (jsonArray == null || jsonArray.size() <= 0) {
            return CurseForgeUtil.getPluginVersions(id);
        }
        return jsonArray;
    }

}
