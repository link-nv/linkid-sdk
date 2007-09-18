/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.SecurityAuditEntity.QUERY_DELETE_WHERE_CONTEXTID;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

@Entity
@Table(name = "security_audit")
@NamedQueries(@NamedQuery(name = QUERY_DELETE_WHERE_CONTEXTID, query = "DELETE "
		+ "FROM ResourceAuditEntity AS record "
		+ "WHERE record.auditContext.id = :contextId"))
public class SecurityAuditEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_DELETE_WHERE_CONTEXTID = "sa.del.id";

	private Long id;

	private AuditContextEntity auditContext;

	private String message;

	private SecurityThreatType securityThreat;

	private String targetPrincipal;

	public SecurityAuditEntity() {
		// empty
	}

	public SecurityAuditEntity(AuditContextEntity auditContext,
			SecurityThreatType securityThreat, String targetPrincipal,
			String message) {
		this.auditContext = auditContext;
		this.securityThreat = securityThreat;
		this.message = message;
		this.targetPrincipal = targetPrincipal;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Enumerated(EnumType.STRING)
	public SecurityThreatType getSecurityThreat() {
		return this.securityThreat;
	}

	public void setSecurityThreat(SecurityThreatType securityThreat) {
		this.securityThreat = securityThreat;
	}

	@ManyToOne
	public AuditContextEntity getAuditContext() {
		return this.auditContext;
	}

	public void setAuditContext(AuditContextEntity auditContext) {
		this.auditContext = auditContext;
	}

	public String getTargetPrincipal() {
		return this.targetPrincipal;
	}

	public void setTargetPrincipal(String targetPrincipal) {
		this.targetPrincipal = targetPrincipal;
	}

	public interface QueryInterface {
		@UpdateMethod(QUERY_DELETE_WHERE_CONTEXTID)
		void deleteRecords(@QueryParam("contextId")
		Long contextId);
	}

}
