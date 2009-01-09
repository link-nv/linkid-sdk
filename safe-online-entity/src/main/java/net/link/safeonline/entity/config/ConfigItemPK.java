/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity.config;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Embeddable
public class ConfigItemPK implements Serializable {

    private static final long  serialVersionUID = 1L;

    public static final String PK_GROUP_NAME    = "groupName";
    public static final String PK_NAME          = "name";

    private String             groupName;

    private String             name;


    public ConfigItemPK() {

        // empty
    }

    public ConfigItemPK(String groupName, String name) {

        this.groupName = groupName;
        this.name = name;
    }

    public String getGroupName() {

        return groupName;
    }

    public void setGroupName(String groupName) {

        this.groupName = groupName;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof ConfigItemPK)
            return false;
        ConfigItemPK rhs = (ConfigItemPK) obj;
        return new EqualsBuilder().append(groupName, rhs.groupName).append(name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(groupName).append(name).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("groupName", groupName).append("name", name).toString();
    }
}
