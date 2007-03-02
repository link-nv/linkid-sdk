/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.TaskEntity.QUERY_WHERE_JNDINAME;
import static net.link.safeonline.entity.TaskEntity.QUERY_LIST_ALL;

@Entity
@Table(name = "task")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_JNDINAME, query = "SELECT task "
				+ "FROM TaskEntity AS task "
				+ "WHERE task.jndiName = :jndiName"),
		@NamedQuery(name = QUERY_LIST_ALL, query = "SELECT task "
				+ "FROM TaskEntity AS task") })
public class TaskEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_JNDINAME = "task.jndiName";

	public static final String QUERY_LIST_ALL = "task.all";

	private String jndiName;

	private String name;

	private SchedulingEntity scheduling;

	public TaskEntity() {
		// required
	}

	public TaskEntity(String jndiName, String name,
			SchedulingEntity schedulingEntity) {
		this.name = name;
		this.jndiName = jndiName;
		this.scheduling = schedulingEntity;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Id
	public String getJndiName() {
		return this.jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	@ManyToOne
	public SchedulingEntity getScheduling() {
		return this.scheduling;
	}

	public void setScheduling(SchedulingEntity schedulingEntity) {
		this.scheduling = schedulingEntity;
	}

	public static Query createQueryWhereJndiName(EntityManager entityManager,
			String jndiName) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_JNDINAME);
		query.setParameter("jndiName", jndiName);
		return query;
	}

	public static Query createQueryListAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_LIST_ALL);
		return query;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("jndiName", this.jndiName)
				.append("name", this.name).append("schedulingEntity",
						this.scheduling.getName()).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof TaskEntity) {
			return false;
		}
		TaskEntity rhs = (TaskEntity) obj;
		return new EqualsBuilder().append(this.jndiName, rhs.jndiName)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.jndiName).toHashCode();
	}

}
