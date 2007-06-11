/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.config.ConfigGroupEntity.QUERY_LIST_ALL;

@Entity
@Table(name = "config_group")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM ConfigGroupEntity c") })
public class ConfigGroupEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "cge.list";

	private String name;

	private List<ConfigItemEntity> configItems;

	public ConfigGroupEntity() {
		this(null);
	}

	public ConfigGroupEntity(String name) {
		this.name = name;
		this.configItems = new ArrayList<ConfigItemEntity>();
	}

	@OneToMany(mappedBy = "configGroup", fetch = FetchType.EAGER)
	public List<ConfigItemEntity> getConfigItems() {
		return configItems;
	}

	public void setConfigItems(List<ConfigItemEntity> configItems) {
		this.configItems = configItems;
	}

	@Id
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Query createQueryListAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_LIST_ALL);
		return query;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (false == obj instanceof ConfigGroupEntity) {
			return false;
		}
		ConfigGroupEntity rhs = (ConfigGroupEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}

}
