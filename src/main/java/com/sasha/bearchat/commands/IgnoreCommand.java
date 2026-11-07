package com.sasha.bearchat.commands;

import com.sasha.bearchat.BearChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IgnoreCommand implements CommandExecutor {

    private final BearChat plugin;

    public IgnoreCommand(BearChat plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMsg("not-player"));
            return true;
        }

        if (command.getName().equalsIgnoreCase("ignore")) {
            if (args.length != 1) {
                player.sendMessage("§cUsage: /ignore <player>");
                return true;
            }
            handleIgnoreToggle(player, args[0]);
            return true;
        }

        if (command.getName().equalsIgnoreCase("ignorelist")) {
            if (args.length == 0 || args[0].matches("\\d+")) {
                int page = args.length == 1 ? Integer.parseInt(args[0]) : 1;
                showIgnoreList(player, page);
                return true;
            }

            if (args.length == 2) {
                String subCmd = args[0].toLowerCase();
                if (subCmd.equals("add") || subCmd.equals("remove")) {
                    handleIgnoreToggle(player, args[1]); // Toggle handles both adding and removing
                    return true;
                }
            }
            player.sendMessage("§cUsage: /ignorelist [page] OR /ignorelist <add|remove> <player>");
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private void handleIgnoreToggle(Player player, String targetName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            player.sendMessage(plugin.getMsg("player-not-found"));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMsg("cannot-message-self"));
            return;
        }

        boolean isNowIgnoring = plugin.getChatManager().toggleIgnore(player.getUniqueId(), target.getUniqueId());
        if (isNowIgnoring) {
            player.sendMessage(plugin.getMsg("ignore-added").replace("%player%", target.getName() != null ? target.getName() : targetName));
        } else {
            player.sendMessage(plugin.getMsg("ignore-removed").replace("%player%", target.getName() != null ? target.getName() : targetName));
        }
    }

    private void showIgnoreList(Player player, int page) {
        Set<UUID> ignoredUUIDs = plugin.getChatManager().getIgnoreList(player.getUniqueId());
        List<UUID> ignoredList = new ArrayList<>(ignoredUUIDs);

        if (ignoredList.isEmpty()) {
            player.sendMessage("§cYou are not ignoring anyone.");
            return;
        }

        int itemsPerPage = 10;
        int maxPages = (int) Math.ceil(ignoredList.size() / (double) itemsPerPage);
        if (page < 1) page = 1;
        if (page > maxPages) page = maxPages;

        player.sendMessage("§e---- Ignored Players §7(Page " + page + "/" + maxPages + ") §e----");

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, ignoredList.size());

        for (int i = startIndex; i < endIndex; i++) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(ignoredList.get(i));
            player.sendMessage("§8- §c" + (op.getName() != null ? op.getName() : "Unknown"));
        }
    }
}