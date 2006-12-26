/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class AttributePK implements Serializable {

	private static final long serialVersionUID = 1L;

	private String attributeType;

	private String subject;

	public AttributePK() {
		// empty
	}

	public AttributePK(String attributeType, String subject) {
		this.attributeType = attributeType;
		this.subject = subject;
	}

	public String getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
