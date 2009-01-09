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
public class DeviceClassDescriptionPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            deviceClassName;

    private String            language;


    public DeviceClassDescriptionPK() {

        // empty
    }

    public DeviceClassDescriptionPK(String deviceClassName, String language) {

        this.deviceClassName = deviceClassName;
        this.language = language;
    }

    public String getDeviceClassName() {

        return deviceClassName;
    }

    public void setDeviceClassName(String deviceClassName) {

        this.deviceClassName = deviceClassName;
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
        if (false == obj instanceof DeviceClassDescriptionPK)
            return false;
        DeviceClassDescriptionPK rhs = (DeviceClassDescriptionPK) obj;
        return new EqualsBuilder().append(deviceClassName, rhs.deviceClassName).append(language, rhs.language).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(deviceClassName).append(language).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("deviceClassName", deviceClassName).append("language", language).toString();
    }
}
