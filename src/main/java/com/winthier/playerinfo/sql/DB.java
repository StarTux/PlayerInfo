package com.winthier.playerinfo.sql;

import com.avaje.ebean.EbeanServer;
import com.winthier.playerinfo.PlayerInfo;

class DB {
    static EbeanServer get() {
        return PlayerInfo.getInstance().getDatabase();
    }
}
