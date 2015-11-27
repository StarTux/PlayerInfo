package com.winthier.playerinfo.sql;

import com.avaje.ebean.validation.NotNull;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="player_countries",
       uniqueConstraints=@UniqueConstraint(columnNames={"player_id"}))
@Getter
@Setter
public class PlayerCountryAndIPRow {
    @Id
    private Integer id;

    @NotNull
    @OneToOne(fetch=FetchType.LAZY)
    private PlayerRow player;

    @ManyToOne
    private IPRow ip;

    @ManyToOne
    private CountryRow country;

    @Version
    private Integer version;

    public static PlayerCountryAndIPRow forId(int id) {
        return DB.get().find(PlayerCountryAndIPRow.class).where().idEq(id).findUnique();
    }

    public static PlayerCountryAndIPRow find(PlayerRow player) {
        return DB.get().find(PlayerCountryAndIPRow.class).where().eq("player", player).findUnique();
    }

    public static PlayerCountryAndIPRow findOrCreate(PlayerRow player) {
        PlayerCountryAndIPRow result = find(player);
        if (result == null) {
            result = new PlayerCountryAndIPRow();
            result.setPlayer(player);
            DB.get().save(result);
        }
        return result;
    }

    public static PlayerCountryAndIPRow updateOrCreate(PlayerRow player, IPRow ip, CountryRow country) {
        PlayerCountryAndIPRow result = find(player);
        if (result == null) {
            result = new PlayerCountryAndIPRow();
            result.setPlayer(player);
        }
        if (ip != null) result.setIp(ip);
        if (country != null) result.setCountry(country);
        DB.get().save(result);
        return result;
    }
}