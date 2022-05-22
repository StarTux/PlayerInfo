package com.winthier.playerinfo.sql;

import com.winthier.playerinfo.PlayerInfo;
import com.winthier.sql.SQLDatabase;

final class DB {
    protected static SQLDatabase get() {
        return PlayerInfo.getInstance().getDatabase();
    }

    private DB() { }
}
