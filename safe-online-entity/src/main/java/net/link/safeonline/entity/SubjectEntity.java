/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static net.link.safeonline.entity.SubjectEntity.QUERY_ALL;

@Entity
@Table(name = "subject")
@NamedQueries( { @NamedQuery(name = QUERY_ALL, query = "SELECT login FROM SubjectEntity") })
public class SubjectEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_ALL = "sub.all";

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

	public interface QueryInterface {
		@QueryMethod(QUERY_ALL)
		List<String> listUsers();
	}
}
