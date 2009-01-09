/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


@Embeddable
public class SubjectIdentifierPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            domain;

    private String            identifier;


    public SubjectIdentifierPK() {

        // empty
    }

    public SubjectIdentifierPK(String domain, String identifier) {

        this.domain = domain;
        this.identifier = identifier;
    }

    public String getDomain() {

        return domain;
    }

    public void setDomain(String domain) {

        this.domain = domain;
    }

    public String getIdentifier() {

        return identifier;
    }

    public void setIdentifier(String identifier) {

        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj)
            return false;
        if (this == obj)
            return true;
        if (false == obj instanceof SubjectIdentifierPK)
            return false;
        SubjectIdentifierPK rhs = (SubjectIdentifierPK) obj;
        return new EqualsBuilder().append(domain, rhs.domain).append(identifier, rhs.identifier).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(domain).append(identifier).toHashCode();
    }
}
