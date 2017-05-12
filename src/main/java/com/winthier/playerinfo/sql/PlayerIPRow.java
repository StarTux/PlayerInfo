package com.winthier.playerinfo.sql;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="player_ips",
       uniqueConstraints=@UniqueConstraint(columnNames={"player_id", "ip_id"}))
@Getter
@Setter
public class PlayerIPRow {
    @Id
    private Integer id;

    @Column(nullable = false)
    @ManyToOne(fetch=FetchType.LAZY)
    private PlayerRow player;

    @Column(nullable = false)
    @ManyToOne
    private IPRow ip;

    public static PlayerIPRow forId(int id) {
        return DB.get().find(PlayerIPRow.class).where().idEq(id).findUnique();
    }

    public static List<PlayerIPRow> find(List<IPRow> ips) {
        return DB.get().find(PlayerIPRow.class).where().in("ip", ips).findList();
    }

    public static PlayerIPRow find(PlayerRow player, IPRow ip) {
        if (player == null) throw new NullPointerException("Player row cannot be null");
        if (ip == null) throw new NullPointerException("IP row cannot be null");
        List<PlayerIPRow> list = DB.get().find(PlayerIPRow.class).where().eq("player", player).eq("ip", ip).findList();
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static PlayerIPRow findOrCreate(PlayerRow player, IPRow ip) {
        PlayerIPRow result = find(player, ip);
        if (result == null) {
            result = new PlayerIPRow();
            result.setPlayer(player);
            result.setIp(ip);
            DB.get().save(result);
        }
        return result;
    }
}
