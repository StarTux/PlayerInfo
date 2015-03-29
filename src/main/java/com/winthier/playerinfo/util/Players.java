package com.winthier.playerinfo.util;

import com.winthier.playercache.PlayerCache;
import com.winthier.playerinfo.PlayerInfoException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Players {
    public static String getName(UUID uuid) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        String result = PlayerCache.nameForUuid(uuid);
        if (result == null) return "Player";
        return result;
    }

    public static List<String> getNames(List<UUID> uuids) {
        List<String> result = new ArrayList<>(uuids.size());
        for (UUID uuid : uuids) {
            result.add(getName(uuid));
        }
        return result;
    }

    public static UUID getUuid(String name) {
        UUID result = PlayerCache.uuidForName(name);
        if (result == null) throw new PlayerInfoException("Player not found: " + name);
        return result;
    }

    public static List<UUID> getUuids(List<String> names) {
        List<UUID> result = new ArrayList<>(names.size());
        for (String name : names) result.add(getUuid(name));
        return result;
    }
}
