/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.pkix;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.X509Certificate;

import javax.ejb.EJBException;
import javax.persistence.Embeddable;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;


@Embeddable
public class TrustPointPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            subjectName;

    private String            keyId;

    private long              domain;


    public TrustPointPK() {

        // empty
    }

    public TrustPointPK(TrustDomainEntity trustDomain, String subjectName, String keyId) {

        domain = trustDomain.getId();
        this.subjectName = subjectName;
        if (null == keyId || keyId.equals("")) {
            this.keyId = subjectName;
        } else {
            this.keyId = keyId;
        }
    }

    public TrustPointPK(TrustDomainEntity trustDomain, X509Certificate certificate) {

        String newSubjectName = getSubjectName(certificate);
        String newKeyId = getSubjectKeyId(certificate);

        domain = trustDomain.getId();
        subjectName = newSubjectName;
        if (null == newKeyId || newKeyId.equals("")) {
            keyId = newSubjectName;
        } else {
            keyId = newKeyId;
        }
    }

    private String getSubjectName(X509Certificate certificate) {

        return certificate.getSubjectX500Principal().getName();
    }

    public static String getSubjectKeyId(X509Certificate certificate) {

        byte[] subjectKeyIdData = certificate.getExtensionValue(X509Extensions.SubjectKeyIdentifier.getId());
        if (null == subjectKeyIdData)
            /*
             * NULL is not allowed as value for persistence.
             */
            return "";
        SubjectKeyIdentifierStructure subjectKeyIdentifierStructure;
        try {
            subjectKeyIdentifierStructure = new SubjectKeyIdentifierStructure(subjectKeyIdData);
        } catch (IOException e) {
            throw new EJBException("error parsing the subject key identifier certificate extension");
        }
        String keyId = new String(Hex.encodeHex(subjectKeyIdentifierStructure.getKeyIdentifier()));
        return keyId;
    }

    public String getSubjectName() {

        return subjectName;
    }

    public void setSubjectName(String application) {

        subjectName = application;
    }

    public long getDomain() {

        return domain;
    }

    public void setDomain(long domain) {

        this.domain = domain;
    }

    public String getKeyId() {

        return keyId;
    }

    public void setKeyId(String keyId) {

        this.keyId = keyId;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (false == obj instanceof TrustPointPK)
            return false;
        TrustPointPK rhs = (TrustPointPK) obj;
        return new EqualsBuilder().append(domain, rhs.domain).append(subjectName, rhs.subjectName).append(keyId, rhs.keyId)
                                  .isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(domain).append(subjectName).append(keyId).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("domain", domain).append("subject name", subjectName)
                                        .append("key id", keyId).toString();
    }
}
