/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_WHERE_CLASS;
import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_WHERE_CLASS_AUTH_CTX;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "devices")
@NamedQueries( {
        @NamedQuery(name = QUERY_LIST_ALL, query = "FROM DeviceEntity d"),
        @NamedQuery(name = QUERY_LIST_WHERE_CLASS, query = "SELECT d FROM DeviceEntity d " + "WHERE d.deviceClass = :deviceClass"),
        @NamedQuery(name = QUERY_LIST_WHERE_CLASS_AUTH_CTX, query = "SELECT d FROM DeviceEntity d "
                + "WHERE d.deviceClass.authenticationContextClass = :authenticationContextClass") })
public class DeviceEntity implements Serializable {

    private static final long                    serialVersionUID                = 1L;

    public static final String                   QUERY_LIST_ALL                  = "dev.all";

    public static final String                   QUERY_LIST_WHERE_CLASS          = "dev.class";

    public static final String                   QUERY_LIST_WHERE_CLASS_AUTH_CTX = "dev.cl.actx";

    private String                               name;

    private DeviceClassEntity                    deviceClass;

    private NodeEntity                           location;

    private String                               authenticationPath;

    private String                               authenticationWSPath;

    private String                               registrationPath;

    private String                               removalPath;

    private String                               updatePath;

    private String                               disablePath;

    private String                               enablePath;

    private AttributeTypeEntity                  attributeType;

    private AttributeTypeEntity                  userAttributeType;

    private AttributeTypeEntity                  disableAttributeType;

    private Map<String, DevicePropertyEntity>    properties;

    private Map<String, DeviceDescriptionEntity> descriptions;


    public DeviceEntity() {

        // empty
    }

    public DeviceEntity(String name, DeviceClassEntity deviceClass, NodeEntity location, String authenticationPath,
                        String authenticationWSPath, String registrationPath, String removalPath, String updatePath, String disablePath,
                        String enablePath) {

        this.name = name;
        this.deviceClass = deviceClass;
        this.location = location;
        this.authenticationPath = authenticationPath;
        this.authenticationWSPath = authenticationWSPath;
        this.registrationPath = registrationPath;
        this.removalPath = removalPath;
        this.updatePath = updatePath;
        this.disablePath = disablePath;
        this.enablePath = enablePath;
        properties = new HashMap<String, DevicePropertyEntity>();
        descriptions = new HashMap<String, DeviceDescriptionEntity>();
    }

    @Id
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     * This device attribute holds all the information returned to OLAS.
     * 
     */
    @ManyToOne
    public AttributeTypeEntity getAttributeType() {

        return attributeType;
    }

    public void setAttributeType(AttributeTypeEntity attributeType) {

        this.attributeType = attributeType;
    }

    /**
     * This device attribute holds the information for a user to recognize his device registration.
     * 
     */
    @ManyToOne
    public AttributeTypeEntity getUserAttributeType() {

        return userAttributeType;
    }

    public void setUserAttributeType(AttributeTypeEntity userAttributeType) {

        this.userAttributeType = userAttributeType;
    }

    /**
     * This device attribute holds the information for a user if this device registration is enabled or disabled.
     * 
     */
    @ManyToOne
    public AttributeTypeEntity getDisableAttributeType() {

        return disableAttributeType;
    }

    public void setDisableAttributeType(AttributeTypeEntity disableAttributeType) {

        this.disableAttributeType = disableAttributeType;
    }

    /**
     * Gives back the device class.
     * 
     */
    @ManyToOne
    public DeviceClassEntity getDeviceClass() {

        return deviceClass;
    }

    public void setDeviceClass(DeviceClassEntity deviceClass) {

        this.deviceClass = deviceClass;
    }

    @Transient
    public String getAuthenticationContextClass() {

        return deviceClass.getAuthenticationContextClass() + ":" + name;
    }

    /**
     * Gives back the location of this device.
     * 
     */
    @ManyToOne
    public NodeEntity getLocation() {

        return location;
    }

    public void setLocation(NodeEntity location) {

        this.location = location;
    }

    /**
     * Retrieve the local path for authentication of this device.
     * 
     */
    public String getAuthenticationPath() {

        return authenticationPath;
    }

    public void setAuthenticationPath(String authenticationPath) {

        this.authenticationPath = authenticationPath;
    }

    /**
     * Returns the full URL for authentication of this device.
     * 
     */
    @Transient
    public String getAuthenticationURL() {

        if (null == location)
            return authenticationPath;
        return location.getLocation() + authenticationPath;
    }

    /**
     * Retrieve the local path for web service authentication of this device.
     */
    public String getAuthenticationWSPath() {

        return authenticationWSPath;
    }

    public void setAuthenticationWSPath(String authenticationWSPath) {

        this.authenticationWSPath = authenticationWSPath;
    }

    /**
     * Returns the full URL for web service authentication of this device.
     */
    @Transient
    public String getAuthenticationWSURL() {

        if (null == location)
            return authenticationWSPath;
        return location.getLocation() + authenticationWSPath;

    }

    /**
     * Retrieves the local path for registration of this device.
     * 
     */
    public String getRegistrationPath() {

        return registrationPath;
    }

    public void setRegistrationPath(String registrationPath) {

        this.registrationPath = registrationPath;
    }

    /**
     * Returns the full URL for registration of this device.
     * 
     */
    @Transient
    public String getRegistrationURL() {

        if (null == location)
            return registrationPath;
        return location.getLocation() + registrationPath;
    }

    /**
     * Returns whether or not a user is allowed to register this device himself.
     * 
     */
    @Transient
    public boolean isRegistrable() {

        return null != registrationPath && registrationPath.length() > 0;
    }

    /**
     * Retrieves the local path for removal of this device.
     * 
     */
    public String getRemovalPath() {

        return removalPath;
    }

    public void setRemovalPath(String removalPath) {

        this.removalPath = removalPath;
    }

    /**
     * Returns the full URL for removal of this device.
     * 
     */
    @Transient
    public String getRemovalURL() {

        if (null == location)
            return removalPath;
        return location.getLocation() + removalPath;
    }

    /**
     * Returns whether or not a user is allowed to remove this device himself.
     * 
     */
    @Transient
    public boolean isRemovable() {

        return null != removalPath && removalPath.length() > 0;
    }

    /**
     * Retrieves the path for updating of this device.
     * 
     */
    public String getUpdatePath() {

        return updatePath;
    }

    public void setUpdatePath(String updatePath) {

        this.updatePath = updatePath;
    }

    /**
     * Returns the full URL for updating of this device.
     * 
     */
    @Transient
    public String getUpdateURL() {

        if (null == location)
            return updatePath;
        return location.getLocation() + updatePath;
    }

    /**
     * Returns whether or not a user is allowed to update this device himself.
     * 
     */
    @Transient
    public boolean isUpdatable() {

        return null != updatePath && updatePath.length() > 0;
    }

    /**
     * Retrieves the path for disabling this device.
     * 
     */
    public String getDisablePath() {

        return disablePath;
    }

    public void setDisablePath(String disablePath) {

        this.disablePath = disablePath;
    }

    /**
     * Returns the full URL for disabling this device.
     * 
     */
    @Transient
    public String getDisableURL() {

        if (null == location)
            return disablePath;
        return location.getLocation() + disablePath;
    }

    /**
     * Returns whether or not a user is allowed to disable this device himself.
     * 
     */
    @Transient
    public boolean isDisablable() {

        return null != disablePath && disablePath.length() > 0;
    }

    /**
     * Retrieves the path for enabling this device.
     * 
     */
    public String getEnablePath() {

        return enablePath;
    }

    public void setEnablePath(String enablePath) {

        this.enablePath = enablePath;
    }

    /**
     * Returns the full URL for enabling this device.
     * 
     */
    @Transient
    public String getEnableURL() {

        if (null == location)
            return enablePath;
        return location.getLocation() + enablePath;
    }

    /**
     * Returns whether or not a user is allowed to enable this device himself.
     * 
     */
    @Transient
    public boolean isEnablable() {

        return null != enablePath && enablePath.length() > 0;
    }

    /**
     * Returns map of device properties.
     * 
     */
    @OneToMany(mappedBy = "device")
    @MapKey(name = "name")
    public Map<String, DevicePropertyEntity> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, DevicePropertyEntity> properties) {

        this.properties = properties;
    }

    /**
     * Returns map of i18n device descriptions.
     * 
     */
    @OneToMany(mappedBy = "device")
    @MapKey(name = "language")
    public Map<String, DeviceDescriptionEntity> getDescriptions() {

        return descriptions;
    }

    public void setDescriptions(Map<String, DeviceDescriptionEntity> descriptions) {

        this.descriptions = descriptions;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof DeviceEntity)
            return false;
        DeviceEntity rhs = (DeviceEntity) obj;
        return new EqualsBuilder().append(name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(name).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("name", name).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<DeviceEntity> listDevices();

        @QueryMethod(QUERY_LIST_WHERE_CLASS)
        List<DeviceEntity> listDevices(@QueryParam("deviceClass") DeviceClassEntity deviceClass);

        @QueryMethod(QUERY_LIST_WHERE_CLASS_AUTH_CTX)
        List<DeviceEntity> listDevices(@QueryParam("authenticationContextClass") String authenticationContextClass);
    }
}
