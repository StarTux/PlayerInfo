package com.winthier.playerinfo.sql;

import com.winthier.sql.SQLRow;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Table(name = "ips",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ip"}))
@Getter @Setter
public class IPRow implements SQLRow {
    @Id
    private Integer id;

    @Column(nullable = false, length = 40)
    private String ip;

    public static IPRow forId(int id) {
        return DB.get().find(IPRow.class).where().idEq(id).findUnique();
    }

    public static IPRow find(String ip) {
        return DB.get().find(IPRow.class).where().eq("ip", ip).findUnique();
    }

    public static IPRow findOrCreate(String ip) {
        IPRow result = find(ip);
        if (result == null) {
            result = new IPRow();
            result.setIp(ip);
            DB.get().save(result);
        }
        return result;
    }
}
