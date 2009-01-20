/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.StatisticEntity.QUERY_ALL;
import static net.link.safeonline.entity.StatisticEntity.QUERY_DELETE_WHERE_DOMAIN;
import static net.link.safeonline.entity.StatisticEntity.QUERY_WHERE_APPLICATION;
import static net.link.safeonline.entity.StatisticEntity.QUERY_WHERE_DOMAIN;
import static net.link.safeonline.entity.StatisticEntity.QUERY_WHERE_NAME_DOMAIN_AND_APPLICATION;
import static net.link.safeonline.entity.StatisticEntity.QUERY_WHERE_NAME_DOMAIN_AND_NULL;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;


@Entity
@Table(name = "statistic", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "application" }))
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_NAME_DOMAIN_AND_APPLICATION, query = "SELECT Statistic " + "FROM StatisticEntity AS Statistic "
                + "WHERE Statistic.name = :name AND Statistic.application = :application " + "AND Statistic.domain = :domain"),
        @NamedQuery(name = QUERY_WHERE_NAME_DOMAIN_AND_NULL, query = "SELECT Statistic " + "FROM StatisticEntity AS Statistic "
                + "WHERE Statistic.name = :name AND Statistic.application IS NULL " + "AND Statistic.domain = :domain"),
        @NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT Statistic " + "FROM StatisticEntity AS Statistic "
                + "WHERE Statistic.application = :application"),
        @NamedQuery(name = QUERY_WHERE_DOMAIN, query = "SELECT Statistic " + "FROM StatisticEntity AS Statistic "
                + "WHERE Statistic.domain = :domain"),
        @NamedQuery(name = QUERY_ALL, query = "SELECT Statistic " + "FROM StatisticEntity AS Statistic"),
        @NamedQuery(name = QUERY_DELETE_WHERE_DOMAIN, query = "DELETE " + "FROM StatisticEntity AS Statistic "
                + "WHERE Statistic.domain = :domain") })
public class StatisticEntity implements Serializable {

    private static final long              serialVersionUID                        = 1L;

    public static final String             QUERY_WHERE_NAME_DOMAIN_AND_APPLICATION = "stat.naa";

    public static final String             QUERY_WHERE_NAME_DOMAIN_AND_NULL        = "stat.nan";

    public static final String             QUERY_WHERE_APPLICATION                 = "stat.app";

    public static final String             QUERY_ALL                               = "stat.all";

    public static final String             QUERY_DELETE_WHERE_DOMAIN               = "stat.deldomain";

    public static final String             QUERY_WHERE_DOMAIN                      = "stat.domain";

    private long                           id;

    private String                         name;

    private String                         domain;

    private ApplicationEntity              application;

    private List<StatisticDataPointEntity> statisticDataPoints;

    private Date                           creationTime;


    public StatisticEntity() {

        this(null, null, null, null);
    }

    public StatisticEntity(String name, String domain, ApplicationEntity application, Date creationTime) {

        this.name = name;
        this.domain = domain;
        this.application = application;
        this.creationTime = creationTime;
        statisticDataPoints = new LinkedList<StatisticDataPointEntity>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public Date getCreationTime() {

        return creationTime;
    }

    public void setCreationTime(Date creationTime) {

        this.creationTime = creationTime;
    }

    @OneToMany(mappedBy = "statistic")
    public List<StatisticDataPointEntity> getStatisticDataPoints() {

        return statisticDataPoints;
    }

    public void setStatisticDataPoints(List<StatisticDataPointEntity> statisticDataPoints) {

        this.statisticDataPoints = statisticDataPoints;
    }

    @ManyToOne
    @JoinColumn(name = "application")
    public ApplicationEntity getApplication() {

        return application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDomain() {

        return domain;
    }

    public void setDomain(String domain) {

        this.domain = domain;
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_ALL)
        List<StatisticEntity> listStatistics();

        @QueryMethod(QUERY_WHERE_DOMAIN)
        List<StatisticEntity> listStatistics(@QueryParam("domain") String domain);

        @QueryMethod(QUERY_WHERE_APPLICATION)
        List<StatisticEntity> listStatistics(@QueryParam("application") ApplicationEntity application);

        @QueryMethod(QUERY_WHERE_NAME_DOMAIN_AND_APPLICATION)
        StatisticEntity findStatisticWhereNameDomainAndApplication(@QueryParam("name") String name, @QueryParam("domain") String domain,
                                                                   @QueryParam("application") ApplicationEntity application);

        @QueryMethod(QUERY_WHERE_NAME_DOMAIN_AND_NULL)
        StatisticEntity findStatisticWhereNameAndDomain(@QueryParam("name") String name, @QueryParam("domain") String domain);

        @UpdateMethod(QUERY_DELETE_WHERE_DOMAIN)
        void deleteWhereDomain(@QueryParam("domain") String domain);
    }

}
