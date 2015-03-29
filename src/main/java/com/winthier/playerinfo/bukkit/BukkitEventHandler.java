package com.winthier.playerinfo.bukkit;

import com.winthier.playerinfo.PlayerInfo;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

@RequiredArgsConstructor
class BukkitEventHandler implements Listener {
    private final BukkitPlayerInfoPlugin plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final String ip = event.getAddress().toString().substring(1);
        PlayerInfo.getInstance().onPlayerLogin(uuid, ip);
    }
}
