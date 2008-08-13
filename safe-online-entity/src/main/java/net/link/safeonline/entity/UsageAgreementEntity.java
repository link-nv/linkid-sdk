/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.UsageAgreementEntity.DELETE_WHERE_APPLICATION_AND_VERSION;
import static net.link.safeonline.entity.UsageAgreementEntity.QUERY_WHERE_APPLICATION;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


@Entity
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT usageAgreement "
                + "FROM UsageAgreementEntity AS usageAgreement " + "WHERE usageAgreement.application = :application "
                + "ORDER BY usageAgreement.pk.usageAgreementVersion DESC"),
        @NamedQuery(name = DELETE_WHERE_APPLICATION_AND_VERSION, query = "DELETE "
                + "FROM UsageAgreementEntity AS usageAgreement " + "WHERE usageAgreement.application = :application "
                + "AND usageAgreement.pk.usageAgreementVersion = :usageAgreementVersion") })
@Table(name = "UsageAg")
public class UsageAgreementEntity implements Serializable {

    private static final long             serialVersionUID                     = 1L;

    public static final String            QUERY_WHERE_APPLICATION              = "ua.app";

    public static final String            DELETE_WHERE_APPLICATION_AND_VERSION = "ua.remove.app.version";

    public static final String            APPLICATION_COLUMN_NAME              = "application";

    public static final String            USAGE_AGREEMENT_VERSION_COLUMN_NAME  = "version";

    private UsageAgreementPK              pk;

    private ApplicationEntity             application;

    private Set<UsageAgreementTextEntity> usageAgreementTexts;


    public UsageAgreementEntity() {

        this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
    }

    public UsageAgreementEntity(ApplicationEntity application, Long usageAgreementVersion) {

        this.pk = new UsageAgreementPK(application.getName(), usageAgreementVersion);
        this.application = application;
        this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
    }

    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = "application", column = @Column(name = APPLICATION_COLUMN_NAME)),
            @AttributeOverride(name = "version", column = @Column(name = USAGE_AGREEMENT_VERSION_COLUMN_NAME)) })
    public UsageAgreementPK getPk() {

        return this.pk;
    }

    public void setPk(UsageAgreementPK pk) {

        this.pk = pk;
    }

    @Column(name = "texts")
    @OneToMany(fetch = FetchType.EAGER)
    public Set<UsageAgreementTextEntity> getUsageAgreementTexts() {

        return this.usageAgreementTexts;
    }

    public void setUsageAgreementTexts(Set<UsageAgreementTextEntity> usageAgreementTexts) {

        this.usageAgreementTexts = usageAgreementTexts;
    }

    @Transient
    public Long getUsageAgreementVersion() {

        return this.pk.getUsageAgreementVersion();
    }

    @Transient
    public void setUsageAgreementVersion(Long usageAgreementVersion) {

        this.pk.setUsageAgreementVersion(usageAgreementVersion);
    }

    @Transient
    public UsageAgreementTextEntity getUsageAgreementText(String language) {

        for (UsageAgreementTextEntity usageAgreementText : this.usageAgreementTexts) {
            if (usageAgreementText.getLanguage().equals(language))
                return usageAgreementText;
        }
        return null;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = APPLICATION_COLUMN_NAME, insertable = false, updatable = false)
    public ApplicationEntity getApplication() {

        return this.application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(this.pk).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof UsageAgreementEntity)
            return false;
        UsageAgreementEntity rhs = (UsageAgreementEntity) obj;
        return new EqualsBuilder().append(this.pk, rhs.pk).isEquals();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_APPLICATION)
        List<UsageAgreementEntity> listUsageAgreements(@QueryParam("application") ApplicationEntity application);

        @UpdateMethod(DELETE_WHERE_APPLICATION_AND_VERSION)
        void removeUsageAgreement(@QueryParam("application") ApplicationEntity application,
                @QueryParam("usageAgreementVersion") Long usageAgreementVersion);
    }

}
