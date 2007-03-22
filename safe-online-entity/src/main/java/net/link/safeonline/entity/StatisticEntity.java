/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static net.link.safeonline.entity.StatisticEntity.QUERY_WHERE_NAME_AND_APPLICATION;
import static net.link.safeonline.entity.StatisticEntity.QUERY_WHERE_NAME_AND_NULL;

@Entity
@Table(name = "statistic", uniqueConstraints = @UniqueConstraint(columnNames = {
		"name", "application" }))
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_NAME_AND_APPLICATION, query = "SELECT Statistic "
				+ "FROM StatisticEntity AS Statistic "
				+ "WHERE Statistic.name = :name AND Statistic.application = :application"),
		@NamedQuery(name = QUERY_WHERE_NAME_AND_NULL, query = "SELECT Statistic "
				+ "FROM StatisticEntity AS Statistic "
				+ "WHERE Statistic.name = :name AND Statistic.application IS NULL") })
public class StatisticEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_NAME_AND_APPLICATION = "stat.naa";

	public static final String QUERY_WHERE_NAME_AND_NULL = "stat.nan";

	private long id;

	private String name;

	private ApplicationEntity application;

	private List<StatisticDataPointEntity> statisticDataPoints;

	private Date creationTime;

	public StatisticEntity() {
		this(null, null, null);
	}

	public StatisticEntity(String name, ApplicationEntity application,
			Date creationTime) {
		this.name = name;
		this.application = application;
		this.creationTime = creationTime;
		this.statisticDataPoints = new LinkedList<StatisticDataPointEntity>();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return this.id;
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

	@OneToMany(mappedBy = "statistic", cascade = CascadeType.REMOVE)
	public List<StatisticDataPointEntity> getStatisticDataPoints() {
		return statisticDataPoints;
	}

	public void setStatisticDataPoints(
			List<StatisticDataPointEntity> statisticDataPoints) {
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

	public static Query createQueryWhereNameAndApplication(
			EntityManager entityManager, String name,
			ApplicationEntity application) {
		Query query = null;
		if (application == null) {
			query = entityManager.createNamedQuery(QUERY_WHERE_NAME_AND_NULL);
			query.setParameter("name", name);
			return query;
		}
		query = entityManager
				.createNamedQuery(QUERY_WHERE_NAME_AND_APPLICATION);

		query.setParameter("name", name);
		query.setParameter("application", application);
		return query;
	}

}
