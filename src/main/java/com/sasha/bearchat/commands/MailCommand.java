package com.sasha.bearchat.commands;

import com.sasha.bearchat.BearChat;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MailCommand implements CommandExecutor {

    private final BearChat plugin;

    public MailCommand(BearChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getRawMsg("not-player"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getRawMsg("mail-help-header"));
            player.sendMessage(plugin.getRawMsg("mail-help-read"));
            player.sendMessage(plugin.getRawMsg("mail-help-send"));
            player.sendMessage(plugin.getRawMsg("mail-help-clear"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "clear":
                if (args.length > 1) {
                    String targetSender = args[1];
                    int removedCount = plugin.getChatManager().clearMailFromUser(player.getUniqueId(), targetSender);

                    if (removedCount > 0) {
                        player.sendMessage(plugin.getMsg("mail-cleared-user")
                                .replace("%count%", String.valueOf(removedCount))
                                .replace("%player%", targetSender));
                    } else {
                        player.sendMessage(plugin.getMsg("mail-empty-user").replace("%player%", targetSender));
                    }
                } else {
                    plugin.getChatManager().clearMail(player.getUniqueId());
                    player.sendMessage(plugin.getMsg("mail-cleared"));
                }
                break;

            case "read":
                List<String> mail = plugin.getChatManager().getMail(player.getUniqueId());
                if (mail == null || mail.isEmpty()) {
                    player.sendMessage(plugin.getMsg("mail-empty"));
                    return true;
                }

                int page = 1;
                if (args.length > 1) {
                    try { page = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
                }

                int itemsPerPage = 5;
                int maxPages = (int) Math.ceil(mail.size() / (double) itemsPerPage);
                if (page < 1) page = 1;
                if (page > maxPages) page = maxPages;

                player.sendMessage(plugin.getRawMsg("mail-mailbox-header")
                        .replace("%page%", String.valueOf(page))
                        .replace("%max%", String.valueOf(maxPages)));

                int startIndex = (page - 1) * itemsPerPage;
                int endIndex = Math.min(startIndex + itemsPerPage, mail.size());

                boolean mailUpdated = false;

                for (int i = startIndex; i < endIndex; i++) {
                    String entry = mail.get(i);

                    // Skip the broken "Legacy" strings if they still exist from the previous test
                    if (entry.startsWith("READ:Legacy")) {
                        continue;
                    }

                    if (entry.contains("[READ]")) {
                        // Already read - just print it normally
                        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', entry));
                    } else {
                        // Brand new message! Temporarily inject [NEW] for the player to see right now
                        String displayNew = entry.replaceFirst("\\]", "] &a[NEW]");
                        player.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', displayNew));

                        // Inject [READ] into the string and save it permanently
                        String saveRead = entry.replaceFirst("\\]", "] &7[READ]");
                        mail.set(i, saveRead);
                        mailUpdated = true;
                    }
                }

                // If any messages were marked as read, save the file!
                if (mailUpdated) {
                    plugin.getChatManager().updateMailStatus(player.getUniqueId(), mail);
                }
                break;

            case "send":
                if (args.length < 3) {
                    player.sendMessage(plugin.getRawMsg("usage-mail-send"));
                    return true;
                }

                String targetName = args[1];
                @SuppressWarnings("deprecation")
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                if (!target.hasPlayedBefore() && !target.isOnline()) {
                    player.sendMessage(plugin.getRawMsg("player-not-found").replace("%player%", targetName));
                    return true;
                }

                if (target.getUniqueId().equals(player.getUniqueId())) {
                    player.sendMessage(plugin.getMsg("cannot-mail-self"));
                    return true;
                }

                if (plugin.getChatManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
                    player.sendMessage(plugin.getMsg("cannot-mail-ignored"));
                    return true;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                plugin.getChatManager().sendMail(target.getUniqueId(), player.getName(), message);
                plugin.getChatManager().notifySocialSpy(player.getName(), target.getName(), "[MAIL] " + message);

                player.sendMessage(plugin.getMsg("mail-sent").replace("%player%", target.getName()));
                break;
        }
        return true;
    }
}