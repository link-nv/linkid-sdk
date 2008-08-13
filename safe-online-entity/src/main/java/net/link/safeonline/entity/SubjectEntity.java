/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.SubjectEntity.QUERY_ALL;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


@Entity
@Table(name = "subject")
@NamedQueries( { @NamedQuery(name = QUERY_ALL, query = "SELECT userId FROM SubjectEntity") })
public class SubjectEntity implements Serializable {

    private static final long  serialVersionUID = 1L;

    private String             userId;

    private Long               confirmedUsageAgreementVersion;

    public static final String QUERY_ALL        = "sub.all";


    public SubjectEntity() {

        // required
    }

    public SubjectEntity(String userId) {

        this.userId = userId;
        this.confirmedUsageAgreementVersion = GlobalUsageAgreementEntity.EMPTY_GLOBAL_USAGE_AGREEMENT_VERSION;
    }

    @Id
    public String getUserId() {

        return this.userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public Long getConfirmedUsageAgreementVersion() {

        return this.confirmedUsageAgreementVersion;
    }

    public void setConfirmedUsageAgreementVersion(Long confirmedUsageAgreementVersion) {

        this.confirmedUsageAgreementVersion = confirmedUsageAgreementVersion;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof SubjectEntity)
            return false;
        SubjectEntity rhs = (SubjectEntity) obj;
        return new EqualsBuilder().append(this.userId, rhs.userId).isEquals();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE).append("userId", this.userId).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_ALL)
        List<String> listUsers();
    }
}
