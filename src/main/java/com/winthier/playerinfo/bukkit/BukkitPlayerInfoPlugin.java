package com.winthier.playerinfo.bukkit;

import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.connect.Connect;
import com.maxmind.geoip.LookupService;
import com.winthier.playerinfo.PlayerInfo;
import com.winthier.playerinfo.PlayerInfoCommands;
import com.winthier.playerinfo.sql.CountryRow;
import com.winthier.playerinfo.sql.DailyOnTimeRow;
import com.winthier.playerinfo.sql.IPRow;
import com.winthier.playerinfo.sql.IgnoredIPRow;
import com.winthier.playerinfo.sql.LogInfoRow;
import com.winthier.playerinfo.sql.OnTimeRow;
import com.winthier.playerinfo.sql.PlayerCountryAndIPRow;
import com.winthier.playerinfo.sql.PlayerIPRow;
import com.winthier.playerinfo.sql.PlayerRow;
import com.winthier.sql.SQLDatabase;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.cavetale.core.playercache.PlayerCache;

@Getter
public final class BukkitPlayerInfoPlugin extends JavaPlugin {
    private static BukkitPlayerInfoPlugin instance;
    private static final int ON_TIME_SECONDS = 60;
    private static final int TPS = 20;
    private BukkitPlayerInfo info;
    private final BukkitEventHandler eventHandler = new BukkitEventHandler(this);
    private BukkitRunnable onTimeTask;
    private LookupService geoip;
    private SQLDatabase db;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.info = new BukkitPlayerInfo(this);
        db = new SQLDatabase(this);
        db.registerTables(List.of(CountryRow.class,
                                  IPRow.class,
                                  IgnoredIPRow.class,
                                  LogInfoRow.class,
                                  OnTimeRow.class,
                                  DailyOnTimeRow.class,
                                  PlayerCountryAndIPRow.class,
                                  PlayerIPRow.class,
                                  PlayerRow.class));
        if (!db.createAllTables()) {
            getLogger().warning("Database setup failed. Disabling " + getDescription().getName());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        for (String name : getDescription().getCommands().keySet()) {
            getCommand(name).setExecutor(new MyCommand(name));
        }
        getServer().getPluginManager().registerEvents(eventHandler, this);
        onTimeTask = new BukkitRunnable() {
            @Override public void run() {
                info.onTimePassed(getOnlineUuids(), ON_TIME_SECONDS);
            }
        };
        onTimeTask.runTaskTimer(this, ON_TIME_SECONDS * TPS, ON_TIME_SECONDS * TPS);
        if (getGeoIP() == null) {
            getLogger().severe("GeoIP not found!");
        }
    }

    @Override
    public void onDisable() {
        onTimeTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    List<UUID> getOnlineUuids() {
        List<UUID> result = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) result.add(player.getUniqueId());
        return result;
    }

    LookupService getGeoIP() {
        if (geoip == null) {
            File file = new File("/home/mc/public/config/PlayerInfo/GeoIP.dat");
            if (!file.exists()) {
                file = new File(getDataFolder(), "GeoIP.dat");
            }
            if (!file.exists()) {
                return null;
            }
            try {
                geoip = new LookupService(file, LookupService.GEOIP_MEMORY_CACHE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return geoip;
    }

    String countryForIP(String ip) {
        LookupService service = getGeoIP();
        if (service == null) return null;
        return service.getCountry(ip).getName();
    }

    public static SQLDatabase database() {
        return instance.db;
    }
}

class MyCommand implements TabExecutor {
    private final Method method;

    MyCommand(final String name) {
        PlayerInfoCommands commands = PlayerInfo.getInstance().getCommands();
        Method theMethod = null;
        try {
            theMethod = commands.getClass().getMethod(name, UUID.class, String[].class);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        this.method = theMethod;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (method == null) return false;
        final UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        try {
            return (boolean) method.invoke(PlayerInfo.getInstance().getCommands(), uuid, args);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) return List.of();
        final List<RemotePlayer> players = Connect.get().getRemotePlayers();
        if (players.isEmpty()) return List.of();
        final String arg = args[0];
        final String lower = arg.toLowerCase();
        final List<String> result = new ArrayList<>(players.size());
        for (RemotePlayer it : players) {
            if (it.getName().toLowerCase().contains(lower)) {
                result.add(it.getName());
            }
        }
        return !result.isEmpty()
            ? result
            : PlayerCache.completeNames(arg);
    }
}
