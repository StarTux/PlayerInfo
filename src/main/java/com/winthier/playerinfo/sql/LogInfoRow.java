package com.winthier.playerinfo.sql;

import com.avaje.ebean.validation.NotNull;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="loginfos",
       uniqueConstraints=@UniqueConstraint(columnNames={"player_id"}))
@Getter
@Setter
public class LogInfoRow {
    private static final int PAGE_LENGTH = 10;
    public static enum Data {
        FIRST("First"),
        LAST("Last");
        public final String human;
        Data(String human) { this.human = human; }
    }
    
    @Id
    private Integer id;

    @NotNull @OneToOne(optional=false, fetch=FetchType.LAZY)
    private PlayerRow player;

    @NotNull
    private Date firstLog;

    @NotNull
    private Date lastLog;

    @Version
    private Integer version;

    public static List<LogInfoRow> rankLogins(Data data, int page) {
        if (data == null) throw new NullPointerException("Data cannot be null");
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");
        String orderBy = data == Data.FIRST ? "first_log desc" : "last_log desc";
        return DB.get().find(LogInfoRow.class).orderBy(orderBy).setFirstRow(page * PAGE_LENGTH).setMaxRows(PAGE_LENGTH).findList();
    }

    public static int count() {
        return DB.get().find(LogInfoRow.class).findRowCount();
    }

    public static int countPages() {
        return (count() - 1) / PAGE_LENGTH + 1;
    }

    public static LogInfoRow find(PlayerRow player) {
        return DB.get().find(LogInfoRow.class).where().eq("player", player).findUnique();
    }

    public static LogInfoRow findOrCreate(PlayerRow player) {
        LogInfoRow result = find(player);
        if (result == null) {
            result = new LogInfoRow();
            result.setPlayer(player);
            result.setFirstLog(new Date());
            result.setLastLog(new Date());
            DB.get().save(result);
        }
        return result;
    }

    public static LogInfoRow updateOrCreate(PlayerRow player) {
        LogInfoRow result = find(player);
        if (result == null) {
            result = new LogInfoRow();
            result.setPlayer(player);
            result.setFirstLog(new Date());
            result.setLastLog(new Date());
            DB.get().save(result);
        } else {
            result.setLastLog(new Date());
            DB.get().save(result);
        }
        return result;
    }
}
