package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity
@Table(name = "entity")
public class SubjectEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String login;

	private String password;

	public SubjectEntity() {
		// required
	}

	public SubjectEntity(String login, String password, String name) {
		this.login = login;
		this.password = password;
		this.name = name;
	}

	public SubjectEntity(String login, String password) {
		this(login, password, null);
	}

	@Id
	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
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
		return new EqualsBuilder().append(this.login, rhs.login).append(
				this.name, rhs.name).append(this.password, rhs.password)
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				"login", this.login).append("name", this.name).toString();
	}
}
