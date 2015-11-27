package com.winthier.playerinfo;

import com.winthier.playercache.PlayerCache;
import com.winthier.playerinfo.sql.*;
import com.winthier.playerinfo.util.Players;
import com.winthier.playerinfo.util.Strings;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayerInfoActions {
    private final PlayerInfo info;

    void loginfo(UUID sender, UUID player) {
        if (player == null) throw new NullPointerException("Player cannot be null");
        PlayerRow playerRow = PlayerRow.find(player);
        if (playerRow == null) throw new PlayerInfoException("No data for player");
        LogInfoRow logInfoRow = playerRow.getLogInfo();
        if (logInfoRow == null) throw new PlayerInfoException("No data for player");
        String playerName = Players.getName(player);
        info.send(sender, "&3Log Info for &b%s", playerName);
        info.send(sender, "&3 First login: &b%s &3(%s ago)", Strings.formatDate(logInfoRow.getFirstLog()), Strings.formatTimeDiffToNow(logInfoRow.getFirstLog()));
        info.send(sender, "&3 Last login: &b%s &3(%s ago)", Strings.formatDate(logInfoRow.getLastLog()), Strings.formatTimeDiffToNow(logInfoRow.getLastLog()));
    }

    private void rankLogs(UUID sender, LogInfoRow.Data data, int page) {
        List<LogInfoRow> rows = LogInfoRow.rankLogins(data, page);
        if (rows.isEmpty()) throw new PlayerInfoException("Nothing found");
        int count = LogInfoRow.countPages();
        info.send(sender, "&3%s logged in players (page %d/%d)", data.human, page + 1, count);
        for (LogInfoRow row : rows) {
            final Date date = data == LogInfoRow.Data.FIRST ? row.getFirstLog() : row.getLastLog();
            UUID uuid = row.getPlayer().getUuid();
            String name = Players.getName(uuid);
            if (info.isOnline(uuid)) {
                info.send(sender, " &3%s &b%s &3(%s ago)", Strings.formatDate(date), name, Strings.formatTimeDiffToNow(date));
            } else {
                info.send(sender, " &3%s &r%s &3(%s ago)", Strings.formatDate(date), name, Strings.formatTimeDiffToNow(date));
            }
        }
    }

    void status(UUID sender, UUID player) {
        PlayerRow playerRow = PlayerRow.find(player);
        if (playerRow == null) throw new PlayerInfoException("Nothing found");
        String playerName = Players.getName(player);
        info.send(sender, "&3Player info for &b%s", playerName);
        LogInfoRow logInfoRow = playerRow.getLogInfo();
        info.send(sender, " &3UUID: &b%s", player);
        String title = info.getTitle(player);
        if (title != null) {
            info.send(sender, " &3Title: [%s&3]", title);
        }
        if (logInfoRow != null) {
            info.send(sender, " &3First seen: &b%s &3(%s ago)", Strings.formatDate(logInfoRow.getFirstLog()), Strings.formatTimeDiffToNow(logInfoRow.getFirstLog()));
            info.send(sender, " &3Last seen: &b%s &3(%s ago)", Strings.formatDate(logInfoRow.getLastLog()), Strings.formatTimeDiffToNow(logInfoRow.getLastLog()));
        }
        OnTimeRow onTimeRow = playerRow.getOnTime();
        if (onTimeRow != null) {
            info.send(sender, " &3Online time: &b%s", Strings.formatSeconds(onTimeRow.getSeconds()));
        }
        OnlinePlayerInfo opi = info.getOnlinePlayerInfo(player);
        if (opi != null) {
            info.send(sender, " &3Online: &bYes");
            info.send(sender, " &Game Mode: &7%s", opi.getGameMode());
            info.send(sender, " &8Location: &7%s %d,%,d,%d", opi.getWorld(), opi.getX(), opi.getY(), opi.getZ());
            info.send(sender, " &8Health: &7%d/%d", opi.getHealth(), opi.getMaxHealth());
            info.send(sender, " &8Food: &7%d/%d &8Sat: &7%d", opi.getFoodLevel(), 20, opi.getSaturation());
            info.send(sender, " &8Exp: &7%d &8Level: &7%d", opi.getExp(), opi.getExpLevel());
            if (opi.getPotionEffects() != null && !opi.getPotionEffects().isEmpty()) {
                info.send(sender, " &8Potion effects: &7%s", Strings.join(opi.getPotionEffects(), ", "));
            }
        } else {
            info.send(sender, " &3Online: &bNo");
        }
        // Don't show the rest if player has hidden permission
        if (sender != null && !sender.equals(player) && info.hasPermission(player, "playerinfo.hidden")) return;
        PlayerCountryAndIPRow countryRow = playerRow.getCountry();
        if (countryRow != null) {
            if (countryRow.getIp() != null) {
                info.send(sender, " &3IP: &b%s", countryRow.getIp().getIp());
            }
            if (countryRow.getCountry() != null) {
                info.send(sender, " &3Country: &b%s", countryRow.getCountry().getCountry());
            }
        }
        List<UUID> alts = info.findAltAccounts(player);
        if (!alts.isEmpty()) {
            info.send(sender, " &3May also be: &b%s", Strings.join(Players.getNames(alts), ", "));
        }
    }

    void firstlog(UUID sender, int page) {
        rankLogs(sender, LogInfoRow.Data.FIRST, page);
    }

    void lastlog(UUID sender, int page) {
        rankLogs(sender, LogInfoRow.Data.LAST, page);
    }

    void ontime(UUID sender, UUID player) {
        if (player == null) throw new NullPointerException("Player cannot be null");
        PlayerRow playerRow = PlayerRow.find(player);
        if (playerRow == null) throw new PlayerInfoException("Nothing found");
        OnTimeRow onTimeRow = playerRow.getOnTime();
        if (onTimeRow == null) throw new PlayerInfoException("Nothing found");
        String playerName = Players.getName(player);
        info.send(sender, "&3Online time for &b%s", playerName);
        info.send(sender, " &b%s", Strings.formatSeconds(onTimeRow.getSeconds()));
    }

    void rankOntimes(UUID sender, int page) {
        List<OnTimeRow> rows = OnTimeRow.rankOntimes(page);
        if (rows.isEmpty()) throw new PlayerInfoException("Nothing found");
        int count = OnTimeRow.countPages();
        info.send(sender, "&3Top online players (page %d/%d)", page + 1, count);
        for (OnTimeRow row : rows) {
            int seconds = row.getSeconds();
            UUID uuid = row.getPlayer().getUuid();
            String playerName = Players.getName(uuid);
            info.send(sender, " &3%s, &b%s", Strings.formatSeconds(seconds), playerName);
        }
    }

    void listIgnoredIPs(UUID sender) {
        info.send(sender, "&3Ignored IPs: &b%s", Strings.join(info.getIgnoredIPs(), ", "));
    }

    void addIgnoredIP(UUID sender, String ip) {
        if (!Strings.isIP(ip)) throw new PlayerInfoException("Bad IP: " + ip);
        IPRow ipRow = IPRow.findOrCreate(ip);
        IgnoredIPRow.findOrCreate(ipRow);
        info.send(sender, "&3IP ignored: &b%s", ip);
        info.flushIgnoredIPs();
    }

    void removeIgnoredIP(UUID sender, String ip) {
        if (!Strings.isIP(ip)) throw new PlayerInfoException("Bad IP: " + ip);
        IPRow ipRow = IPRow.find(ip);
        if (ipRow == null) throw new PlayerInfoException("Unknown IP: " + ip);
        if (!IgnoredIPRow.delete(ipRow)) throw new PlayerInfoException("IP not ignored: " + ipRow.getIp());
        info.send(sender, "&3IP no longer ignored: &b%s", ip);
        info.flushIgnoredIPs();
    }

    void listPlayerIPs(UUID sender, UUID player) {
        PlayerRow playerRow = PlayerRow.find(player);
        if (playerRow == null) throw new PlayerInfoException("Nothing found");
        List<PlayerIPRow> playerIPRows = playerRow.getIps();
        if (playerIPRows == null) throw new PlayerInfoException("Nothing found");
        info.send(sender, "&3IPs of &b%s &3(%d)", Players.getName(player), playerIPRows.size());
        for (PlayerIPRow playerIPRow : playerIPRows) {
            String ip = playerIPRow.getIp().getIp();
            String country = info.countryForIP(ip);
            if (country == null) country = "N/A";
            info.send(sender, " &3%s &b%s", ip, country);
        }
    }

    void findSharedIPs(UUID sender, List<UUID> players) {
        List<PlayerRow> playerRows = PlayerRow.findAll(players);
        if (playerRows.isEmpty()) throw new PlayerInfoException("Nothing found");
        List<String> ips = new LinkedList<String>();
        List<PlayerIPRow> playerIPRows = playerRows.get(0).getIps();
        if (playerIPRows == null) throw new PlayerInfoException("Nothing found");
        for (PlayerIPRow playerIPRow : playerIPRows) ips.add(playerIPRow.getIp().getIp());
        for (int i = 1; i < playerRows.size(); ++i) {
            PlayerRow playerRow = playerRows.get(i);
             playerIPRows = playerRows.get(0).getIps();
            if (playerIPRows == null) throw new PlayerInfoException("Nothing found");
            List<String> otherIPs = new ArrayList<>();
            for (PlayerIPRow playerIPRow : playerIPRows) otherIPs.add(playerIPRow.getIp().getIp());
            ips.retainAll(otherIPs);
            if (ips.isEmpty()) throw new PlayerInfoException("Nothing found");
        }
        info.send(sender, "&3Common IPs (%d)", ips.size());
        for (String ip : ips) {
            String country = info.countryForIP(ip);
            if (country == null) country = "N/A";
            info.send(sender, "&3%s &b%s", ip, country);
        }
    }

    void listAltIPs(UUID sender, UUID player) {
        List<UUID> alts = info.findAltAccounts(player);
        if (alts.isEmpty()) throw new PlayerInfoException("Nothing found");
        alts.add(player);
        findSharedIPs(sender, alts);
    }
}
