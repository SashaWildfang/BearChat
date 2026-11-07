package com.sasha.bearchat.commands;

import com.sasha.bearchat.BearChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ChannelCommand implements CommandExecutor {

    private final BearChat plugin;

    public ChannelCommand(BearChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMsg("not-player"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("staffchat")) {
            if (!player.hasPermission("bn.channel.staff")) {
                player.sendMessage(plugin.getMsg("no-permission"));
                return true;
            }

            if (args.length == 0) {
                // Toggle mode
                String current = plugin.getChatManager().getChannel(player.getUniqueId());
                if (current.equals("staff")) {
                    plugin.getChatManager().setChannel(player.getUniqueId(), "public");
                    player.sendMessage(plugin.getMsg("channel-join").replace("%channel%", "public"));
                } else {
                    plugin.getChatManager().setChannel(player.getUniqueId(), "staff");
                    player.sendMessage(plugin.getMsg("channel-join").replace("%channel%", "staff"));
                }
            } else {
                // Quick message mode (e.g. /sc hello everyone)
                String message = String.join(" ", args);
                String format = plugin.getConfig().getString("formats.staff", "&8[&cStaff&8] &7%player% &8» &c%message%")
                        .replace("%player%", player.getName())
                        .replace("%message%", message);
                format = org.bukkit.ChatColor.translateAlternateColorCodes('&', format);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("bn.channel.staff")) {
                        p.sendMessage(format);
                    }
                }
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("channel") || command.getName().equalsIgnoreCase("chat")) {
            if (args.length != 1) {
                player.sendMessage("§cUsage: /channel <public|trading|donor|staff>");
                return true;
            }

            String targetChannel = args[0].toLowerCase();

            // Default channels don't need perms, but specific ones do
            if (!targetChannel.equals("public") && !targetChannel.equals("trading")) {
                if (!player.hasPermission("bn.channel." + targetChannel)) {
                    player.sendMessage(plugin.getMsg("channel-denied").replace("%channel%", targetChannel));
                    return true;
                }
            }

            plugin.getChatManager().setChannel(player.getUniqueId(), targetChannel);
            player.sendMessage(plugin.getMsg("channel-join").replace("%channel%", targetChannel));
            return true;
        }

        return true;
    }
}