/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Embeddable
public class UsageAgreementPK implements Serializable {

    public static final Long   DRAFT_USAGE_AGREEMENT_VERSION   = -1L;
    public static final Long   EMPTY_USAGE_AGREEMENT_VERSION   = 0L;
    public static final Long   INITIAL_USAGE_AGREEMENT_VERSION = 1L;

    public static final String APPLICATION_ID_COLUMN           = "applicationId";
    public static final String VERSION_COLUMN                  = "version";

    private static final long  serialVersionUID                = 1L;

    private long               applicationId;

    private Long               usageAgreementVersion;


    public UsageAgreementPK() {

        // empty
    }

    public UsageAgreementPK(long applicationId, Long usageAgreementVersion) {

        this.applicationId = applicationId;
        this.usageAgreementVersion = usageAgreementVersion;
    }

    public long getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(long applicationId) {

        this.applicationId = applicationId;
    }

    @Column(name = VERSION_COLUMN)
    public Long getUsageAgreementVersion() {

        return usageAgreementVersion;
    }

    public void setUsageAgreementVersion(Long usageAgreementVersion) {

        this.usageAgreementVersion = usageAgreementVersion;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof UsageAgreementPK)
            return false;
        UsageAgreementPK rhs = (UsageAgreementPK) obj;
        return new EqualsBuilder().append(applicationId, rhs.applicationId).append(usageAgreementVersion, rhs.usageAgreementVersion)
                                  .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(applicationId).append(usageAgreementVersion).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("applicationId", applicationId).append("usageAgreementVersion", usageAgreementVersion)
                                        .toString();
    }

}
