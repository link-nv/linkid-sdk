/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_WHERE_CERT_SUBJECT;
import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_WHERE_CLASS;
import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_WHERE_CLASS_AUTH_CTX;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
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
        @NamedQuery(name = QUERY_LIST_WHERE_CLASS, query = "SELECT d FROM DeviceEntity d "
                + "WHERE d.deviceClass = :deviceClass"),
        @NamedQuery(name = QUERY_LIST_WHERE_CLASS_AUTH_CTX, query = "SELECT d FROM DeviceEntity d "
                + "WHERE d.deviceClass.authenticationContextClass = :authenticationContextClass"),
        @NamedQuery(name = QUERY_LIST_WHERE_CERT_SUBJECT, query = "SELECT device " + "FROM DeviceEntity AS device "
                + "WHERE device.certificateSubject = :certificateSubject") })
public class DeviceEntity implements Serializable {

    private static final long                    serialVersionUID                = 1L;

    public static final String                   QUERY_LIST_ALL                  = "dev.all";

    public static final String                   QUERY_LIST_WHERE_CLASS          = "dev.class";

    public static final String                   QUERY_LIST_WHERE_CLASS_AUTH_CTX = "dev.cl.actx";

    public static final String                   QUERY_LIST_WHERE_CERT_SUBJECT   = "dev.cert.sub";

    private String                               name;

    private DeviceClassEntity                    deviceClass;

    private NodeEntity                           location;

    private String                               authenticationPath;

    private String                               registrationPath;

    private String                               removalPath;

    private String                               updatePath;

    private String                               disablePath;

    private String                               enablePath;

    private String                               certificateSubject;

    private AttributeTypeEntity                  attributeType;

    private AttributeTypeEntity                  userAttributeType;

    private Map<String, DevicePropertyEntity>    properties;

    private Map<String, DeviceDescriptionEntity> descriptions;


    public DeviceEntity() {

        // empty
    }

    public DeviceEntity(String name, DeviceClassEntity deviceClass, NodeEntity location, String authenticationPath,
            String registrationPath, String removalPath, String updatePath, String disablePath, String enablePath,
            X509Certificate certificate) {

        this.name = name;
        this.deviceClass = deviceClass;
        this.location = location;
        this.authenticationPath = authenticationPath;
        this.registrationPath = registrationPath;
        this.removalPath = removalPath;
        this.updatePath = updatePath;
        this.disablePath = disablePath;
        this.enablePath = enablePath;
        this.properties = new HashMap<String, DevicePropertyEntity>();
        this.descriptions = new HashMap<String, DeviceDescriptionEntity>();
        if (null != certificate) {
            this.certificateSubject = certificate.getSubjectX500Principal().getName();
        }
    }

    @Id
    public String getName() {

        return this.name;
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

        return this.attributeType;
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

        return this.userAttributeType;
    }

    public void setUserAttributeType(AttributeTypeEntity userAttributeType) {

        this.userAttributeType = userAttributeType;
    }

    /**
     * Gives back the device class.
     * 
     */
    @ManyToOne
    public DeviceClassEntity getDeviceClass() {

        return this.deviceClass;
    }

    public void setDeviceClass(DeviceClassEntity deviceClass) {

        this.deviceClass = deviceClass;
    }

    @Transient
    public String getAuthenticationContextClass() {

        return this.deviceClass.getAuthenticationContextClass() + ":" + this.name;
    }

    /**
     * Gives back the location of this device.
     * 
     */
    @ManyToOne
    public NodeEntity getLocation() {

        return this.location;
    }

    public void setLocation(NodeEntity location) {

        this.location = location;
    }

    /**
     * Retrieve the local path for authentication of this device.
     * 
     */
    public String getAuthenticationPath() {

        return this.authenticationPath;
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

        if (null == this.location)
            return this.authenticationPath;
        return this.location.getLocation() + this.authenticationPath;
    }

    /**
     * Retrieves the local path for registration of this device.
     * 
     */
    public String getRegistrationPath() {

        return this.registrationPath;
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

        if (null == this.location)
            return this.registrationPath;
        return this.location.getLocation() + this.registrationPath;
    }

    /**
     * Returns whether or not a user is allowed to register this device himself.
     * 
     */
    @Transient
    public boolean isRegistrable() {

        return null != this.registrationPath && this.registrationPath.length() > 0;
    }

    /**
     * Retrieves the local path for removal of this device.
     * 
     */
    public String getRemovalPath() {

        return this.removalPath;
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

        if (null == this.location)
            return this.removalPath;
        return this.location.getLocation() + this.removalPath;
    }

    /**
     * Returns whether or not a user is allowed to remove this device himself.
     * 
     */
    @Transient
    public boolean isRemovable() {

        return null != this.removalPath && this.removalPath.length() > 0;
    }

    /**
     * Retrieves the path for updating of this device.
     * 
     */
    public String getUpdatePath() {

        return this.updatePath;
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

        if (null == this.location)
            return this.updatePath;
        return this.location.getLocation() + this.updatePath;
    }

    /**
     * Returns whether or not a user is allowed to update this device himself.
     * 
     */
    @Transient
    public boolean isUpdatable() {

        return null != this.updatePath && this.updatePath.length() > 0;
    }

    /**
     * Retrieves the path for disabling this device.
     * 
     */
    public String getDisablePath() {

        return this.disablePath;
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

        if (null == this.location)
            return this.disablePath;
        return this.location.getLocation() + this.disablePath;
    }

    /**
     * Returns whether or not a user is allowed to disable this device himself.
     * 
     */
    @Transient
    public boolean isDisablable() {

        return null != this.disablePath && this.disablePath.length() > 0;
    }

    /**
     * Retrieves the path for enabling this device.
     * 
     */
    public String getEnablePath() {

        return this.enablePath;
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

        if (null == this.location)
            return this.enablePath;
        return this.location.getLocation() + this.enablePath;
    }

    /**
     * Returns whether or not a user is allowed to enable this device himself.
     * 
     */
    @Transient
    public boolean isEnablable() {

        return null != this.enablePath && this.enablePath.length() > 0;
    }

    /**
     * The certificate subject is used during application authentication phase to associate a given certificate with
     * it's corresponding application.
     * 
     */
    @Column(unique = true)
    public String getCertificateSubject() {

        return this.certificateSubject;
    }

    /**
     * Sets the certificate subject. Do not use this method directly. Use {@link #setCertificate(X509Certificate)
     * setCertificate} instead. JPA requires this setter.
     * 
     * @param certificateSubject
     * @see #setCertificate(X509Certificate)
     */
    public void setCertificateSubject(String certificateSubject) {

        this.certificateSubject = certificateSubject;
    }

    /**
     * Sets the X509 certificate subject of the application. Use this method to update the certificate subject for this
     * application.
     * 
     * @param certificate
     */
    @Transient
    public void setCertificate(X509Certificate certificate) {

        setCertificateSubject(certificate.getSubjectX500Principal().getName());
    }

    /**
     * Returns map of device properties.
     * 
     */
    @OneToMany(mappedBy = "device")
    @MapKey(name = "name")
    public Map<String, DevicePropertyEntity> getProperties() {

        return this.properties;
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

        return this.descriptions;
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
        return new EqualsBuilder().append(this.name, rhs.name).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.name).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("name", this.name).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_LIST_ALL)
        List<DeviceEntity> listDevices();

        @QueryMethod(QUERY_LIST_WHERE_CLASS)
        List<DeviceEntity> listDevices(@QueryParam("deviceClass") DeviceClassEntity deviceClass);

        @QueryMethod(QUERY_LIST_WHERE_CLASS_AUTH_CTX)
        List<DeviceEntity> listDevices(@QueryParam("authenticationContextClass") String authenticationContextClass);

        @QueryMethod(QUERY_LIST_WHERE_CERT_SUBJECT)
        List<DeviceEntity> listDevicesWhereCertificateSubject(
                @QueryParam("certificateSubject") String certificateSubject);
    }
}
