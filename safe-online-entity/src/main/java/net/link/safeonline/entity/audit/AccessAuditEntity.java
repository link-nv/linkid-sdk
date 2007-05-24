/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 * Access Audit entity.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "access_audit")
public class AccessAuditEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private AuditContextEntity auditContext;

	private String operation;

	private OperationStateType operationState;

	private String principal;

	private Date eventDate;

	public AccessAuditEntity() {
		// empty
	}

	public AccessAuditEntity(AuditContextEntity auditContext, String operation,
			OperationStateType operationState, String principal) {
		this.auditContext = auditContext;
		this.operation = operation;
		this.operationState = operationState;
		this.principal = principal;
		this.eventDate = new Date();
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

	@Column(nullable = false)
	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getPrincipal() {
		return this.principal;
	}

	public void setPrincipal(String principal) {
		this.principal = principal;
	}

	@Enumerated(EnumType.ORDINAL)
	public OperationStateType getOperationState() {
		return this.operationState;
	}

	public void setOperationState(OperationStateType operationState) {
		this.operationState = operationState;
	}

	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
}
