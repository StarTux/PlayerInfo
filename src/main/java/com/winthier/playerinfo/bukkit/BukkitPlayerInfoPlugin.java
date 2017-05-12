package com.winthier.playerinfo.bukkit;

import com.maxmind.geoip.LookupService;
import com.winthier.playerinfo.PlayerInfo;
import com.winthier.playerinfo.PlayerInfoCommands;
import com.winthier.playerinfo.sql.*;
import com.winthier.sql.SQLDatabase;
import com.winthier.sql.SQLDatabase;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.PersistenceException;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class BukkitPlayerInfoPlugin extends JavaPlugin {
    private final int ON_TIME_SECONDS = 60;
    private final int TPS = 20;
    private BukkitPlayerInfo info;
    private Chat chat;
    private Permission permission;
    private final BukkitEventHandler eventHandler = new BukkitEventHandler(this);
    private BukkitRunnable onTimeTask;
    private LookupService geoip;
    private SQLDatabase db;
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) chat = chatProvider.getProvider();
        return (chat != null);
    }

    private boolean setupPermission() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) permission = permissionProvider.getProvider();
        return (permission != null);
    }

    @Override
    public void onEnable() {
        this.info = new BukkitPlayerInfo(this);
        db = new SQLDatabase(this);
        db.registerTables(CountryRow.class,
                          IPRow.class,
                          IgnoredIPRow.class,
                          LogInfoRow.class,
                          OnTimeRow.class,
                          PlayerCountryAndIPRow.class,
                          PlayerIPRow.class,
                          PlayerRow.class);
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
    }

    @Override
    public void onDisable() {
        onTimeTask.cancel();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        return false;
    }

    Chat getChat() {
        if (chat == null) {
            if (!setupChat()) return null;
        }
        return chat;
    }

    Permission getPermission() {
        if (permission == null) {
            if (!setupPermission()) return null;
        }
        return permission;
    }

    List<UUID> getOnlineUuids() {
        List<UUID> result = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) result.add(player.getUniqueId());
        return result;
    }

    LookupService getGeoIP() {
        if (geoip == null) {
            try {
                geoip = new LookupService(new File(getDataFolder(), "GeoIP.dat"), LookupService.GEOIP_MEMORY_CACHE);
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
}

class MyCommand implements CommandExecutor {
    private final Method method;
    
    MyCommand(String name) {
        PlayerInfoCommands commands = PlayerInfo.getInstance().getCommands();
        Method method = null;
        try {
            method = commands.getClass().getMethod(name, UUID.class, String[].class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.method = method;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]) {
        if (method == null) return false;
        final UUID uuid = sender instanceof Player ? ((Player)sender).getUniqueId() : null;
        try {
            return (boolean)method.invoke(PlayerInfo.getInstance().getCommands(), uuid, args);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
