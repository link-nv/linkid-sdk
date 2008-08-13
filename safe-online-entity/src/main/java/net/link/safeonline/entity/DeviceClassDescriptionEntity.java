/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.DeviceClassDescriptionEntity.QUERY_WHERE_DEVICE_CLASS;

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
@Table(name = "device_class_description")
@NamedQueries( { @NamedQuery(name = QUERY_WHERE_DEVICE_CLASS, query = "SELECT deviceClassDesc "
        + "FROM DeviceClassDescriptionEntity AS deviceClassDesc " + "WHERE deviceClassDesc.deviceClass = :deviceClass") })
public class DeviceClassDescriptionEntity implements Serializable {

    private static final long        serialVersionUID         = 1L;

    public static final String       QUERY_WHERE_DEVICE_CLASS = "dcd.dc";

    public static final String       DEVICE_CLASS_COLUMN_NAME = "deviceClassName";

    public static final String       LANGUAGE_COLUMN_NAME     = "language";

    private DeviceClassDescriptionPK pk;

    private DeviceClassEntity        deviceClass;

    private String                   language;

    private String                   description;


    public DeviceClassDescriptionEntity() {

        // empty
    }

    public DeviceClassDescriptionEntity(DeviceClassEntity deviceClass, String language, String description) {

        this.deviceClass = deviceClass;
        this.language = language;
        this.description = description;
        this.pk = new DeviceClassDescriptionPK(deviceClass.getName(), language);
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
    @AttributeOverrides( {
            @AttributeOverride(name = "deviceClassName", column = @Column(name = DEVICE_CLASS_COLUMN_NAME)),
            @AttributeOverride(name = "language", column = @Column(name = LANGUAGE_COLUMN_NAME)) })
    public DeviceClassDescriptionPK getPk() {

        return this.pk;
    }

    public void setPk(DeviceClassDescriptionPK pk) {

        this.pk = pk;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = DEVICE_CLASS_COLUMN_NAME, insertable = false, updatable = false)
    public DeviceClassEntity getDeviceClass() {

        return this.deviceClass;
    }

    public void setDeviceClass(DeviceClassEntity deviceClass) {

        this.deviceClass = deviceClass;
    }

    @Transient
    public String getDeviceClassName() {

        return this.pk.getDeviceClassName();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (false == obj instanceof DeviceClassDescriptionEntity) {
            return false;
        }
        DeviceClassDescriptionEntity rhs = (DeviceClassDescriptionEntity) obj;
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

        @QueryMethod(QUERY_WHERE_DEVICE_CLASS)
        List<DeviceClassDescriptionEntity> listDescriptions(@QueryParam("deviceClass") DeviceClassEntity deviceClass);
    }

}
