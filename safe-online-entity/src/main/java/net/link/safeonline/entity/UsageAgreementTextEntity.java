/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.UsageAgreementTextEntity.DELETE_WHERE_OWNER_AND_VERSION;
import static net.link.safeonline.entity.UsageAgreementTextEntity.QUERY_WHERE_OWNER_AND_VERSION;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


@Entity
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_OWNER_AND_VERSION, query = "SELECT usageAgreementText "
                + "FROM UsageAgreementTextEntity AS usageAgreementText " + "WHERE usageAgreementText.pk.owner = :owner "
                + "AND usageAgreementText.pk.usageAgreementVersion = :version " + "ORDER BY usageAgreementText.pk.language DESC"),
        @NamedQuery(name = DELETE_WHERE_OWNER_AND_VERSION, query = "DELETE " + "FROM UsageAgreementTextEntity AS usageAgreementText "
                + "WHERE usageAgreementText.pk.owner = :owner " + "AND usageAgreementText.pk.usageAgreementVersion = :version") })
@Table(name = "UsageAgText")
public class UsageAgreementTextEntity implements Serializable {

    private static final long    serialVersionUID               = 1L;

    public static final String   QUERY_WHERE_OWNER_AND_VERSION  = "uat.owner.version";

    public static final String   DELETE_WHERE_OWNER_AND_VERSION = "uat.del.owner.version";

    private String               text;

    private UsageAgreementTextPK pk;


    public UsageAgreementTextEntity() {

        // empty
    }

    public UsageAgreementTextEntity(GlobalUsageAgreementEntity globalUsageAgreement, String text, String language) {

        this.text = text;
        pk = new UsageAgreementTextPK(GlobalUsageAgreementEntity.GLOBAL_USAGE_AGREEMENT, globalUsageAgreement.getUsageAgreementVersion(),
                language);
    }

    public UsageAgreementTextEntity(UsageAgreementEntity usageAgreement, String text, String language) {

        this.text = text;
        pk = new UsageAgreementTextPK(usageAgreement.getApplication().getId(), usageAgreement.getUsageAgreementVersion(), language);
    }

    @Lob
    @Column(length = 1024 * 1024)
    public String getText() {

        return text;
    }

    public void setText(String text) {

        this.text = text;
    }

    @EmbeddedId
    public UsageAgreementTextPK getPk() {

        return pk;
    }

    public void setPk(UsageAgreementTextPK pk) {

        this.pk = pk;
    }

    @Transient
    public String getLanguage() {

        return pk.getLanguage();
    }

    @Transient
    public Long getUsageAgreementVersion() {

        return pk.getUsageAgreementVersion();
    }

    @Transient
    public void setUsageAgreementVersion(Long usageAgreementVersion) {

        pk.setUsageAgreementVersion(usageAgreementVersion);
    }

    @Transient
    public long getOwner() {

        return pk.getOwner();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof UsageAgreementTextEntity)
            return false;
        UsageAgreementTextEntity rhs = (UsageAgreementTextEntity) obj;
        return new EqualsBuilder().append(pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("pk", pk).append("text", text).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_OWNER_AND_VERSION)
        List<UsageAgreementTextEntity> listUsageAgreementTexts(@QueryParam("owner") String ownerName,
                                                               @QueryParam("version") Long usageAgreementVersion);

        @UpdateMethod(DELETE_WHERE_OWNER_AND_VERSION)
        void removeUsageAgreementTexts(@QueryParam("owner") long owner, @QueryParam("version") Long usageAgreementVersion);
    }

}
