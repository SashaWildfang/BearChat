package com.sasha.bearchat.commands;

import com.sasha.bearchat.BearChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ChatAdminCommand implements CommandExecutor {

    private final BearChat plugin;

    public ChatAdminCommand(BearChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bn.bearchat.admin")) {
            sender.sendMessage(plugin.getMsg("no-permission"));
            return true;
        }

        String action = label.toLowerCase();

        // Handle Aliases directly acting as the action
        if (action.equals("lockchat")) {
            lockChat();
            return true;
        } else if (action.equals("unlockchat")) {
            unlockChat();
            return true;
        } else if (action.equals("clearchat") || action.equals("cc")) {
            action = "clear";
        } else if (args.length == 0) {
            sender.sendMessage("§cUsage: /chatadmin <clear|lock|unlock|slowmode|status>");
            return true;
        } else {
            action = args[0].toLowerCase();
        }

        switch (action) {
            case "clear":
                int lines = plugin.getConfig().getInt("settings.clear-lines", 50);
                for (int i = 0; i < lines; i++) {
                    Bukkit.broadcastMessage("");
                }
                Bukkit.broadcastMessage("§aChat has been cleared by " + sender.getName() + ".");
                break;

            case "lock":
                lockChat();
                break;

            case "unlock":
                unlockChat();
                break;

            case "slowmode":
                if (args.length == 1) {
                    plugin.getChatManager().adminSlowmodeSeconds = 10;
                    Bukkit.broadcastMessage(plugin.getMsg("slowmode-set").replace("%time%", "10"));
                } else {
                    String timeStr = args[1].toLowerCase().replace("s", "");
                    try {
                        int seconds = Integer.parseInt(timeStr);
                        if (seconds <= 0) {
                            plugin.getChatManager().adminSlowmodeSeconds = 0;
                            Bukkit.broadcastMessage(plugin.getMsg("slowmode-disabled"));
                        } else {
                            plugin.getChatManager().adminSlowmodeSeconds = seconds;
                            Bukkit.broadcastMessage(plugin.getMsg("slowmode-set").replace("%time%", String.valueOf(seconds)));
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid time format. Use a number (e.g., 5 or 10s).");
                    }
                }
                break;

            case "status":
                sender.sendMessage("§e---- Chat Status ----");
                sender.sendMessage("§7Locked: " + (plugin.getChatManager().isChatLocked ? "§cYes" : "§aNo"));

                int adminSlow = plugin.getChatManager().adminSlowmodeSeconds;
                if (adminSlow > 0) {
                    sender.sendMessage("§7Slowmode: §cActive (" + adminSlow + "s)");
                } else {
                    int defaultSlow = plugin.getConfig().getInt("settings.default-cooldown", 3);
                    sender.sendMessage("§7Slowmode: §aDisabled §8(Default: " + defaultSlow + "s)");
                }
                break;

            default:
                sender.sendMessage("§cUsage: /chatadmin <clear|lock|unlock|slowmode|status>");
                break;
        }

        return true;
    }

    private void lockChat() {
        plugin.getChatManager().isChatLocked = true;
        Bukkit.broadcastMessage(plugin.getMsg("chat-locked-broadcast"));
    }

    private void unlockChat() {
        plugin.getChatManager().isChatLocked = false;
        Bukkit.broadcastMessage(plugin.getMsg("chat-unlocked-broadcast"));
    }
}