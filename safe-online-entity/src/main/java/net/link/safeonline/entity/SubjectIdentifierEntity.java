/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Subject Identifier entity. This entity allows us to unambiguously map from an
 * identifier within a certains domain to its subject. For example, within the
 * domain of Belgian eID, we will map from the SHA-1 of the encoded
 * authentication certificate to a subject.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "subject_identifier")
public class SubjectIdentifierEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private SubjectIdentifierPK pk;

	private SubjectEntity subject;

	public SubjectIdentifierEntity() {
		// empty
	}

	public SubjectIdentifierEntity(String domain, String identifier,
			SubjectEntity subject) {
		this.pk = new SubjectIdentifierPK(domain, identifier);
		this.subject = subject;
	}

	@EmbeddedId
	public SubjectIdentifierPK getPk() {
		return pk;
	}

	public void setPk(SubjectIdentifierPK pk) {
		this.pk = pk;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "subject")
	public SubjectEntity getSubject() {
		return this.subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}
}
