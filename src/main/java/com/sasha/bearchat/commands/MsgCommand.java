package com.sasha.bearchat.commands;

import com.sasha.bearchat.BearChat;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class MsgCommand implements CommandExecutor {

    private final BearChat plugin;

    public MsgCommand(BearChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            // Switched to getRawMsg
            sender.sendMessage(plugin.getRawMsg("not-player"));
            return true;
        }

        Player target = null;
        String message = "";

        if (command.getName().equalsIgnoreCase("msg")) {
            if (args.length < 2) {
                player.sendMessage("§cUsage: /msg <player> <message>");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        } else if (command.getName().equalsIgnoreCase("reply")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /reply <message>");
                return true;
            }

            UUID targetId = plugin.getChatManager().getReplyTarget(player.getUniqueId());
            if (targetId != null) {
                target = Bukkit.getPlayer(targetId);
            } else {
                player.sendMessage("§cYou have nobody to reply to!");
                return true;
            }

            message = String.join(" ", args);
        }

        if (target == null || !target.isOnline()) {
            // Switched to getRawMsg
            player.sendMessage(plugin.getRawMsg("player-not-found"));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            // Switched to getRawMsg
            player.sendMessage(plugin.getRawMsg("cannot-message-self"));
            return true;
        }

        if (plugin.getChatManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            // Switched to getRawMsg
            player.sendMessage(plugin.getRawMsg("ignore-error"));
            return true;
        }

        // --- INTERACTIVE MESSAGE FOR THE SENDER ---
        // Switched to getRawMsg
        TextComponent toMsg = new TextComponent(plugin.getRawMsg("msg-format-to")
                .replace("%player%", target.getName())
                .replace("%message%", message));

        toMsg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + target.getName() + " "));
        toMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to message §e" + target.getName())));

        player.spigot().sendMessage(toMsg);


        // --- INTERACTIVE MESSAGE FOR THE RECEIVER ---
        // Switched to getRawMsg
        TextComponent fromMsg = new TextComponent(plugin.getRawMsg("msg-format-from")
                .replace("%player%", player.getName())
                .replace("%message%", message));

        fromMsg.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getName() + " "));
        fromMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§7Click to reply to §e" + player.getName())));

        target.spigot().sendMessage(fromMsg);

        // Notify SocialSpy Admins
        plugin.getChatManager().notifySocialSpy(player.getName(), target.getName(), message);

        // Update Reply Data
        plugin.getChatManager().setRecentMessage(player.getUniqueId(), target.getUniqueId());

        return true;
    }
}