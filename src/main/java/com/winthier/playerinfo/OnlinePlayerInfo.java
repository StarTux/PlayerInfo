package com.winthier.playerinfo;

import lombok.Data;
import java.util.List;

@Data
public class OnlinePlayerInfo {
    private String world;
    private int x, y, z;
    private int health, maxHealth, foodLevel, saturation, exp, expLevel;
    private List<String> potionEffects;
    private String gameMode;

    public void setLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
