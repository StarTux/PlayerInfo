package com.winthier.playerinfo;

import com.winthier.playerinfo.sql.CountryRow;
import com.winthier.playerinfo.sql.DailyOnTimeRow;
import com.winthier.playerinfo.sql.IPRow;
import com.winthier.playerinfo.sql.IgnoredIPRow;
import com.winthier.playerinfo.sql.LogInfoRow;
import com.winthier.playerinfo.sql.OnTimeRow;
import com.winthier.playerinfo.sql.PlayerCountryAndIPRow;
import com.winthier.playerinfo.sql.PlayerIPRow;
import com.winthier.playerinfo.sql.PlayerRow;
import com.winthier.playerinfo.util.Players;
import com.winthier.playerinfo.util.Strings;
import com.winthier.sql.SQLDatabase;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public abstract void runSync(Runnable run);

    public final List<UUID> findAltAccounts(UUID player) {
        if (player == null) throw new NullPointerException("UUID cannot be null");
        // Fetch player row
        PlayerRow playerRow = PlayerRow.find(player);
        if (playerRow == null) return Collections.<UUID>emptyList();
        // Fetch player ips
        List<PlayerIPRow> playerIPRows = playerRow.getIps();
        if (playerIPRows == null) return Collections.<UUID>emptyList();
        List<IPRow> ips = new ArrayList<IPRow>();
        List<String> ignoredIPList = IgnoredIPRow.findAllAsString();
        for (PlayerIPRow ip : playerIPRows) {
            if (!ignoredIPList.contains(ip.getIp().getIp())) {
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

    public final void onPlayerLogin(UUID uuid, String ip) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        if (ip == null) throw new NullPointerException("IP cannot be null");
        boolean hasHiddenPerm = hasPermission(uuid, "playerinfo.hidden");
        String country = countryForIP(ip);
        getDatabase().scheduleAsyncTask(() -> {
                PlayerRow playerRow = PlayerRow.findOrCreate(uuid);
                if (!Strings.isIP(ip)) {
                    System.err.println("Received bad IP: " + ip);
                } else {
                    IPRow ipRow = IPRow.findOrCreate(ip);
                    PlayerIPRow.findOrCreate(playerRow, ipRow);
                    List<String> ignoredIPList = IgnoredIPRow.findAllAsString();
                    if (!ignoredIPList.contains(ipRow.getIp())) {
                        CountryRow countryRow = country == null ? null : CountryRow.findOrCreate(country);
                        PlayerCountryAndIPRow.updateOrCreate(playerRow, ipRow, countryRow);
                    }
                }
                LogInfoRow.updateOrCreate(playerRow);
                if (!hasHiddenPerm) {
                    List<UUID> alts = findAltAccounts(uuid);
                    if (!alts.isEmpty()) {
                        runSync(() -> {
                                String playerName = Players.getName(uuid);
                                announce("playerinfo.notify", "&8%s may also be: %s", playerName, Strings.join(Players.getNames(alts), ", "));
                            });
                    }
                }
            });
    }

    public final int getDayId() {
        final Instant instant = Instant.now();
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        final LocalDate localDate = localDateTime.toLocalDate();
        final int year = localDate.getYear();
        final int month = localDate.getMonth().getValue();
        final int day = localDate.getDayOfMonth();
        final int dayId = year * 10000 + month * 100 + day;
        return dayId;
    }

    public final void onTimePassed(List<UUID> onlinePlayers, int seconds) {
        final int dayId = getDayId();
        getDatabase().scheduleAsyncTask(() -> {
                for (UUID uuid : onlinePlayers) {
                    PlayerRow playerRow = PlayerRow.findOrCreate(uuid);
                    OnTimeRow.updateOrCreate(playerRow, seconds);
                    DailyOnTimeRow.updateOrCreate(playerRow, dayId, seconds);
                }
            });
    }

    public final long getOnTime(UUID uuid) {
        PlayerRow playerRow = PlayerRow.find(uuid);
        if (playerRow == null) return 0L;
        OnTimeRow onTimeRow = playerRow.getOnTime();
        if (onTimeRow == null) return 0L;
        return onTimeRow.getSeconds();
    }

    public final Date getFirstLog(UUID uuid) {
        PlayerRow playerRow = PlayerRow.find(uuid);
        if (playerRow == null) return null;
        LogInfoRow logInfoRow = playerRow.getLogInfo();
        if (logInfoRow == null) return null;
        return logInfoRow.getFirstLog();
    }

    public final Date getLastLog(UUID uuid) {
        PlayerRow playerRow = PlayerRow.find(uuid);
        if (playerRow == null) return null;
        LogInfoRow logInfoRow = playerRow.getLogInfo();
        if (logInfoRow == null) return null;
        return logInfoRow.getLastLog();
    }
}
