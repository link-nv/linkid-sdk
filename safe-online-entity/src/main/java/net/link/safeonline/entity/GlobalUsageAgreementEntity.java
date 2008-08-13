/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.GlobalUsageAgreementEntity.QUERY_CURRENT_GLOBAL;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;


@Entity
@NamedQueries( { @NamedQuery(name = QUERY_CURRENT_GLOBAL, query = "SELECT usageAgreement "
        + "FROM GlobalUsageAgreementEntity AS usageAgreement "
        + "WHERE usageAgreement.usageAgreementVersion = (SELECT MAX(usageAgreement.usageAgreementVersion) "
        + "FROM GlobalUsageAgreementEntity as usageAgreement)") })
@Table(name = "GUsageAg")
public class GlobalUsageAgreementEntity implements Serializable {

    private static final long             serialVersionUID                       = 1L;

    public static final String            GLOBAL_USAGE_AGREEMENT                 = "GLOBAL_USAGE_AGREEMENT";

    public static final Long              DRAFT_GLOBAL_USAGE_AGREEMENT_VERSION   = -1L;

    public static final Long              EMPTY_GLOBAL_USAGE_AGREEMENT_VERSION   = 0L;

    public static final Long              INITIAL_GLOBAL_USAGE_AGREEMENT_VERSION = 1L;

    public static final String            QUERY_CURRENT_GLOBAL                   = "gua.cur";

    private Long                          usageAgreementVersion;

    private Set<UsageAgreementTextEntity> usageAgreementTexts;


    public GlobalUsageAgreementEntity() {

        this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
    }

    public GlobalUsageAgreementEntity(Long usageAgreementVersion) {

        this.usageAgreementVersion = usageAgreementVersion;
        this.usageAgreementTexts = new HashSet<UsageAgreementTextEntity>();
    }

    @Column(name = "texts")
    @OneToMany(fetch = FetchType.EAGER)
    public Set<UsageAgreementTextEntity> getUsageAgreementTexts() {

        return this.usageAgreementTexts;
    }

    public void setUsageAgreementTexts(Set<UsageAgreementTextEntity> usageAgreementTexts) {

        this.usageAgreementTexts = usageAgreementTexts;
    }

    @Column(name = "version")
    @Id
    public Long getUsageAgreementVersion() {

        return this.usageAgreementVersion;
    }

    public void setUsageAgreementVersion(Long usageAgreementVersion) {

        this.usageAgreementVersion = usageAgreementVersion;
    }

    @Transient
    public UsageAgreementTextEntity getUsageAgreementText(String language) {

        for (UsageAgreementTextEntity usageAgreementText : this.usageAgreementTexts) {
            if (usageAgreementText.getLanguage().equals(language))
                return usageAgreementText;
        }
        return null;
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_CURRENT_GLOBAL)
        GlobalUsageAgreementEntity getCurrentGlobalUsageAgreement();
    }

}
