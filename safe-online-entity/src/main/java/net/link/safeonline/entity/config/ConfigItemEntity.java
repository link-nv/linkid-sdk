/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.config;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static net.link.safeonline.entity.config.ConfigItemEntity.QUERY_LIST_ALL;

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
		return this.configGroup;
	}

	public void setConfigGroup(ConfigGroupEntity configGroup) {
		this.configGroup = configGroup;
	}

	@Id
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
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
		if (false == obj instanceof ConfigItemEntity) {
			return false;
		}
		ConfigItemEntity rhs = (ConfigItemEntity) obj;
		return new EqualsBuilder().append(this.name, rhs.name).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.name).toHashCode();
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_ALL)
		List<ConfigItemEntity> listConfigItems();
	}
}
