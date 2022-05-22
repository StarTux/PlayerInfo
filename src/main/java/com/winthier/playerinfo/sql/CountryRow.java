package com.winthier.playerinfo.sql;

import com.winthier.sql.SQLRow;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Table(name = "countries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"country"}))
@Getter @Setter
public class CountryRow implements SQLRow {
    public static final int LENGTH = 40;

    @Id
    private Integer id;

    @Column(nullable = false, length = LENGTH)
    private String country;

    public static CountryRow find(String country) {
        if (country == null) throw new NullPointerException("Country cannot be null");
        return DB.get().find(CountryRow.class).where().eq("country", country).findUnique();
    }

    public static CountryRow findOrCreate(String country) {
        if (country == null) throw new NullPointerException("Country cannot be null");
        if (country.length() > LENGTH) country = country.substring(0, LENGTH);
        CountryRow row = find(country);
        if (row == null) {
            row = new CountryRow();
            row.setCountry(country);
            DB.get().save(row);
        }
        return row;
    }
}
