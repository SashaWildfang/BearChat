package com.sasha.bearchat.listeners;

import com.sasha.bearchat.BearChat;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.regex.Pattern;

public class ChatListener implements Listener {

    private final BearChat plugin;
    private final Pattern LINK_PATTERN = Pattern.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))");

    public ChatListener(BearChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // 1. Lock Check
        if (plugin.getChatManager().isChatLocked && !player.hasPermission("bn.bearchat.bypass.lock")) {
            player.sendMessage(plugin.getMsg("chat-locked"));
            event.setCancelled(true);
            return;
        }

        // 2. Anti-Spam Check (Prevents repeating the exact same message)
        if (plugin.getChatManager().isSameAsLastMessage(player.getUniqueId(), message)) {
            if (!player.hasPermission("bn.bearchat.bypass.spam")) {
                player.sendMessage(plugin.getMsg("chat-same-message"));
                event.setCancelled(true);
                return;
            }
        }

        // 3. Distinct Cooldown Logic (Checks for Admin Slowmode vs Default)
        int cooldown = plugin.getChatManager().getRemainingCooldown(player.getUniqueId());
        if (cooldown > 0 && !player.hasPermission("bn.bearchat.bypass.cooldown")) {
            if (plugin.getChatManager().isAdminSlowmodeActive()) {
                // Admin Slowmode Message
                player.sendMessage(plugin.getMsg("chat-slowmode-active").replace("%time%", String.valueOf(cooldown)));
            } else {
                // Default 3s Chat Delay Message
                player.sendMessage(plugin.getMsg("chat-default-cooldown").replace("%time%", String.valueOf(cooldown)));
            }
            event.setCancelled(true);
            return;
        }

        // 4. Filters (Links & Blocked Words)
        if (plugin.getConfig().getBoolean("filters.block-links") && !player.hasPermission("bn.bearchat.bypass.links")) {
            if (LINK_PATTERN.matcher(message.replace(" ", "")).find()) {
                player.sendMessage(plugin.getMsg("filter-link"));
                event.setCancelled(true);
                return;
            }
        }

        if (!player.hasPermission("bn.bearchat.bypass.filter")) {
            String sanitized = message.toLowerCase().replaceAll("[^a-z]", "");
            for (String word : plugin.getConfig().getStringList("filters.blocked-words")) {
                if (sanitized.contains(word.toLowerCase())) {
                    player.sendMessage(plugin.getMsg("filter-word"));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // 5. Cancel Vanilla Chat & Update Trackers
        // We cancel the event because we manually broadcast the interactive Bungee components later!
        event.setCancelled(true);
        plugin.getChatManager().setLastMessage(player.getUniqueId(), message);
        plugin.getChatManager().updateMessageTime(player.getUniqueId());

        // 6. Determine Group Format via PlaceholderAPI
        String primaryGroupRaw = PlaceholderAPI.setPlaceholders(player, "%luckperms_primary_group_name%");
        String groupFormatPath = "group-formats." + primaryGroupRaw;

        // Fallback to 'default' if their specific group isn't defined in the config
        if (!plugin.getConfig().contains(groupFormatPath)) {
            groupFormatPath = "group-formats.default";
        }
        String groupFormat = plugin.getConfig().getString(groupFormatPath, "%luckperms_prefix%&7%player% &8» &f%message%");

        // 7. Append Channel Prefix (if applicable)
        String channel = plugin.getChatManager().getChannel(player.getUniqueId());
        String channelPrefixPath = "channel-prefixes." + channel;
        String channelPrefix = plugin.getConfig().getString(channelPrefixPath, "");

        String rawFormat = channelPrefix + groupFormat;
        String finalFormat = rawFormat.replace("%player%", player.getName()).replace("%message%", message);

        // Parse all Placeholders (including %luckperms_prefix%)
        finalFormat = PlaceholderAPI.setPlaceholders(player, finalFormat);
        finalFormat = ChatColor.translateAlternateColorCodes('&', finalFormat);

        // ==========================================
        // 8. CUSTOM HOVER VARIABLES CALCULATION
        // ==========================================

        // A. Capitalize Primary Rank (e.g. "admin" -> "Admin")
        String capitalizedRank = "Member";
        if (primaryGroupRaw != null && !primaryGroupRaw.isEmpty()) {
            capitalizedRank = primaryGroupRaw.substring(0, 1).toUpperCase() + primaryGroupRaw.substring(1).toLowerCase();
        }

        // B. Extract Prison Rank (e.g. "rank_a" -> "A")
        String allGroups = PlaceholderAPI.setPlaceholders(player, "%luckperms_inherited_groups%");
        String prisonRank = "Free";
        for (String group : allGroups.split(",")) {
            group = group.trim();
            if (group.startsWith("rank_")) {
                prisonRank = group.replace("rank_", "").toUpperCase();
                break;
            }
        }

        // ==========================================
        // 9. BUILD MULTI-LINE HOVER COMPONENT
        // ==========================================
        List<String> hoverLines = plugin.getConfig().getStringList("hover-text");
        if (hoverLines.isEmpty()) {
            hoverLines.add("&eClick to message %player%"); // Safety fallback
        }

        StringBuilder hoverBuilder = new StringBuilder();
        for (int i = 0; i < hoverLines.size(); i++) {
            String line = hoverLines.get(i)
                    .replace("%player%", player.getName())
                    .replace("%capitalized_rank%", capitalizedRank) // Our new custom placeholder!
                    .replace("%prison_rank%", prisonRank);          // Our new custom placeholder!

            // Run PlaceholderAPI and Color Translation on each line
            line = PlaceholderAPI.setPlaceholders(player, line);
            line = ChatColor.translateAlternateColorCodes('&', line);

            hoverBuilder.append(line);

            // Add a line break unless it's the last line
            if (i < hoverLines.size() - 1) {
                hoverBuilder.append("\n");
            }
        }

        // 10. Attach Interactive Components
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(finalFormat));
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hoverBuilder.toString())));
        component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + player.getName() + " "));

        // 11. Distribution (Checking Channels & Ignores)
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (plugin.getChatManager().isIgnoring(recipient.getUniqueId(), player.getUniqueId())) continue;

            boolean recipientInChannel = plugin.getChatManager().getChannel(recipient.getUniqueId()).equals(channel);
            boolean isStaffChat = channel.equals("staff");

            if (recipientInChannel || (isStaffChat && recipient.hasPermission("bn.channel.staff"))) {
                recipient.spigot().sendMessage(component);
            }
        }

        Bukkit.getConsoleSender().sendMessage(finalFormat);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // This now ONLY counts mail that has the "UNREAD:" tag
        int unreadCount = plugin.getChatManager().getUnreadMailCount(player.getUniqueId());

        if (unreadCount > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage(plugin.getMsg("mail-login-notification").replace("%count%", String.valueOf(unreadCount)));
                }
            }, 40L);
        }
    }
}