package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "entity")
public class EntityEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String login;

	private String password;

	public EntityEntity() {
		// required
	}

	public EntityEntity(String login, String password) {
		super();
		this.login = login;
		this.password = password;
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
		if (false == obj instanceof EntityEntity) {
			return false;
		}
		EntityEntity rhs = (EntityEntity) obj;
		return new EqualsBuilder().append(this.login, rhs.login).append(
				this.name, rhs.name).append(this.password, rhs.password)
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("login", this.login).append(
				"name", this.name).toString();
	}
}
