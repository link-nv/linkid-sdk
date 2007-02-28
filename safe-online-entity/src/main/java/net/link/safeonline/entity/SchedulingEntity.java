/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.ejb.TimerHandle;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.SchedulingEntity.QUERY_WHERE_NAME;
import static net.link.safeonline.entity.SchedulingEntity.QUERY_WHERE_TIMERHANDLE;
import static net.link.safeonline.entity.SchedulingEntity.QUERY_LIST_ALL;

@Entity
@Table(name = "scheduling")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_NAME, query = "SELECT scheduling "
				+ "FROM SchedulingEntity AS scheduling "
				+ "WHERE scheduling.name = :name"),
		@NamedQuery(name = QUERY_WHERE_TIMERHANDLE, query = "SELECT scheduling "
				+ "FROM SchedulingEntity AS scheduling "
				+ "WHERE scheduling.timerHandle = :timerHandle"),
		@NamedQuery(name = QUERY_LIST_ALL, query = "SELECT scheduling "
				+ "FROM SchedulingEntity AS scheduling") })
public class SchedulingEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_NAME = "sch.name";

	public static final String QUERY_WHERE_TIMERHANDLE = "sch.timer";

	public static final String QUERY_LIST_ALL = "sch.all";

	private String cronExpression;

	private String name;

	private TimerHandle timerHandle;

	private Date fireDate;

	private Collection<TaskEntity> taskEntities = new ArrayList<TaskEntity>();

	public SchedulingEntity() {
		// required
	}

	public SchedulingEntity(String name, String cronExpression,
			TimerHandle timerHandle) {
		this.name = name;
		this.cronExpression = cronExpression;
		this.timerHandle = timerHandle;
	}

	public String getCronExpression() {
		return this.cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Lob
	public TimerHandle getTimerHandle() {
		return this.timerHandle;
	}

	public void setTimerHandle(TimerHandle timerHandle) {
		this.timerHandle = timerHandle;
	}

	@OneToMany(mappedBy = "schedulingEntity")
	public Collection<TaskEntity> getTaskEntities() {
		return this.taskEntities;
	}

	public void setTaskEntities(Collection<TaskEntity> taskEntities) {
		this.taskEntities = taskEntities;
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getFireDate() {
		return this.fireDate;
	}

	public void setFireDate(Date fireDate) {
		this.fireDate = fireDate;
	}

	public void addTaskEntity(TaskEntity taskEntity) {
		ArrayList<TaskEntity> list = (ArrayList<TaskEntity>) this
				.getTaskEntities();
		list.add(taskEntity);
		this.setTaskEntities(list);
	}

	public static Query createQueryWhereName(EntityManager entityManager,
			String name) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_NAME);
		query.setParameter("name", name);
		return query;
	}

	public static Query createQueryWhereTimerHandle(
			EntityManager entityManager, TimerHandle timerHandle) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_TIMERHANDLE);
		query.setParameter("timerHandle", timerHandle);
		return query;
	}

	public static Query createQueryListAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_LIST_ALL);
		return query;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append(
				"cronExpression", this.cronExpression).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof SchedulingEntity) {
			return false;
		}
		SchedulingEntity rhs = (SchedulingEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}
}
