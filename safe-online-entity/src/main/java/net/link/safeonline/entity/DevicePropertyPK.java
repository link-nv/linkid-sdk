/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Embeddable
public class DevicePropertyPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            deviceName;

    private String            name;


    public DevicePropertyPK() {

        // empty
    }

    public DevicePropertyPK(String deviceName, String name) {

        this.deviceName = deviceName;
        this.name = name;
    }

    public String getDeviceName() {

        return deviceName;
    }

    public void setDeviceName(String deviceName) {

        this.deviceName = deviceName;
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
        if (false == obj instanceof DevicePropertyPK)
            return false;
        DevicePropertyPK rhs = (DevicePropertyPK) obj;
        return new EqualsBuilder().append(deviceName, rhs.deviceName).append(name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(deviceName).append(name).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("deviceName", deviceName).append("name", name).toString();
    }
}
