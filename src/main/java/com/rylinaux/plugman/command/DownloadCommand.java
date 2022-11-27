package com.rylinaux.plugman.command;

import com.rylinaux.plugman.PlugMan;
import com.rylinaux.plugman.util.PluginUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DownloadCommand extends AbstractCommand {

    /**
     * The name of the command.
     */
    public static final String NAME = "Download";

    /**
     * The description of the command.
     */
    public static final String DESCRIPTION = "Download a plugin.";

    /**
     * The main permission of the command.
     */
    public static final String PERMISSION = "plugman.download";

    /**
     * The proper usage of the command.
     */
    public static final String USAGE = "/plugman download <URL>";

    /**
     * The sub permissions of the command.
     */
    public static final String[] SUB_PERMISSIONS = {};

    /**
     * Construct out object.
     *
     * @param sender the command sender
     */
    public DownloadCommand(CommandSender sender) {
        super(sender, NAME, DESCRIPTION, PERMISSION, SUB_PERMISSIONS, USAGE);
    }

    /**
     * Execute the command
     *
     * @param sender  the sender of the command
     * @param command the command being done
     * @param label   the name of the command
     * @param args    the arguments supplied
     */
    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {

        if (!hasPermission()) {
            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.no-permission"));
            return;
        }

        if (args.length < 2 || !args[1].startsWith("http://") && !args[1].startsWith("https://")) {
            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.invalid-url"));
            sendUsage();
            return;
        }

        File file;
        try {
            URL url = new URL(args[1]);
            file = PluginUtil.download(url);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("download.download-failed"));
            return;
        }

        String name = file.getName();
        name = name.substring(0, name.length() - 4);
        sender.sendMessage(PluginUtil.load(name));
    }

}
