/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static net.link.safeonline.entity.StatisticDataPointEntity.DELETE_WHERE_STATISTIC;

@Entity
@Table(name = "statistic_data_point", uniqueConstraints = @UniqueConstraint(columnNames = {
		"name", "statistic" }))
@NamedQueries( { @NamedQuery(name = DELETE_WHERE_STATISTIC, query = "DELETE FROM StatisticDataPointEntity "
		+ "WHERE statistic = :statistic") })
public class StatisticDataPointEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DELETE_WHERE_STATISTIC = "sdp.del";

	private long id;

	private String name;

	private StatisticEntity statistic;

	private Date creationTime;

	private long x;

	private long y;

	private long z;

	public StatisticDataPointEntity() {
		// empty
	}

	public StatisticDataPointEntity(String name, StatisticEntity statistic,
			Date creationTime, long x, long y, long z) {
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
		return this.id;
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
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne
	@JoinColumn(name = "statistic")
	public StatisticEntity getStatistic() {
		return this.statistic;
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

	public static Query createQueryDeleteWhereStatistic(
			EntityManager entityManager, StatisticEntity statistic) {
		Query query = entityManager.createNamedQuery(DELETE_WHERE_STATISTIC);
		query.setParameter("statistic", statistic);
		return query;
	}

}
