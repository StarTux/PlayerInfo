package com.winthier.playerinfo;

import java.util.List;
import lombok.Data;

@Data
public final class OnlinePlayerInfo {
    private String world;
    private int x;
    private int y;
    private int z;
    private int health;
    private int maxHealth;
    private int foodLevel;
    private float saturation;
    private float exhaustion;
    private int exp;
    private int expPerc;
    private int expLevel;
    private List<String> potionEffects;
    private String gameMode;

    public void setLocation(String newWorld, int newX, int newY, int newZ) {
        this.world = newWorld;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
    }
}
