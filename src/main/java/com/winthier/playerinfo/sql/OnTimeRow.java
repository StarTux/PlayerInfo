package com.winthier.playerinfo.sql;

import java.util.List;
import javax.persistence.Column;
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
@Table(name="ontimes",
       uniqueConstraints=@UniqueConstraint(columnNames={"player_id"}))
@Getter
@Setter
public class OnTimeRow {
    private static final int PAGE_LENGTH = 10;
    
    @Id
    private Integer id;

    @Column(nullable = false)
    @OneToOne(optional=false, fetch=FetchType.LAZY)
    private PlayerRow player;

    @Column(nullable = false)
    private Integer seconds;

    @Version
    private Integer version;

    public static List<OnTimeRow> rankOntimes(int page) {
        if (page < 0) throw new IllegalArgumentException("Page cannot be negative");
        return DB.get().find(OnTimeRow.class).orderByDescending("seconds").offset(page * PAGE_LENGTH).limit(PAGE_LENGTH).findList();
    }

    public static int count() {
        return DB.get().find(OnTimeRow.class).findRowCount();
    }

    public static int countPages() {
        return (count() - 1) / PAGE_LENGTH + 1;
    }

    public static OnTimeRow find(PlayerRow player) {
        return DB.get().find(OnTimeRow.class).where().eq("player", player).findUnique();
    }

    public static OnTimeRow findOrCreate(PlayerRow player) {
        OnTimeRow result = find(player);
        if (result == null) {
            result = new OnTimeRow();
            result.setPlayer(player);
            result.setSeconds(0);
            DB.get().save(result);
        }
        return result;
    }

    public static OnTimeRow updateOrCreate(PlayerRow player, int seconds) {
        OnTimeRow result = find(player);
        if (result == null) {
            result = new OnTimeRow();
            result.setPlayer(player);
            result.setSeconds(60);
            DB.get().save(result);
        } else {
            result.setSeconds(result.getSeconds() + seconds);
            DB.get().save(result);
        }
        return result;
    }
}
