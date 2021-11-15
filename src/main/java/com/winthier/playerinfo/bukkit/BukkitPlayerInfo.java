package com.winthier.playerinfo.bukkit;

import com.winthier.playerinfo.OnlinePlayerInfo;
import com.winthier.playerinfo.PlayerInfo;
import com.winthier.playerinfo.util.Strings;
import com.winthier.sql.SQLDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

class BukkitPlayerInfo extends PlayerInfo {
    protected BukkitPlayerInfo(final BukkitPlayerInfoPlugin plugin) {
        super(plugin);
    }

    @Override
    public SQLDatabase getDatabase() {
        return plugin.getDb();
    }

    @Override
    public String format(String msg, Object... args) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (args.length > 0) msg = String.format(msg, args);
        return msg;
    }

    @Override
    public boolean send(UUID uuid, String msg, Object... args) {
        msg = format(msg, args);
        CommandSender sender = null;
        if (uuid == null) {
            sender = Bukkit.getServer().getConsoleSender();
        } else {
            sender = Bukkit.getServer().getPlayer(uuid);
            if (sender == null) return false;
        }
        sender.sendMessage(msg);
        return true;
    }

    @Override
    public void announce(String permission, String msg, Object... args) {
        msg = format(msg, args);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(msg);
            }
        }
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        if (uuid == null) return true;
        Player player = Bukkit.getServer().getPlayer(uuid);
        if (player != null) return player.hasPermission(permission);
        OfflinePlayer offPlayer = Bukkit.getServer().getOfflinePlayer(uuid);
        return plugin.getPermission().playerHas((String) null, offPlayer, permission);
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return Bukkit.getServer().getPlayer(uuid) != null;
    }

    @Override
    public String getTitle(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(uuid);
        if (plugin.getChat() == null) {
            if (plugin.getPermission() != null) return plugin.getPermission().getPrimaryGroup((String) null, player);
            return null;
        }
        String prefix = plugin.getChat().getPlayerPrefix((String) null, player);
        String suffix = plugin.getChat().getPlayerSuffix((String) null, player);
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";
        return ChatColor.translateAlternateColorCodes('&', prefix + suffix);
    }

    @Override
    public OnlinePlayerInfo getOnlinePlayerInfo(UUID uuid) {
        Player player = Bukkit.getServer().getPlayer(uuid);
        if (player == null) return null;
        OnlinePlayerInfo result = new OnlinePlayerInfo();
        final Location loc = player.getLocation();
        result.setLocation(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        result.setHealth((int) player.getHealth());
        result.setMaxHealth((int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        result.setFoodLevel(player.getFoodLevel());
        result.setSaturation((int) player.getSaturation());
        result.setExpLevel(player.getLevel());
        result.setExpPerc((int) Math.round(player.getExp() * 100f));
        result.setExp((int) (player.getExp() * (float) player.getExpToLevel()));
        List<String> potionEffects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            potionEffects.add(Strings.camelCase(effect.getType().getName()) + " " + (effect.getAmplifier() + 1));
        }
        result.setPotionEffects(potionEffects);
        result.setGameMode(Strings.camelCase(player.getGameMode().toString()));
        return result;
    }

    @Override
    public String countryForIP(String ip) {
        return plugin.countryForIP(ip);
    }

    @Override
    public void runSync(Runnable run) {
        Bukkit.getScheduler().runTask(plugin, run);
    }
}
