package com.rylinaux.plugman.util;

import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BukkitCommandWrap {
    private String nmsVersion;
    private Class minecraftServerClass;
    private Method aMethod;
    private Method getServerMethod;
    private Method getCommandDispatcherMethod;
    private Method registerMethod;
    private Constructor bukkitcommandWrapperConstructor;

    public BukkitCommandWrap() {
        try {
            this.nmsVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            this.nmsVersion = null;
        }
    }

    public void wrap(Command command, String alias) {
        if (this.nmsVersion == null) return;
        if (this.minecraftServerClass == null) try {
            this.minecraftServerClass = Class.forName("net.minecraft.server." + this.nmsVersion + ".MinecraftServer");
        } catch (ClassNotFoundException e) {
            try {
                this.minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.addSuppressed(e);
                classNotFoundException.printStackTrace();
                return;
            }
        }

        if (this.getServerMethod == null) try {
            this.getServerMethod = this.minecraftServerClass.getMethod("getServer");
            this.getServerMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        Object minecraftServer;
        try {
            minecraftServer = this.getServerMethod.invoke(this.minecraftServerClass);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        if (this.getCommandDispatcherMethod == null) try {
            this.getCommandDispatcherMethod = this.minecraftServerClass.getDeclaredMethod("getCommandDispatcher");
            this.getCommandDispatcherMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        Object commandDispatcher;
        try {
            commandDispatcher = this.getCommandDispatcherMethod.invoke(minecraftServer);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        if (this.aMethod == null) try {
            this.aMethod = commandDispatcher.getClass().getDeclaredMethod("a");
            this.aMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        if (this.bukkitcommandWrapperConstructor == null) try {
            this.bukkitcommandWrapperConstructor = Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".command.BukkitCommandWrapper").getDeclaredConstructor(Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".CraftServer"), Command.class);
            this.bukkitcommandWrapperConstructor.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Object commandWrapper;

        try {
            commandWrapper = this.bukkitcommandWrapperConstructor.newInstance(Bukkit.getServer(), command);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        Object a;

        try {
            a = this.aMethod.invoke(commandDispatcher);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        if (this.registerMethod == null) try {
            this.registerMethod = Class.forName("org.bukkit.craftbukkit." + this.nmsVersion + ".command.BukkitCommandWrapper").getMethod("register", com.mojang.brigadier.CommandDispatcher.class, String.class);
            this.registerMethod.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            this.registerMethod.invoke(commandWrapper, a, alias);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    Field bField;
    Method removeCommandMethod;

    public void unwrap(String command) {
        if (this.nmsVersion == null) return;
        if (this.minecraftServerClass == null) try {
            this.minecraftServerClass = Class.forName("net.minecraft.server." + this.nmsVersion + ".MinecraftServer");
        } catch (ClassNotFoundException e) {
            try {
                this.minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
                classNotFoundException.addSuppressed(e);
                return;
            }
        }
        if (this.getServerMethod == null) try {
            this.getServerMethod = this.minecraftServerClass.getMethod("getServer");
            this.getServerMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        Object server;

        try {
            server = getServerMethod.invoke(minecraftServerClass);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        if (this.getCommandDispatcherMethod == null) try {
            this.getCommandDispatcherMethod = this.minecraftServerClass.getDeclaredMethod("getCommandDispatcher");
            this.getCommandDispatcherMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }

        Object commandDispatcher = null;
        try {
            commandDispatcher = getCommandDispatcherMethod.invoke(server);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        if (bField == null) {
            try {
                bField = Class.forName("net.minecraft.server." + nmsVersion + ".CommandDispatcher").getDeclaredField("b");
                bField.setAccessible(true);
            } catch (NoSuchFieldException | ClassNotFoundException e) {
                if (this.bField == null) try {
                    this.bField = Class.forName("net.minecraft.commands.CommandDispatcher").getDeclaredField("g");
                    this.bField.setAccessible(true);
                } catch (NoSuchFieldException | ClassNotFoundException ex) {
                    ex.addSuppressed(e);
                    e.printStackTrace();
                    return;
                }
            }
        }

        com.mojang.brigadier.CommandDispatcher b;
        try {
            b = (com.mojang.brigadier.CommandDispatcher) bField.get(commandDispatcher);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (removeCommandMethod == null) {
            try {
                removeCommandMethod = RootCommandNode.class.getDeclaredMethod("removeCommand", String.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            removeCommandMethod.invoke(b.getRoot(), command);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
