/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.AuditAuditEntity.QUERY_DELETE_WHERE_CONTEXTID;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

/**
 * Audit entity about audit system itself.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "audit_audit")
@NamedQueries(@NamedQuery(name = QUERY_DELETE_WHERE_CONTEXTID, query = "DELETE "
		+ "FROM AuditAuditEntity AS record "
		+ "WHERE record.auditContext.id = :contextId"))
public class AuditAuditEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_DELETE_WHERE_CONTEXTID = "aa.del.id";

	private Long id;

	private AuditContextEntity auditContext;

	private String message;

	public AuditAuditEntity() {
		// empty
	}

	public AuditAuditEntity(AuditContextEntity auditContext, String message) {
		this.auditContext = auditContext;
		this.message = message;
	}

	public AuditAuditEntity(String message) {
		this(null, message);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	public AuditContextEntity getAuditContext() {
		return this.auditContext;
	}

	public void setAuditContext(AuditContextEntity auditContext) {
		this.auditContext = auditContext;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public interface QueryInterface {
		@UpdateMethod(QUERY_DELETE_WHERE_CONTEXTID)
		void deleteRecords(@QueryParam("contextId")
		Long contextId);
	}

}
