package com.winthier.playerinfo.sql;

import com.avaje.ebean.validation.NotNull;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="players",
       uniqueConstraints=@UniqueConstraint(columnNames={"uuid"}))
@Getter
@Setter
public class PlayerRow {
    @Id
    private Integer id;

    @NotNull
    private UUID uuid;

    @OneToMany(mappedBy="player", fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private List<PlayerIPRow> ips;

    @OneToOne(mappedBy="player", optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private OnTimeRow onTime;

    @OneToOne(mappedBy="player", optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private LogInfoRow logInfo;

    @OneToOne(mappedBy="player", optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private PlayerCountryAndIPRow country;
    
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
