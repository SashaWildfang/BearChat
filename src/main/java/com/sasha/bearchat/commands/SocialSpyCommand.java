package com.sasha.bearchat.commands;

import com.sasha.bearchat.BearChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SocialSpyCommand implements CommandExecutor {

    private final BearChat plugin;

    public SocialSpyCommand(BearChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (!player.hasPermission("bn.bearchat.socialspy")) {
            player.sendMessage(plugin.getRawMsg("no-permission").replace("%node%", "bn.bearchat.socialspy"));
            return true;
        }

        plugin.getChatManager().toggleSocialSpy(player);
        return true;
    }
}