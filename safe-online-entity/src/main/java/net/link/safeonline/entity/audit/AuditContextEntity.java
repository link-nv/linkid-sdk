/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.AuditContextEntity.QUERY_LIST_ALL;

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
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "SELECT context FROM AuditContextEntity as context WHERE context.time < :ageLimit") })
public class AuditContextEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "ac.all";

	private Long id;

	private Date time;

	public AuditContextEntity() {
		time = new Date();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIME)
	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_ALL)
		List<AuditContextEntity> listContexts(@QueryParam("ageLimit")
		Date ageLimit);
	}
}