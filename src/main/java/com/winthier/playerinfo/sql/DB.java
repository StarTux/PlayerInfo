package com.winthier.playerinfo.sql;

import com.winthier.playerinfo.PlayerInfo;
import com.winthier.sql.SQLDatabase;

class DB {
    static SQLDatabase get() {
        return PlayerInfo.getInstance().getDatabase();
    }
}
