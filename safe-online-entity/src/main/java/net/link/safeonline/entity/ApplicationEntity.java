package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_ALL;

@Entity
@Table(name = "application")
@NamedQueries(@NamedQuery(name = QUERY_WHERE_ALL, query = "FROM ApplicationEntity"))
public class ApplicationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_WHERE_ALL = "app.all";

	String name;

	String description;

	boolean allowUserSubscription;

	boolean removable;

	public ApplicationEntity() {
		// empty
	}

	public ApplicationEntity(String name) {
		this(name, true);
	}

	public ApplicationEntity(String name, String description) {
		this(name, description, true, true);
	}

	public ApplicationEntity(String name, boolean allowUserSubscription) {
		this(name, true, true);
	}

	public ApplicationEntity(String name, boolean allowUserSubscription,
			boolean removable) {
		this(name, null, allowUserSubscription, removable);
	}

	public ApplicationEntity(String name, String description,
			boolean allowUserSubscription, boolean removable) {
		this.name = name;
		this.description = description;
		this.allowUserSubscription = allowUserSubscription;
		this.removable = removable;
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

	public boolean isAllowUserSubscription() {
		return this.allowUserSubscription;
	}

	public void setAllowUserSubscription(boolean allowUserSubscription) {
		this.allowUserSubscription = allowUserSubscription;
	}

	public boolean isRemovable() {
		return this.removable;
	}

	public void setRemovable(boolean removable) {
		this.removable = removable;
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
				this.description, rhs.description).append(
				this.allowUserSubscription, rhs.allowUserSubscription).append(
				this.removable, rhs.removable).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append(
				"name", this.name).append("description", this.description)
				.append("allowUserSubscription", this.allowUserSubscription)
				.append("removable", this.removable).toString();
	}

	public static Query createQueryAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_ALL);
		return query;
	}
}
