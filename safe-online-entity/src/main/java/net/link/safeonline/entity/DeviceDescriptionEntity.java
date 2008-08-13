/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.DeviceDescriptionEntity.QUERY_WHERE_DEVICE;

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
@Table(name = "device_description")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_DEVICE, query = "SELECT deviceDesc "
        + "FROM DeviceDescriptionEntity AS deviceDesc " + "WHERE deviceDesc.device = :device") })
public class DeviceDescriptionEntity implements Serializable {

    private static final long   serialVersionUID     = 1L;

    public static final String  QUERY_WHERE_DEVICE   = "dd.dev";

    public static final String  DEVICE_COLUMN_NAME   = "device";

    public static final String  LANGUAGE_COLUMN_NAME = "language";

    private DeviceDescriptionPK pk;

    private DeviceEntity        device;

    private String              language;

    private String              description;


    public DeviceDescriptionEntity() {

        // empty
    }

    public DeviceDescriptionEntity(DeviceEntity device, String language, String description) {

        this.device = device;
        this.language = language;
        this.description = description;
        this.pk = new DeviceDescriptionPK(device.getName(), language);
    }

    @Column(name = LANGUAGE_COLUMN_NAME, insertable = false, updatable = false)
    public String getLanguage() {

        return this.language;
    }

    public void setLanguage(String language) {

        this.language = language;
    }

    public String getDescription() {

        return this.description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = "deviceName", column = @Column(name = DEVICE_COLUMN_NAME)),
            @AttributeOverride(name = "language", column = @Column(name = LANGUAGE_COLUMN_NAME)) })
    public DeviceDescriptionPK getPk() {

        return this.pk;
    }

    public void setPk(DeviceDescriptionPK pk) {

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
        if (false == obj instanceof DeviceDescriptionEntity)
            return false;
        DeviceDescriptionEntity rhs = (DeviceDescriptionEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", this.pk).append("description", this.description).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_DEVICE)
        List<DeviceDescriptionEntity> listDescriptions(@QueryParam("device") DeviceEntity device);
    }
}
