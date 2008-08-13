/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_ALL;
import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_CERT_SUBJECT;
import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_OWNER;
import static net.link.safeonline.entity.ApplicationEntity.QUERY_WHERE_USER_ALL;

import java.awt.Color;
import java.io.Serializable;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.entity.listener.SecurityApplicationEntityListener;
import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * Application Entity.
 * 
 * @author fcorneli
 * 
 */
@Entity
@Table(name = "application")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_ALL, query = "FROM ApplicationEntity"),
        @NamedQuery(name = QUERY_WHERE_USER_ALL, query = "SELECT application "
                + "FROM ApplicationEntity AS application " + "WHERE application.allowUserSubscription = true"),
        @NamedQuery(name = QUERY_WHERE_OWNER, query = "SELECT application " + "FROM ApplicationEntity AS application "
                + "WHERE application.applicationOwner = :applicationOwner"),
        @NamedQuery(name = QUERY_WHERE_CERT_SUBJECT, query = "SELECT application "
                + "FROM ApplicationEntity AS application "
                + "WHERE application.certificateSubject = :certificateSubject") })
@EntityListeners(SecurityApplicationEntityListener.class)
public class ApplicationEntity implements Serializable {

    private static final long      serialVersionUID         = 1L;

    public static final String     QUERY_WHERE_ALL          = "app.all";

    public static final String     QUERY_WHERE_USER_ALL     = "app.user.all";

    public static final String     QUERY_WHERE_OWNER        = "app.owner";

    public static final String     QUERY_WHERE_CERT_SUBJECT = "app.cert.sub";

    protected String               name;

    protected String               friendlyName;

    protected String               description;

    protected URL                  applicationUrl;

    protected byte[]               applicationLogo;

    protected Color                applicationColor;

    protected boolean              allowUserSubscription;

    protected boolean              removable;

    private ApplicationOwnerEntity applicationOwner;

    private String                 certificateSubject;

    private long                   currentApplicationIdentity;

    private long                   currentApplicationUsageAgreement;

    private boolean                deviceRestriction;

    private boolean                identifierMappingAllowed;

    private IdScopeType            idScope;

    private boolean                skipMessageIntegrityCheck;


    public boolean isDeviceRestriction() {

        return this.deviceRestriction;
    }

    public void setDeviceRestriction(boolean deviceRestriction) {

        this.deviceRestriction = deviceRestriction;
    }

    public ApplicationEntity() {

        // empty
    }

    public ApplicationEntity(String name, String friendlyName, ApplicationOwnerEntity applicationOwner,
            String description, URL applicationUrl, byte[] applicationLogo, Color applicationColor,
            X509Certificate certificate) {

        this(name, friendlyName, applicationOwner, description, applicationUrl, applicationLogo, applicationColor,
                true, true, certificate, 0, 0);
    }

    public ApplicationEntity(String name, String friendlyName, ApplicationOwnerEntity applicationOwner,
            String description, URL applicationUrl, byte[] applicationLogo, Color applicationColor,
            boolean allowUserSubscription, boolean removable, X509Certificate certificate, long identityVersion,
            long usageAgreementVersion) {

        this(name, friendlyName, applicationOwner, description, applicationUrl, applicationLogo, applicationColor,
                allowUserSubscription, removable, certificate, identityVersion, usageAgreementVersion, false);
    }

    public ApplicationEntity(String name, String friendlyName, ApplicationOwnerEntity applicationOwner,
            String description, URL applicationUrl, byte[] applicationLogo, Color applicationColor,
            boolean allowUserSubscription, boolean removable, X509Certificate certificate, long identityVersion,
            long usageAgreementVersion, boolean deviceRestriction) {

        this.name = name;
        this.friendlyName = friendlyName;
        this.applicationOwner = applicationOwner;
        this.description = description;
        this.applicationUrl = applicationUrl;
        this.applicationLogo = applicationLogo;
        this.applicationColor = applicationColor;
        this.allowUserSubscription = allowUserSubscription;
        this.removable = removable;
        this.deviceRestriction = deviceRestriction;
        this.currentApplicationIdentity = identityVersion;
        this.currentApplicationUsageAgreement = usageAgreementVersion;
        if (null != certificate) {
            this.certificateSubject = certificate.getSubjectX500Principal().getName();
        }
    }

    public String getDescription() {

        return this.description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * The unique name of the application. This field is used as primary key on the application entity.
     * 
     */
    @Id
    @Column(name = "name")
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    /**
     * The optional user friendly name of the application
     * 
     */
    @Column(name = "friendlyName")
    public String getFriendlyName() {

        return this.friendlyName;
    }

    public void setFriendlyName(String friendlyName) {

        this.friendlyName = friendlyName;
    }

    /**
     * Retrieve the URL where the application can be accessed.
     */
    public URL getApplicationUrl() {

        return this.applicationUrl;
    }

    /**
     * Set the URL where the application can be accessed.
     */
    public void setApplicationUrl(URL applicationUrl) {

        this.applicationUrl = applicationUrl;
    }

    /**
     * Retrieve the logo of this application.
     */
    @Lob
    @Column(length = 10 * 1024, nullable = true)
    public byte[] getApplicationLogo() {

        return this.applicationLogo;
    }

    /**
     * Set the logo of this application.
     */
    public void setApplicationLogo(byte[] applicationLogo) {

        this.applicationLogo = applicationLogo;
    }

    /**
     * Retrieve the base color of this application's theme.
     */
    public Color getApplicationColor() {

        return this.applicationColor;
    }

    /**
     * Set the base color of this application's theme.
     */
    public void setApplicationColor(Color applicationColor) {

        this.applicationColor = applicationColor;
    }

    /**
     * Marks whether a user is allowed to subscribe himself onto this application. This field prevents users from
     * subscribing themselves onto the operator web application or the application owner web application.
     * 
     */
    public boolean isAllowUserSubscription() {

        return this.allowUserSubscription;
    }

    public void setAllowUserSubscription(boolean allowUserSubscription) {

        this.allowUserSubscription = allowUserSubscription;
    }

    /**
     * Marks whether the operator can remove this application. This prevents the operator from removing critical
     * application like the SafeOnline user web application, the SafeOnline application owner web application, the
     * SafeOnline authentication web application and the SafeOnline operator web application.
     * 
     */
    public boolean isRemovable() {

        return this.removable;
    }

    public void setRemovable(boolean removable) {

        this.removable = removable;
    }

    /**
     * Gives back the application owner of this application. Each application has an application owner. The application
     * owner is allowed to perform certain operations regarding this application.
     * 
     */
    @ManyToOne(optional = false)
    public ApplicationOwnerEntity getApplicationOwner() {

        return this.applicationOwner;
    }

    public void setApplicationOwner(ApplicationOwnerEntity applicationOwner) {

        this.applicationOwner = applicationOwner;
    }

    /**
     * Gives back the current application identity version number. Each application can have multiple application
     * identities. Each application identity has a version number. This field marks the currently active application
     * identity version.
     * 
     */
    public long getCurrentApplicationIdentity() {

        return this.currentApplicationIdentity;
    }

    public void setCurrentApplicationIdentity(long currentApplicationIdentity) {

        this.currentApplicationIdentity = currentApplicationIdentity;
    }

    /**
     * Gives back the current application usage agreement version number. Each application can have multiple usage
     * agreement identities. Each application usage agreement has a version number. This field marks the currently
     * active application usage agreement version.
     * 
     */
    @Column(name = "currentUsageAg")
    public long getCurrentApplicationUsageAgreement() {

        return this.currentApplicationUsageAgreement;
    }

    public void setCurrentApplicationUsageAgreement(long currentApplicationUsageAgreement) {

        this.currentApplicationUsageAgreement = currentApplicationUsageAgreement;
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
     * The identifier mapping allowed field use used for access control over the identifier mapping service.
     * 
     */
    public boolean isIdentifierMappingAllowed() {

        return this.identifierMappingAllowed;
    }

    public void setIdentifierMappingAllowed(boolean identifierMappingAllowed) {

        this.identifierMappingAllowed = identifierMappingAllowed;
    }

    /**
     * The id scope field is used to determine which type of id should be returned to the caller application.
     * 
     */
    public IdScopeType getIdScope() {

        return this.idScope;
    }

    public void setIdScope(IdScopeType idScope) {

        this.idScope = idScope;
    }

    /**
     * When set to <code>true</code> the WS-Security SOAP handlers will not check whether the SOAP body has been signed.
     * This is required for compatability with .NET 3.0 WCF clients.
     * 
     */
    public boolean isSkipMessageIntegrityCheck() {

        return this.skipMessageIntegrityCheck;
    }

    public void setSkipMessageIntegrityCheck(boolean skipMessageIntegrityCheck) {

        this.skipMessageIntegrityCheck = skipMessageIntegrityCheck;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof ApplicationEntity)
            return false;
        ApplicationEntity rhs = (ApplicationEntity) obj;
        return new EqualsBuilder().append(this.name, rhs.name).isEquals();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("name", this.name).append("description",
                this.description).append("allowUserSubscription", this.allowUserSubscription).append("removable",
                this.removable).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_ALL)
        List<ApplicationEntity> listApplications();

        @QueryMethod(QUERY_WHERE_USER_ALL)
        List<ApplicationEntity> listUserApplications();

        @QueryMethod(QUERY_WHERE_OWNER)
        List<ApplicationEntity> listApplicationsWhereApplicationOwner(
                @QueryParam("applicationOwner") ApplicationOwnerEntity applicationOwner);

        @QueryMethod(QUERY_WHERE_CERT_SUBJECT)
        List<ApplicationEntity> listApplicationsWhereCertificateSubject(
                @QueryParam("certificateSubject") String certificateSubject);
    }
}
