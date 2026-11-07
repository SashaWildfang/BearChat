package com.sasha.bearchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BearChatTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        // Private Messaging & Ignore (Online Players)
        if (cmd.equals("msg") || cmd.equals("tell") || cmd.equals("message") || cmd.equals("w") || cmd.equals("whisper") || cmd.equals("ignore")) {
            if (args.length == 1) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        // IgnoreList Management
        else if (cmd.equals("ignorelist")) {
            if (args.length == 1) {
                return List.of("add", "remove").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        // Channel Switching
        else if (cmd.equals("channel") || cmd.equals("chat")) {
            if (args.length == 1) {
                return List.of("public", "trading", "donor", "staff").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        // Admin Chat Tools
        else if (cmd.equals("chatadmin") || cmd.equals("lockchat") || cmd.equals("unlockchat")) {
            if (args.length == 1 && cmd.equals("chatadmin")) {
                return List.of("clear", "lock", "unlock", "slowmode", "status").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2 && args[0].equalsIgnoreCase("slowmode")) {
                return List.of("3s", "5s", "10s", "30s", "1m").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        // --- NEW: Mail System ---
        else if (cmd.equals("mail")) {
            if (args.length == 1) {
                // Suggest subcommands
                return List.of("read", "send", "clear").stream()
                        .filter(s -> s.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("send") || args[0].equalsIgnoreCase("clear"))) {
                // Suggest online player names for BOTH the 'send' and 'clear' subcommands for everyone!
                return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}