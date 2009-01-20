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
public class UsageAgreementTextPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            owner;

    private Long              usageAgreementVersion;

    private String            language;


    public UsageAgreementTextPK() {

        // empty
    }

    public UsageAgreementTextPK(String owner, Long usageAgreementVersion, String language) {

        this.language = language;
        this.usageAgreementVersion = usageAgreementVersion;
        this.owner = owner;
    }

    public String getOwner() {

        return owner;
    }

    public void setOwner(String owner) {

        this.owner = owner;
    }

    @Column(name = "version")
    public Long getUsageAgreementVersion() {

        return usageAgreementVersion;
    }

    public void setUsageAgreementVersion(Long usageAgreementVersion) {

        this.usageAgreementVersion = usageAgreementVersion;
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
        if (false == obj instanceof UsageAgreementTextPK)
            return false;
        UsageAgreementTextPK rhs = (UsageAgreementTextPK) obj;
        return new EqualsBuilder().append(usageAgreementVersion, rhs.usageAgreementVersion).append(owner, rhs.owner).append(
                language, rhs.language).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(owner).append(usageAgreementVersion).append(language).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append(owner).append(usageAgreementVersion.toString()).append(language).toString();
    }
}
