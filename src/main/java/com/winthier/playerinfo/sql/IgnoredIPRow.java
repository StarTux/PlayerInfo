package com.winthier.playerinfo.sql;

import com.avaje.ebean.validation.NotNull;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="ignored_ips",
       uniqueConstraints=@UniqueConstraint(columnNames={"ip_id"}))
@Getter
@Setter
public class IgnoredIPRow {
    @Id
    private Integer id;

    @NotNull
    @OneToOne(optional=false, fetch=FetchType.LAZY)
    private IPRow ip;

    public static List<IgnoredIPRow> findAll() {
        return DB.get().find(IgnoredIPRow.class).findList();
    }

    public static IgnoredIPRow find(IPRow ip) {
        if (ip == null) throw new NullPointerException("IP row cannot be null");
        return DB.get().find(IgnoredIPRow.class).where().eq("ip", ip).findUnique();
    }

    public static IgnoredIPRow findOrCreate(IPRow ip) {
        IgnoredIPRow result = find(ip);
        if (result == null) {
            result = new IgnoredIPRow();
            result.setIp(ip);
            DB.get().save(result);
        }
        return result;
    }

    public static boolean delete(IPRow ip) {
        IgnoredIPRow row = find(ip);
        if (row == null) return false;
        DB.get().delete(row);
        return true;
    }
}
