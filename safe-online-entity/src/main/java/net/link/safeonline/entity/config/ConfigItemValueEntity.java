/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.config;

import static net.link.safeonline.entity.config.ConfigItemValueEntity.QUERY_LIST_VALUES;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "config_item_value")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_VALUES, query = "FROM ConfigItemValueEntity c WHERE c.configItem = :configItem") })
public class ConfigItemValueEntity implements Serializable {

    private static final long  serialVersionUID  = 1L;

    public static final String QUERY_LIST_VALUES = "ciev.list";

    private long               id;

    private String             value;

    private ConfigItemEntity   configItem;


    public ConfigItemValueEntity() {

        // empty
    }

    public ConfigItemValueEntity(ConfigItemEntity configItem, String value) {

        this.configItem = configItem;
        this.value = value;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    @ManyToOne
    public ConfigItemEntity getConfigItem() {

        return this.configItem;
    }

    public void setConfigItem(ConfigItemEntity configItem) {

        this.configItem = configItem;
    }

    public String getValue() {

        return this.value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("id", this.id).append("value", this.value).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof ConfigItemValueEntity)
            return false;
        ConfigItemValueEntity rhs = (ConfigItemValueEntity) obj;
        return new EqualsBuilder().append(this.id, rhs.id).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.id).append(this.value).toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_VALUES)
        List<ConfigItemValueEntity> listConfigItemValues(@QueryParam("configItem") ConfigItemEntity configItem);

    }
}
