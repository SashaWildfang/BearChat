package com.sasha.bearchat.managers;

import com.sasha.bearchat.BearChat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ChatManager {

    private final BearChat plugin;

    public boolean isChatLocked = false;
    public int adminSlowmodeSeconds = 0;

    private final Map<UUID, UUID> recentMessages = new HashMap<>();
    private final Map<UUID, Set<UUID>> ignoreLists = new HashMap<>();
    private final Map<UUID, String> activeChannels = new HashMap<>();
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private final Map<UUID, String> lastMessages = new HashMap<>();

    private final Set<UUID> socialSpyUsers = new HashSet<>();
    private File mailFile;
    private FileConfiguration mailConfig;

    public ChatManager(BearChat plugin) {
        this.plugin = plugin;
    }

    public void setupMailFile(BearChat plugin) {
        mailFile = new File(plugin.getDataFolder(), "mail.yml");
        if (!mailFile.exists()) {
            try { mailFile.createNewFile(); } catch (Exception ignored) {}
        }
        mailConfig = YamlConfiguration.loadConfiguration(mailFile);
    }

    public void setRecentMessage(UUID sender, UUID receiver) { recentMessages.put(receiver, sender); }
    public UUID getReplyTarget(UUID player) { return recentMessages.get(player); }

    public boolean toggleIgnore(UUID player, UUID target) {
        ignoreLists.putIfAbsent(player, new HashSet<>());
        Set<UUID> list = ignoreLists.get(player);
        if (list.contains(target)) {
            list.remove(target);
            return false;
        } else {
            list.add(target);
            return true;
        }
    }
    public boolean isIgnoring(UUID player, UUID target) {
        return ignoreLists.containsKey(player) && ignoreLists.get(player).contains(target);
    }
    public Set<UUID> getIgnoreList(UUID player) {
        return ignoreLists.getOrDefault(player, new HashSet<>());
    }

    public void setChannel(UUID player, String channel) { activeChannels.put(player, channel.toLowerCase()); }
    public String getChannel(UUID player) { return activeChannels.getOrDefault(player, "public"); }

    public boolean isAdminSlowmodeActive() { return adminSlowmodeSeconds > 0; }

    public int getRemainingCooldown(UUID player) {
        long last = lastMessageTime.getOrDefault(player, 0L);
        long now = System.currentTimeMillis();
        int requiredDelay = adminSlowmodeSeconds > 0 ? adminSlowmodeSeconds : plugin.getConfig().getInt("settings.default-cooldown", 3);
        long passedTime = (now - last) / 1000;
        if (passedTime >= requiredDelay) return 0;
        return (int) (requiredDelay - passedTime);
    }

    public void updateMessageTime(UUID player) { lastMessageTime.put(player, System.currentTimeMillis()); }
    public boolean isSameAsLastMessage(UUID player, String message) { return message.equalsIgnoreCase(lastMessages.get(player)); }
    public void setLastMessage(UUID player, String message) { lastMessages.put(player, message); }

    public void toggleSocialSpy(Player player) {
        if (socialSpyUsers.contains(player.getUniqueId())) {
            socialSpyUsers.remove(player.getUniqueId());
            player.sendMessage(plugin.getMsg("socialspy-disabled"));
        } else {
            socialSpyUsers.add(player.getUniqueId());
            player.sendMessage(plugin.getMsg("socialspy-enabled"));
        }
    }

    public void notifySocialSpy(String sender, String target, String message) {
        String format = plugin.getMsg("socialspy-format").replace("%sender%", sender).replace("%target%", target).replace("%message%", message);
        for (UUID uuid : socialSpyUsers) {
            Player spy = Bukkit.getPlayer(uuid);
            if (spy != null && spy.isOnline()) {
                spy.sendMessage(format);
            }
        }
    }

    // --- REVERTED & CLEANED MAIL LOGIC ---
    public void sendMail(UUID targetId, String senderName, String message) {
        String tzString = plugin.getConfig().getString("settings.timezone", "America/Denver");
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, h:mm a");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone(tzString));

        String date = sdf.format(new java.util.Date());
        String formattedMail = plugin.getConfig().getString("messages.mail-format", "&8[&e%date%&8] &7%sender% &8» &f%message%")
                .replace("%date%", date)
                .replace("%sender%", senderName)
                .replace("%message%", message);

        List<String> currentMail = mailConfig.getStringList(targetId.toString());
        currentMail.add(formattedMail);
        mailConfig.set(targetId.toString(), currentMail);

        try { mailConfig.save(mailFile); } catch (Exception ignored) {}

        Player target = Bukkit.getPlayer(targetId);
        if (target != null && target.isOnline()) {
            target.sendMessage(plugin.getMsg("mail-received"));
        }
    }

    public List<String> getMail(UUID targetId) { return mailConfig.getStringList(targetId.toString()); }

    public void clearMail(UUID targetId) {
        mailConfig.set(targetId.toString(), null);
        try { mailConfig.save(mailFile); } catch (Exception ignored) {}
    }

    public void updateMailStatus(UUID targetId, List<String> updatedMail) {
        mailConfig.set(targetId.toString(), updatedMail);
        try { mailConfig.save(mailFile); } catch (Exception ignored) {}
    }

    public int clearMailFromUser(UUID ownerId, String targetSender) {
        List<String> currentMail = mailConfig.getStringList(ownerId.toString());
        if (currentMail == null || currentMail.isEmpty()) return 0;

        int originalSize = currentMail.size();
        // Simply checks if the target's name is anywhere in the string!
        currentMail.removeIf(mail -> mail.toLowerCase().contains(targetSender.toLowerCase()));

        if (currentMail.size() != originalSize) {
            mailConfig.set(ownerId.toString(), currentMail);
            try { mailConfig.save(mailFile); } catch (Exception ignored) {}
        }
        return originalSize - currentMail.size();
    }

    public int getUnreadMailCount(UUID targetId) {
        List<String> currentMail = mailConfig.getStringList(targetId.toString());
        if (currentMail == null) return 0;
        int count = 0;
        for (String mail : currentMail) {
            // Count any string that DOES NOT have the read tag
            if (!mail.contains("[READ]")) count++;
        }
        return count;
    }
}