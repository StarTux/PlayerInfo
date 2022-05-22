package com.winthier.playerinfo.sql;

import com.winthier.sql.SQLRow;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Table(name = "players",
       uniqueConstraints = @UniqueConstraint(columnNames = {"uuid"}))
@Getter @Setter
public final class PlayerRow implements SQLRow {
    @Id
    private Integer id;

    @Column(nullable = false)
    private UUID uuid;

    public List<PlayerIPRow> getIps() {
        return DB.get().find(PlayerIPRow.class).where().eq("player", this).findList();
    }

    public OnTimeRow getOnTime() {
        return OnTimeRow.find(this);
    }

    public LogInfoRow getLogInfo() {
        return LogInfoRow.find(this);
    }

    public PlayerCountryAndIPRow getCountry() {
        return PlayerCountryAndIPRow.find(this);
    }

    public static PlayerRow find(UUID uuid) {
        return DB.get().find(PlayerRow.class).where().eq("uuid", uuid).findUnique();
    }

    public static List<PlayerRow> findAll(List<UUID> uuids) {
        return DB.get().find(PlayerRow.class).where().in("uuid", uuids).findList();
    }

    public static PlayerRow findOrCreate(UUID uuid) {
        PlayerRow result = find(uuid);
        if (result == null) {
            result = new PlayerRow();
            result.setUuid(uuid);
            DB.get().save(result);
        }
        return result;
    }
}
