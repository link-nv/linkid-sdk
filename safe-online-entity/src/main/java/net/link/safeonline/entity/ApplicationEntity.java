package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "application")
public class ApplicationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	String name;

	String description;

	public ApplicationEntity() {
		// empty
	}

	public ApplicationEntity(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof ApplicationEntity) {
			return false;
		}
		ApplicationEntity rhs = (ApplicationEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).append(
				this.description, rhs.description).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append(
				"description", this.description).toString();
	}
}
