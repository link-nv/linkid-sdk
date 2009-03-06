/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.config;

import static net.link.safeonline.entity.config.ConfigGroupEntity.QUERY_GET_GROUP;
import static net.link.safeonline.entity.config.ConfigGroupEntity.QUERY_LIST_ALL;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "config_group", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM ConfigGroupEntity c"),
        @NamedQuery(name = QUERY_GET_GROUP, query = "FROM ConfigGroupEntity c WHERE c.name = :name") })
public class ConfigGroupEntity implements Serializable {

    private static final long      serialVersionUID = 1L;

    public static final String     QUERY_LIST_ALL   = "cge.list";
    public static final String     QUERY_GET_GROUP  = "cge.get";

    private String                 name;

    @Transient
    private List<ConfigItemEntity> configItems;

    private int                id;


    public ConfigGroupEntity() {

        this(null);
    }

    public ConfigGroupEntity(String name) {

        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    // TODO: quickfix, better push out transient methods and put login the service bean ...
    @Transient
    public List<ConfigItemEntity> getConfigItems() {

        return configItems;
    }

    @Transient
    public void setConfigItems(List<ConfigItemEntity> configItems) {

        this.configItems = configItems;
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("name", name).toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof ConfigGroupEntity)
            return false;
        ConfigGroupEntity rhs = (ConfigGroupEntity) obj;
        return id == rhs.id;
    }

    @Override
    public int hashCode() {

        return id;
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<ConfigGroupEntity> listConfigGroups();

        @QueryMethod(value = QUERY_GET_GROUP, nullable = true)
        ConfigGroupEntity getConfigGroup(@QueryParam("name") String name);
    }
}
