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
public class DeviceDescriptionPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            deviceName;

    private String            language;


    public DeviceDescriptionPK() {

        // empty
    }

    public DeviceDescriptionPK(String deviceName, String language) {

        this.deviceName = deviceName;
        this.language = language;
    }

    public String getDeviceName() {

        return deviceName;
    }

    public void setDeviceName(String deviceName) {

        this.deviceName = deviceName;
    }

    public String getLanguage() {

        return language;
    }

    public void setLanguage(String language) {

        this.language = language;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof DeviceDescriptionPK)
            return false;
        DeviceDescriptionPK rhs = (DeviceDescriptionPK) obj;
        return new EqualsBuilder().append(deviceName, rhs.deviceName).append(language, rhs.language).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(deviceName).append(language).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("deviceName", deviceName).append("language", language).toString();
    }
}
