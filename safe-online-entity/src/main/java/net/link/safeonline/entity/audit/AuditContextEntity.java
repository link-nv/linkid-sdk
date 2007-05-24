/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.audit;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Audit Context JPA entity. This entity is kind of empty. Basically we're only
 * interested in the automagically generated audit context Id sequence.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "audit_context")
public class AuditContextEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	public AuditContextEntity() {
		// empty
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
