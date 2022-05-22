package com.winthier.playerinfo.sql;

import com.winthier.sql.SQLRow;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Table(name = "daily_ontimes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "day_id"}))
@Getter
@Setter
public class DailyOnTimeRow implements SQLRow {
    private static final int PAGE_LENGTH = 10;

    @Id
    private Integer id;

    @Column(nullable = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private PlayerRow player;
    @Column(nullable = false)
    private int dayId;

    @Column(nullable = false)
    private int seconds;

    public static DailyOnTimeRow find(PlayerRow player, int dayId) {
        return DB.get().find(DailyOnTimeRow.class).where().eq("player", player).eq("day_id", dayId).findUnique();
    }

    public static DailyOnTimeRow findOrCreate(PlayerRow player, int dayId) {
        DailyOnTimeRow result = find(player, dayId);
        if (result == null) {
            result = new DailyOnTimeRow();
            result.setPlayer(player);
            result.setDayId(dayId);
            result.setSeconds(0);
            DB.get().save(result);
        }
        return result;
    }

    public static void updateOrCreate(PlayerRow player, int dayId, int seconds) {
        DailyOnTimeRow result = find(player, dayId);
        if (result == null) {
            result = new DailyOnTimeRow();
            result.setPlayer(player);
            result.setDayId(dayId);
            result.setSeconds(seconds);
            DB.get().insert(result);
        } else {
            result.setSeconds(result.getSeconds() + seconds);
            DB.get().update(result, "seconds");
        }
    }
}
