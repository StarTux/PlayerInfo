package com.winthier.playerinfo.sql;

import com.avaje.ebean.validation.Length;
import com.avaje.ebean.validation.NotEmpty;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="ips",
       uniqueConstraints=@UniqueConstraint(columnNames={"ip"}))
@Getter
@Setter
public class IPRow {
    @Id
    private Integer id;

    @NotEmpty
    @Length(max=40)
    private String ip;

    @OneToMany(mappedBy="ip", fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private List<PlayerIPRow> playerIps;

    @OneToOne(mappedBy="ip", optional=true, fetch=FetchType.LAZY, cascade=CascadeType.REMOVE)
    private IgnoredIPRow ignored;

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
