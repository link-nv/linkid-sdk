/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.DevicePropertyEntity.QUERY_WHERE_DEVICE;

import java.io.Serializable;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "device_property")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_DEVICE, query = "SELECT deviceProp "
        + "FROM DevicePropertyEntity AS deviceProp " + "WHERE deviceProp.device = :device") })
public class DevicePropertyEntity implements Serializable {

    private static final long  serialVersionUID          = 1L;

    public static final String QUERY_WHERE_DEVICE        = "dp.dev";

    public static final String DEVICE_COLUMN_NAME        = "device";

    public static final String PROPERTY_NAME_COLUMN_NAME = "name";

    private DevicePropertyPK   pk;

    private DeviceEntity       device;

    private String             name;

    private String             value;


    public DevicePropertyEntity() {

        // empty
    }

    public DevicePropertyEntity(DeviceEntity device, String name, String value) {

        this.device = device;
        this.name = name;
        this.value = value;
        this.pk = new DevicePropertyPK(device.getName(), name);
    }

    @Column(name = PROPERTY_NAME_COLUMN_NAME, insertable = false, updatable = false)
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

    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = "deviceName", column = @Column(name = DEVICE_COLUMN_NAME)),
            @AttributeOverride(name = "name", column = @Column(name = PROPERTY_NAME_COLUMN_NAME)) })
    public DevicePropertyPK getPk() {

        return this.pk;
    }

    public void setPk(DevicePropertyPK pk) {

        this.pk = pk;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = DEVICE_COLUMN_NAME, insertable = false, updatable = false)
    public DeviceEntity getDevice() {

        return this.device;
    }

    public void setDevice(DeviceEntity device) {

        this.device = device;
    }

    @Transient
    public String getDeviceName() {

        return this.pk.getDeviceName();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof DevicePropertyEntity)
            return false;
        DevicePropertyEntity rhs = (DevicePropertyEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", this.pk).append("value", this.value).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_DEVICE)
        List<DevicePropertyEntity> listProperties(@QueryParam("device") DeviceEntity device);
    }

}
