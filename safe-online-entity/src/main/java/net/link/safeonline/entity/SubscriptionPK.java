/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
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
public class SubscriptionPK implements Serializable {

    private static final long  serialVersionUID      = 1L;

    public static final String APPLICATION_ID_COLUMN = "applicationId";
    public static final String SUBJECT_COLUMN        = "subject";

    private long               applicationId;

    private String             subject;


    public SubscriptionPK() {

        // empty
    }

    public SubscriptionPK(String subject, long applicationId) {

        this.subject = subject;
        this.applicationId = applicationId;
    }

    public SubscriptionPK(SubjectEntity subject, ApplicationEntity application) {

        this.subject = subject.getUserId();
        applicationId = application.getId();
    }

    @Column(name = APPLICATION_ID_COLUMN)
    public long getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(long applicationId) {

        this.applicationId = applicationId;
    }

    @Column(name = SUBJECT_COLUMN)
    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof SubscriptionPK)
            return false;
        SubscriptionPK rhs = (SubscriptionPK) obj;
        return new EqualsBuilder().append(subject, rhs.subject).append(applicationId, rhs.applicationId).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(subject).append(applicationId).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("subject", subject).append("applicationId", applicationId).toString();
    }
}
