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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.TaskHistoryEntity.QUERY_DELETE_WHERE_TASK;
import static net.link.safeonline.entity.TaskHistoryEntity.QUERY_DELETE;
import static net.link.safeonline.entity.TaskHistoryEntity.QUERY_DELETE_WHERE_OLDER;

@Entity
@Table(name = "task_history")
@NamedQueries( {
		@NamedQuery(name = QUERY_DELETE_WHERE_TASK, query = "DELETE "
				+ "FROM TaskHistoryEntity AS taskHistory "
				+ "WHERE taskHistory.task = :task"),
		@NamedQuery(name = QUERY_DELETE, query = "DELETE "
				+ "FROM TaskHistoryEntity"),
		@NamedQuery(name = QUERY_DELETE_WHERE_OLDER, query = "DELETE "
				+ "FROM TaskHistoryEntity AS taskHistory "
				+ "WHERE taskHistory.executionDate < :ageLimit") })
public class TaskHistoryEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_DELETE_WHERE_TASK = "the.deltask";

	public static final String QUERY_DELETE = "the.del";

	public static final String QUERY_DELETE_WHERE_OLDER = "the.old";

	private long id;

	private String message;

	private boolean result;

	private Date executionDate;

	private long executionTime;

	private TaskEntity task;

	public TaskHistoryEntity() {
		// empty
	}

	public TaskHistoryEntity(TaskEntity task, String message, boolean result,
			Date startDate, Date endDate) {
		this.task = task;
		this.message = message;
		this.executionDate = startDate;
		this.result = result;
		this.executionTime = endDate.getTime() - startDate.getTime();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getExecutionDate() {
		return this.executionDate;
	}

	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

	public long getExecutionTime() {
		return this.executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isResult() {
		return this.result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	@ManyToOne
	public TaskEntity getTask() {
		return this.task;
	}

	public void setTask(TaskEntity task) {
		this.task = task;
	}

	public static Query createQueryDeleteWhereTask(EntityManager entityManager,
			TaskEntity task) {
		Query query = entityManager.createNamedQuery(QUERY_DELETE_WHERE_TASK);
		query.setParameter("task", task);
		return query;
	}

	public static Query createQueryDelete(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_DELETE);
		return query;
	}

	public static Query createQueryDeleteWhereOlder(
			EntityManager entityManager, long ageInMillis) {
		Query query = entityManager.createNamedQuery(QUERY_DELETE_WHERE_OLDER);
		Date ageLimit = new Date(System.currentTimeMillis() - ageInMillis);
		query.setParameter("ageLimit", ageLimit);
		return query;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).append("task",
				this.task.getName()).append("result", this.result).append(
				"message", this.message).append("date", this.executionDate)
				.append("time", this.executionTime).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof TaskHistoryEntity) {
			return false;
		}
		TaskHistoryEntity rhs = (TaskHistoryEntity) obj;
		return new EqualsBuilder().append(this.id, rhs.id).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.id).toHashCode();
	}

}
