/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import static net.link.safeonline.entity.audit.ResourceAuditEntity.QUERY_DELETE_WHERE_CONTEXTID;

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
@Table(name = "resource_audit")
@NamedQueries(@NamedQuery(name = QUERY_DELETE_WHERE_CONTEXTID, query = "DELETE "
		+ "FROM ResourceAuditEntity AS record "
		+ "WHERE record.auditContext.id = :contextId"))
public class ResourceAuditEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_DELETE_WHERE_CONTEXTID = "ra.del.id";

	private Long id;

	private AuditContextEntity auditContext;

	private ResourceNameType resourceName;

	private ResourceLevelType resourceLevel;

	private String sourceComponent;

	private String message;

	public ResourceAuditEntity(AuditContextEntity auditContext,
			ResourceNameType resourceName, ResourceLevelType resourceLevel,
			String sourceComponent, String message) {
		this.auditContext = auditContext;
		this.resourceName = resourceName;
		this.resourceLevel = resourceLevel;
		this.sourceComponent = sourceComponent;
		this.message = message;
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
	public ResourceNameType getResourceName() {
		return this.resourceName;
	}

	public void setResourceName(ResourceNameType resourceName) {
		this.resourceName = resourceName;
	}

	@Enumerated(EnumType.STRING)
	public ResourceLevelType getResourceLevel() {
		return this.resourceLevel;
	}

	public void setResourceLevel(ResourceLevelType resourceLevel) {
		this.resourceLevel = resourceLevel;
	}

	@ManyToOne
	public AuditContextEntity getAuditContext() {
		return this.auditContext;
	}

	public void setAuditContext(AuditContextEntity auditContext) {
		this.auditContext = auditContext;
	}

	public String getSourceComponent() {
		return sourceComponent;
	}

	public void setSourceComponent(String sourceComponent) {
		this.sourceComponent = sourceComponent;
	}

	public interface QueryInterface {
		@UpdateMethod(QUERY_DELETE_WHERE_CONTEXTID)
		void deleteRecords(@QueryParam("contextId")
		Long contextId);
	}
}
