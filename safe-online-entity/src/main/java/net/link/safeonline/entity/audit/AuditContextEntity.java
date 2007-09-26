/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.AuditContextEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.audit.AuditContextEntity.QUERY_LIST_ALL_LAST;
import static net.link.safeonline.entity.audit.AuditContextEntity.QUERY_LIST_OLD;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.ToStringBuilder;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

/**
 * Audit Context JPA entity. This entity is kind of empty. Basically we're only
 * interested in the automagically generated audit context Id sequence.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "audit_context")
@NamedQueries( {
		@NamedQuery(name = QUERY_LIST_OLD, query = "SELECT context FROM AuditContextEntity as context WHERE context.creationTime < :ageLimit"),
		@NamedQuery(name = QUERY_LIST_ALL_LAST, query = "SELECT context FROM AuditContextEntity as context ORDER BY context.creationTime DESC"),
		@NamedQuery(name = QUERY_LIST_ALL, query = "FROM AuditContextEntity ") })
public class AuditContextEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "ac.all";

	public static final String QUERY_LIST_ALL_LAST = "ac.all.last";

	public static final String QUERY_LIST_OLD = "ac.old";

	private Long id;

	private Date creationTime;

	public AuditContextEntity() {
		this.creationTime = new Date();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date time) {
		this.creationTime = time;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", this.id).toString();
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_OLD)
		List<AuditContextEntity> listContextsOlderThen(@QueryParam("ageLimit")
		Date ageLimit);

		@QueryMethod(QUERY_LIST_ALL)
		List<AuditContextEntity> listContexts();

		@QueryMethod(QUERY_LIST_ALL_LAST)
		List<AuditContextEntity> listLastContexts();
	}
}