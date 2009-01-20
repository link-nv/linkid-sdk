/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.pkix;

import static net.link.safeonline.entity.pkix.TrustPointEntity.QUERY_WHERE_CERT_SUBJECT;
import static net.link.safeonline.entity.pkix.TrustPointEntity.QUERY_WHERE_DOMAIN;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.EJBException;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


@Entity
@Table(name = "trust_point")
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_DOMAIN, query = "SELECT trustPoint " + "FROM TrustPointEntity AS trustPoint "
                + "WHERE trustPoint.trustDomain = :trustDomain"),
        @NamedQuery(name = QUERY_WHERE_CERT_SUBJECT, query = "SELECT trustPoint " + "FROM TrustPointEntity AS trustPoint "
                + "WHERE trustPoint.subjectName = :certificateSubject") })
public class TrustPointEntity implements Serializable {

    private static final long         serialVersionUID         = 1L;

    public static final String        QUERY_WHERE_DOMAIN       = "tp.domain";

    public static final String        QUERY_WHERE_CERT_SUBJECT = "tp.cert.sub";

    private TrustPointPK              pk;

    private byte[]                    encodedCert;

    private transient X509Certificate certificate;

    private String                    issuerName;

    private String                    subjectName;

    private TrustDomainEntity         trustDomain;


    public TrustPointEntity() {

        // empty
    }

    public TrustPointEntity(TrustDomainEntity trustDomain, X509Certificate certificate) {

        this.trustDomain = trustDomain;
        this.certificate = certificate;
        issuerName = certificate.getIssuerX500Principal().getName();
        try {
            encodedCert = certificate.getEncoded();
        } catch (CertificateEncodingException e) {
            throw new EJBException("cert encoding error: " + e.getMessage());
        }
        pk = new TrustPointPK(trustDomain, certificate);
        subjectName = pk.getSubjectName();
    }

    @EmbeddedId
    @AttributeOverrides( { @AttributeOverride(name = "domain", column = @Column(name = "domain")),
            @AttributeOverride(name = "subjectName", column = @Column(name = "subjectName")),
            @AttributeOverride(name = "keyId", column = @Column(name = "keyId")) })
    public TrustPointPK getPk() {

        return pk;
    }

    public void setPk(TrustPointPK pk) {

        this.pk = pk;
    }

    public String getIssuerName() {

        return issuerName;
    }

    public void setIssuerName(String issuerName) {

        this.issuerName = issuerName;
    }

    @Column(name = "subjectName", insertable = false, updatable = false)
    public String getSubjectName() {

        return subjectName;
    }

    public void setSubjectName(String subjectName) {

        this.subjectName = subjectName;
    }

    @Lob
    @Column(length = 4 * 1024)
    public byte[] getEncodedCert() {

        return encodedCert;
    }

    public void setEncodedCert(byte[] encodedCert) {

        this.encodedCert = encodedCert;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "domain", insertable = false, updatable = false)
    public TrustDomainEntity getTrustDomain() {

        return trustDomain;
    }

    public void setTrustDomain(TrustDomainEntity trustDomain) {

        this.trustDomain = trustDomain;
    }

    @Transient
    public X509Certificate getCertificate() {

        if (null != certificate)
            return certificate;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(encodedCert);
            certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        } catch (CertificateException e) {
            throw new EJBException("cert factory error: " + e.getMessage());
        }
        return certificate;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (null == obj)
            return false;
        if (false == obj instanceof TrustPointEntity)
            return false;
        TrustPointEntity rhs = (TrustPointEntity) obj;
        return new EqualsBuilder().append(pk, rhs.pk).isEquals();
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder().append(pk).toHashCode();
    }

    @Override
    public String toString() {

        return new ToStringBuilder(this).append("pk", pk).toString();
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_DOMAIN)
        List<TrustPointEntity> listTrustPoints(@QueryParam("trustDomain") TrustDomainEntity trustDomain);

        @QueryMethod(QUERY_WHERE_CERT_SUBJECT)
        List<TrustPointEntity> listTrustPoints(@QueryParam("certificateSubject") String certificateSubject);
    }
}
