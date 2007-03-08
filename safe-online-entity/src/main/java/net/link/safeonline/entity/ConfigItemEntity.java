/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.ConfigItemEntity.QUERY_LIST_ALL;

@Entity
@Table(name = "config_item")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM ConfigItemEntity c") })
public class ConfigItemEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "cie.list";

	private String name;

	private String value;

	private ConfigGroupEntity configGroup;

	public ConfigItemEntity() {
		// empty
	}

	public ConfigItemEntity(String name, String value,
			ConfigGroupEntity configGroup) {
		this.name = name;
		this.value = value;
		this.configGroup = configGroup;
	}

	@ManyToOne
	public ConfigGroupEntity getConfigGroup() {
		return configGroup;
	}

	public void setConfigGroup(ConfigGroupEntity configGroup) {
		this.configGroup = configGroup;
	}

	@Id
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
		if (false == obj instanceof TaskEntity) {
			return false;
		}
		ConfigItemEntity rhs = (ConfigItemEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}

}
