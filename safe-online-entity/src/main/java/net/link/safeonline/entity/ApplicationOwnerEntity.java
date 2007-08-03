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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.ApplicationOwnerEntity.QUERY_WHERE_ALL;
import static net.link.safeonline.entity.ApplicationOwnerEntity.QUERY_WHERE_ADMIN;

@Entity
@Table(name = "application_owner")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_ALL, query = "FROM ApplicationOwnerEntity"),
		@NamedQuery(name = QUERY_WHERE_ADMIN, query = "SELECT applicationOwner "
				+ "FROM ApplicationOwnerEntity AS applicationOwner "
				+ "WHERE applicationOwner.admin = :admin") })
public class ApplicationOwnerEntity implements Serializable {

	public static final String QUERY_WHERE_ALL = "owner.all";

	public static final String QUERY_WHERE_ADMIN = "owner.admin";

	private static final long serialVersionUID = 1L;

	private String name;

	private SubjectEntity admin;

	private List<ApplicationEntity> applications;

	public ApplicationOwnerEntity() {
		// empty
	}

	public ApplicationOwnerEntity(String name, SubjectEntity admin) {
		this.name = name;
		this.admin = admin;
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToOne(optional = false)
	public SubjectEntity getAdmin() {
		return this.admin;
	}

	public void setAdmin(SubjectEntity admin) {
		this.admin = admin;
	}

	@OneToMany(mappedBy = "applicationOwner")
	@OrderBy("name")
	public List<ApplicationEntity> getApplications() {
		return this.applications;
	}

	public void setApplications(List<ApplicationEntity> applications) {
		this.applications = applications;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ApplicationOwnerEntity rhs = (ApplicationOwnerEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(this.name).toString();
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_WHERE_ALL)
		List<ApplicationOwnerEntity> listApplicationOwners();

		@QueryMethod(QUERY_WHERE_ADMIN)
		ApplicationOwnerEntity getApplicationOwner(@QueryParam("admin")
		SubjectEntity adminSubject);
	}
}
