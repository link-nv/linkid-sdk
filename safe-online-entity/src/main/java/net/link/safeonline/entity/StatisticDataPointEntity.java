/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.StatisticDataPointEntity.DELETE_WHERE_STATISTIC;
import static net.link.safeonline.entity.StatisticDataPointEntity.DELETE_WHERE_STATISTIC_EXPIRED;
import static net.link.safeonline.entity.StatisticDataPointEntity.QUERY_WHERE_NAME_AND_STATISTIC;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;


@Entity
@Table(name = "statistic_data_point")
@NamedQueries( {
        @NamedQuery(name = DELETE_WHERE_STATISTIC, query = "DELETE FROM StatisticDataPointEntity " + "WHERE statistic = :statistic"),
        @NamedQuery(name = QUERY_WHERE_NAME_AND_STATISTIC, query = "SELECT dp FROM StatisticDataPointEntity "
                + "AS dp WHERE dp.statistic = :statistic " + "AND dp.name = :name"),
        @NamedQuery(name = DELETE_WHERE_STATISTIC_EXPIRED, query = "DELETE FROM StatisticDataPointEntity "
                + "WHERE statistic = :statistic " + "AND creationTime < :ageLimit") })
public class StatisticDataPointEntity implements Serializable {

    private static final long  serialVersionUID               = 1L;

    public static final String DELETE_WHERE_STATISTIC         = "sdp.del";

    public static final String QUERY_WHERE_NAME_AND_STATISTIC = "sdp.stat.name";

    public static final String DELETE_WHERE_STATISTIC_EXPIRED = "sdp.del.exp";

    private long               id;

    private String             name;

    private StatisticEntity    statistic;

    private Date               creationTime;

    private long               x;

    private long               y;

    private long               z;


    public StatisticDataPointEntity() {

        // empty
    }

    public StatisticDataPointEntity(String name, StatisticEntity statistic, Date creationTime, long x, long y, long z) {

        this.name = name;
        this.statistic = statistic;
        this.x = x;
        this.y = y;
        this.z = z;
        this.creationTime = creationTime;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public long getX() {

        return x;
    }

    public void setX(long x) {

        this.x = x;
    }

    public long getY() {

        return y;
    }

    public void setY(long y) {

        this.y = y;
    }

    public long getZ() {

        return z;
    }

    public void setZ(long z) {

        this.z = z;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @ManyToOne
    @JoinColumn(name = "statistic")
    public StatisticEntity getStatistic() {

        return statistic;
    }

    public void setStatistic(StatisticEntity statistic) {

        this.statistic = statistic;
    }

    public Date getCreationTime() {

        return creationTime;
    }

    public void setCreationTime(Date creationTime) {

        this.creationTime = creationTime;
    }


    public interface QueryInterface {

        @UpdateMethod(DELETE_WHERE_STATISTIC)
        void deleteWhereStatistic(@QueryParam("statistic") StatisticEntity statistic);

        @QueryMethod(QUERY_WHERE_NAME_AND_STATISTIC)
        List<StatisticDataPointEntity> listStatisticDataPoints(@QueryParam("name") String name,
                                                               @QueryParam("statistic") StatisticEntity statistic);

        @UpdateMethod(DELETE_WHERE_STATISTIC_EXPIRED)
        void deleteWhereStatisticExpired(@QueryParam("statistic") StatisticEntity statistic, @QueryParam("ageLimit") Date ageLimit);
    }
}
