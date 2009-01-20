/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.config;

import static net.link.safeonline.entity.config.ConfigGroupEntity.QUERY_LIST_ALL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "config_group")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM ConfigGroupEntity c") })
public class ConfigGroupEntity implements Serializable {

    private static final long     serialVersionUID = 1L;

    public static final String    QUERY_LIST_ALL   = "cge.list";

    private String                name;

    private Set<ConfigItemEntity> configItems;


    public ConfigGroupEntity() {

        this(null);
    }

    public ConfigGroupEntity(String name) {

        this.name = name;
        configItems = new HashSet<ConfigItemEntity>();
    }

    // This has to be a set as Hibernate does not allow a List of List of entities ... :
    // http://www.jboss.com/index.html?module=bb&op=viewtopic&t=82946&postdays=0&postorder=asc&start=10
    @OneToMany(mappedBy = ConfigItemEntity.GROUP_COLUMN_NAME, fetch = FetchType.EAGER)
    public Set<ConfigItemEntity> getConfigItems() {

        return configItems;
    }

    public void setConfigItems(Set<ConfigItemEntity> configItems) {

        this.configItems = configItems;
    }

    // Introduced to easily use in JSF components like ui:repeat or datatable that want ordered collections...
    @Transient
    public List<ConfigItemEntity> getConfigItemsAsList() {

        List<ConfigItemEntity> itemList = new LinkedList<ConfigItemEntity>();
        Iterator<ConfigItemEntity> it = getConfigItems().iterator();
        while (it.hasNext()) {
            itemList.add(it.next());
        }
        return itemList;
    }

    @Id
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
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
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(name).toHashCode();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<ConfigGroupEntity> listConfigGroups();
    }
}
