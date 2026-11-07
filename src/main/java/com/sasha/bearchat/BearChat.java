package com.sasha.bearchat;

import com.sasha.bearchat.commands.*;
import com.sasha.bearchat.listeners.ChatListener;
import com.sasha.bearchat.managers.ChatManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class BearChat extends JavaPlugin {

    private static BearChat instance;
    private ChatManager chatManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().severe("PlaceholderAPI not found! BearChat requires it.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        chatManager = new ChatManager(this);
        // Initialize the mail.yml file!
        chatManager.setupMailFile(this);

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register Commands
        getCommand("msg").setExecutor(new MsgCommand(this));
        getCommand("reply").setExecutor(new MsgCommand(this));
        getCommand("ignore").setExecutor(new IgnoreCommand(this));
        getCommand("ignorelist").setExecutor(new IgnoreCommand(this));
        getCommand("channel").setExecutor(new ChannelCommand(this));
        getCommand("staffchat").setExecutor(new ChannelCommand(this));
        getCommand("chatadmin").setExecutor(new ChatAdminCommand(this));
        getCommand("clearchat").setExecutor(new ChatAdminCommand(this));

        // NEW: Register Mail & SocialSpy commands
        getCommand("mail").setExecutor(new MailCommand(this));
        getCommand("socialspy").setExecutor(new SocialSpyCommand(this));

        // Register Tab Completion
        BearChatTabCompleter tabCompleter = new BearChatTabCompleter();
        getCommand("msg").setTabCompleter(tabCompleter);
        getCommand("reply").setTabCompleter(tabCompleter);
        getCommand("ignore").setTabCompleter(tabCompleter);
        getCommand("ignorelist").setTabCompleter(tabCompleter);
        getCommand("channel").setTabCompleter(tabCompleter);
        getCommand("staffchat").setTabCompleter(tabCompleter);
        getCommand("chatadmin").setTabCompleter(tabCompleter);
        getCommand("mail").setTabCompleter(tabCompleter);

        getLogger().info("BearChat enabled! Networking and Mail systems online.");
    }

    public static BearChat getInstance() { return instance; }
    public ChatManager getChatManager() { return chatManager; }

    public String getMsg(String path) {
        String prefix = getConfig().getString("messages.prefix", "&8[&bBearChat&8] ");
        String msg = getConfig().getString("messages." + path, "&cMessage missing: " + path);
        return ChatColor.translateAlternateColorCodes('&', prefix + msg);
    }

    // ADDED: The getRawMsg method to fix your error!
    public String getRawMsg(String path) {
        String msg = getConfig().getString("messages." + path, "&cMessage missing: " + path);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}