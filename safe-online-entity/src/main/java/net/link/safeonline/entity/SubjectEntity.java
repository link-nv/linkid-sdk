/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "subject")
public class SubjectEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String login;

	public SubjectEntity() {
		// required
	}

	public SubjectEntity(String login) {
		this.login = login;
	}

	@Id
	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof SubjectEntity) {
			return false;
		}
		SubjectEntity rhs = (SubjectEntity) obj;
		return new EqualsBuilder().append(this.login, rhs.login).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				"login", this.login).toString();
	}
}
