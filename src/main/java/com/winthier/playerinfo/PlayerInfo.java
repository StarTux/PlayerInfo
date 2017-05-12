package com.winthier.playerinfo;

import com.winthier.playerinfo.sql.*;
import com.winthier.playerinfo.util.Players;
import com.winthier.playerinfo.util.Strings;
import com.winthier.sql.SQLDatabase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;

public abstract class PlayerInfo {
    @Getter
    private static PlayerInfo instance;
    @Getter
    private PlayerInfoCommands commands = new PlayerInfoCommands(this);
    @Getter
    private PlayerInfoActions actions = new PlayerInfoActions(this);
    private List<String> ignoredIPs = null;

    protected PlayerInfo() {
        instance = this;
    }
    
    public abstract SQLDatabase getDatabase();
    public abstract String format(String msg, Object... args);
    public abstract boolean send(UUID uuid, String msg, Object... args);
    public abstract void announce(String permission, String msg, Object... args);
    public abstract boolean hasPermission(UUID uuid, String permission);
    public abstract boolean isOnline(UUID uuid);
    public abstract String getTitle(UUID uuid);
    public abstract OnlinePlayerInfo getOnlinePlayerInfo(UUID uuid);
    public abstract String countryForIP(String ip);

    public List<UUID> findAltAccounts(UUID player) {
        if (player == null) throw new NullPointerException("UUID cannot be null");
        // Fetch player row
        PlayerRow playerRow = PlayerRow.find(player);
        if (playerRow == null) return Collections.<UUID>emptyList();
        // Fetch player ips
        List<PlayerIPRow> playerIPRows = playerRow.getIps();
        if (playerIPRows == null) return Collections.<UUID>emptyList();
        List<IPRow> ips = new ArrayList<IPRow>();
        for (PlayerIPRow ip : playerIPRows) {
            if (!getIgnoredIPs().contains(ip.getIp().getIp())) {
                ips.add(ip.getIp());
            }
        }
        // Fetch all player ips for the ips
        playerIPRows = PlayerIPRow.find(ips);
        Set<UUID> result = new HashSet<>();
        for (PlayerIPRow ip : playerIPRows) {
            UUID uuid = ip.getPlayer().getUuid();
            if (!hasPermission(uuid, "playerinfo.hidden")) {
                result.add(uuid);
            }
        }
        result.remove(player);
        return new ArrayList<UUID>(result);
    }

    public void onPlayerLogin(UUID uuid, String ip) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        if (ip == null) throw new NullPointerException("IP cannot be null");
        PlayerRow playerRow = PlayerRow.findOrCreate(uuid);
        if (!Strings.isIP(ip)) {
            System.err.println("Received bad IP: " + ip);
        } else {
            IPRow ipRow = IPRow.findOrCreate(ip);
            PlayerIPRow.findOrCreate(playerRow, ipRow);
            if (!getIgnoredIPs().contains(ipRow.getIp())) {
                String country = countryForIP(ipRow.getIp());
                CountryRow countryRow = country == null ? null : CountryRow.findOrCreate(country);
                PlayerCountryAndIPRow.updateOrCreate(playerRow, ipRow, countryRow);
            }
        }
        LogInfoRow.updateOrCreate(playerRow);
        if (!hasPermission(uuid, "playerinfo.hidden")) {
            List<UUID> alts = findAltAccounts(uuid);
            if (!alts.isEmpty()) {
                String playerName = Players.getName(uuid);
                announce("playerinfo.notify", "&8%s may also be: %s", playerName, Strings.join(Players.getNames(alts), ", "));
            }
        }
    }

    public void onTimePassed(List<UUID> onlinePlayers, int seconds) {
        for (UUID uuid : onlinePlayers) {
            PlayerRow playerRow = PlayerRow.findOrCreate(uuid);
            OnTimeRow.updateOrCreate(playerRow, seconds);
        }
    }

    public List<String> getIgnoredIPs() {
        if (ignoredIPs == null) {
            ignoredIPs = new ArrayList<String>();
            for (IgnoredIPRow row : IgnoredIPRow.findAll()) {
                ignoredIPs.add(row.getIp().getIp());
            }
        }
        return ignoredIPs;
    }

    void flushIgnoredIPs() {
        ignoredIPs = null;
    }

    public long getOnTime(UUID uuid) {
        PlayerRow playerRow = PlayerRow.find(uuid);
        if (playerRow == null) return 0L;
        OnTimeRow onTimeRow = playerRow.getOnTime();
        if (onTimeRow == null) return 0L;
        return onTimeRow.getSeconds();
    }

    public Date getFirstLog(UUID uuid) {
        PlayerRow playerRow = PlayerRow.find(uuid);
        if (playerRow == null) return null;
        LogInfoRow logInfoRow = playerRow.getLogInfo();
        if (logInfoRow == null) return null;
        return logInfoRow.getFirstLog();
    }

    public Date getLastLog(UUID uuid) {
        PlayerRow playerRow = PlayerRow.find(uuid);
        if (playerRow == null) return null;
        LogInfoRow logInfoRow = playerRow.getLogInfo();
        if (logInfoRow == null) return null;
        return logInfoRow.getLastLog();
    }
}
