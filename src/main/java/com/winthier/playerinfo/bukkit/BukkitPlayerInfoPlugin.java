package com.winthier.playerinfo.bukkit;

import com.maxmind.geoip.LookupService;
import com.winthier.playerinfo.PlayerInfo;
import com.winthier.playerinfo.PlayerInfoCommands;
import com.winthier.playerinfo.sql.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.persistence.PersistenceException;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class BukkitPlayerInfoPlugin extends JavaPlugin {
    private final int ON_TIME_SECONDS = 60;
    private final int TPS = 20;
    private BukkitPlayerInfo info;
    private Chat chat;
    private final BukkitEventHandler eventHandler = new BukkitEventHandler(this);
    private BukkitRunnable onTimeTask;
    private LookupService geoip;
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(Chat.class);
        if (chatProvider != null) chat = chatProvider.getProvider();
        return (chat != null);
    }

    @Override
    public void onEnable() {
        this.info = new BukkitPlayerInfo(this);
        try {
            for (Class<?> clazz : getDatabaseClasses()) {
                getDatabase().find(clazz).findRowCount();
            }
        } catch (PersistenceException ex) {
            getLogger().info("Installing database for " + getDescription().getName() + " due to first time usage");
            try {
                installDDL();
            } catch (Exception e) {
                getLogger().warning("Database setup failed. Disabling " + getDescription().getName());
                e.printStackTrace();
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
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

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(
            CountryRow.class,
            IPRow.class,
            IgnoredIPRow.class,
            LogInfoRow.class,
            OnTimeRow.class,
            PlayerCountryAndIPRow.class,
            PlayerIPRow.class,
            PlayerRow.class
            );
    }

    Chat getChat() {
        if (chat == null) {
            if (!setupChat()) return null;
        }
        return chat;
    }

    List<UUID> getOnlineUuids() {
        List<UUID> result = new ArrayList<>();
        for (Player player : getServer().getOnlinePlayers()) result.add(player.getUniqueId());
        return result;
    }

    LookupService getGeoIP() {
        if (geoip == null) {
            try {
                saveResource("GeoIP.dat", false);
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
