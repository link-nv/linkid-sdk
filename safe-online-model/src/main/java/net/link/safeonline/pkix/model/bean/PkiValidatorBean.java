/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Copyright 2005-2006 Frank Cornelis.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.pkix.model.bean;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.security.auth.x500.X500Principal;

import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.entity.pkix.TrustPointPK;
import net.link.safeonline.pkix.dao.TrustDomainDAO;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.model.CachedOcspValidator;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.OcspValidator.OcspResult;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.X509ExtensionUtil;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@LocalBinding(jndiBinding = PkiValidator.JNDI_BINDING)
public class PkiValidatorBean implements PkiValidator {

    private static final Log    LOG = LogFactory.getLog(PkiValidatorBean.class);

    @EJB(mappedName = TrustPointDAO.JNDI_BINDING)
    private TrustPointDAO       trustPointDAO;

    @EJB(mappedName = TrustDomainDAO.JNDI_BINDING)
    private TrustDomainDAO      trustDomainDAO;

    @EJB(mappedName = CachedOcspValidator.JNDI_BINDING)
    private CachedOcspValidator cachedOcspValidator;


    public PkiResult validateCertificate(TrustDomainEntity trustDomain, X509Certificate certificate) {

        /*
         * We don't use the JDK certificate path builder API here, since it doesn't bring anything but unnecessary complexity. Keep It
         * Simple, Stupid.
         */

        if (null == certificate)
            throw new IllegalArgumentException("certificate is null");

        LOG.debug("validate certificate " + certificate.getSubjectX500Principal() + " in domain " + trustDomain.getName());

        List<TrustPointEntity> trustPointPath = buildTrustPointPath(trustDomain, certificate);

        return verifyPath(trustDomain, certificate, trustPointPath);
    }

    private PkiResult checkValidity(X509Certificate certificate) {

        try {
            certificate.checkValidity();
            return PkiResult.VALID;
        } catch (CertificateExpiredException e) {
            LOG.debug("certificate expired");
            return PkiResult.EXPIRED;
        } catch (CertificateNotYetValidException e) {
            LOG.debug("certificate not yet valid");
            return PkiResult.NOT_YET_VALID;
        }
    }

    /**
     * Build the trust point path for a given certificate.
     * 
     * @param trustDomain
     * @param certificate
     * @return the path, or an empty list otherwise.
     */
    private List<TrustPointEntity> buildTrustPointPath(TrustDomainEntity trustDomain, X509Certificate certificate) {

        List<TrustPointEntity> trustPoints = trustPointDAO.listTrustPoints(trustDomain);
        HashMap<TrustPointPK, TrustPointEntity> trustPointMap = new HashMap<TrustPointPK, TrustPointEntity>();
        for (TrustPointEntity trustPoint : trustPoints) {
            trustPointMap.put(trustPoint.getPk(), trustPoint);
        }

        List<TrustPointEntity> trustPointPath = new LinkedList<TrustPointEntity>();

        LOG.debug("build path for cert: " + certificate.getSubjectX500Principal());

        X509Certificate currentRootCertificate = certificate;
        while (true) {
            byte[] authorityKeyIdentifierData = currentRootCertificate.getExtensionValue(X509Extensions.AuthorityKeyIdentifier.getId());
            String keyId;
            if (null == authorityKeyIdentifierData) {
                /*
                 * PKIX RFC allows this for the root CA certificate.
                 */
                LOG.warn("certificate has no authority key identifier extension");
                /*
                 * NULL is not allowed for persistence.
                 */
                keyId = "";
            } else {
                AuthorityKeyIdentifierStructure authorityKeyIdentifierStructure;
                try {
                    authorityKeyIdentifierStructure = new AuthorityKeyIdentifierStructure(authorityKeyIdentifierData);
                } catch (IOException e) {
                    LOG.error("error parsing authority key identifier structure");
                    break;
                }
                keyId = new String(Hex.encodeHex(authorityKeyIdentifierStructure.getKeyIdentifier()));
            }
            String issuer = currentRootCertificate.getIssuerX500Principal().getName();
            LOG.debug("issuer: " + issuer);
            LOG.debug("keyId: " + keyId);
            TrustPointPK trustPointPK = new TrustPointPK(trustDomain, issuer, keyId);
            TrustPointEntity matchingTrustPoint = trustPointMap.get(trustPointPK);
            if (null == matchingTrustPoint) {
                LOG.debug("no matching trust point found");
                break;
            }
            LOG.debug("found path node: " + matchingTrustPoint.getCertificate().getSubjectX500Principal());
            trustPointPath.add(0, matchingTrustPoint);
            currentRootCertificate = matchingTrustPoint.getCertificate();
            if (isSelfIssued(currentRootCertificate)) {
                break;
            }
        }

        LOG.debug("path construction completed");
        return trustPointPath;
    }

    private boolean isSelfIssued(X509Certificate certificate) {

        X500Principal issuer = certificate.getIssuerX500Principal();
        X500Principal subject = certificate.getSubjectX500Principal();
        boolean result = subject.equals(issuer);
        return result;
    }

    PkiResult verifyPath(TrustDomainEntity trustDomain, X509Certificate certificate, List<TrustPointEntity> trustPointPath) {

        if (trustPointPath.isEmpty()) {
            LOG.debug("trust point path is empty");
            return PkiResult.INVALID;
        }

        boolean performOcspCheck = trustDomain.isPerformOcspCheck();

        X509Certificate rootCertificate = trustPointPath.get(0).getCertificate();
        X509Certificate issuerCertificate = rootCertificate;
        PublicKey issuerPublicKey = issuerCertificate.getPublicKey();

        for (TrustPointEntity trustPoint : trustPointPath) {
            X509Certificate trustPointCertificate = trustPoint.getCertificate();
            LOG.debug("verifying: " + trustPointCertificate.getSubjectX500Principal());
            PkiResult checkValidityResult = checkValidity(trustPointCertificate);
            if (PkiResult.VALID != checkValidityResult)
                return checkValidityResult;
            if (false == verifySignature(trustPointCertificate, issuerPublicKey))
                return PkiResult.INVALID;
            if (false == verifyConstraints(trustPointCertificate)) {
                LOG.debug("verify constraints did not pass");
                return PkiResult.INVALID;
            }
            issuerCertificate = trustPointCertificate;
            issuerPublicKey = issuerCertificate.getPublicKey();
        }

        PkiResult checkValidityResult = checkValidity(certificate);
        if (PkiResult.VALID != checkValidityResult)
            return checkValidityResult;
        if (false == verifySignature(certificate, issuerPublicKey))
            return PkiResult.INVALID;
        if (true == performOcspCheck) {
            LOG.debug("performing OCSP check");
            return convertOcspResult(cachedOcspValidator.performCachedOcspCheck(trustDomain, certificate, issuerCertificate));
        }

        return PkiResult.VALID;
    }

    private PkiResult convertOcspResult(OcspResult ocspResult) {

        if (ocspResult == OcspResult.GOOD)
            return PkiResult.VALID;
        else if (ocspResult == OcspResult.REVOKED)
            return PkiResult.REVOKED;
        else if (ocspResult == OcspResult.SUSPENDED)
            return PkiResult.SUSPENDED;
        else
            return PkiResult.INVALID;
    }

    private boolean verifyConstraints(X509Certificate certificate) {

        byte[] basicConstraintsValue = certificate.getExtensionValue(X509Extensions.BasicConstraints.getId());
        if (null == basicConstraintsValue) {
            LOG.debug("no basic contraints extension present");
            /*
             * A basic constraints extension is optional.
             */
            return true;
        }
        ASN1Encodable basicConstraintsDecoded;
        try {
            basicConstraintsDecoded = X509ExtensionUtil.fromExtensionValue(basicConstraintsValue);
        } catch (IOException e) {
            LOG.error("IO error: " + e.getMessage(), e);
            return false;
        }
        if (false == basicConstraintsDecoded instanceof ASN1Sequence) {
            LOG.debug("basic constraints extension is not an ASN1 sequence");
            return false;
        }
        ASN1Sequence basicConstraintsSequence = (ASN1Sequence) basicConstraintsDecoded;
        BasicConstraints basicConstraints = new BasicConstraints(basicConstraintsSequence);
        if (false == basicConstraints.isCA()) {
            LOG.debug("basic contraints says not a CA");
            return false;
        }
        return true;
    }

    private boolean verifySignature(X509Certificate certificate, PublicKey issuerPublicKey) {

        try {
            certificate.verify(issuerPublicKey);
        } catch (InvalidKeyException e) {
            LOG.debug("invalid key");
            /*
             * This can occur if a root certificate was not self-signed.
             */
            return false;
        } catch (CertificateException e) {
            LOG.debug("cert error: " + e.getMessage());
            return false;
        } catch (NoSuchAlgorithmException e) {
            LOG.debug("algo error");
            return false;
        } catch (NoSuchProviderException e) {
            LOG.debug("provider error");
            return false;
        } catch (SignatureException e) {
            LOG.debug("sign error: " + e.getMessage(), e);
            return false;
        }
        return true;
    }

    public PkiResult validateCertificate(String trustDomainName, X509Certificate certificate)
            throws TrustDomainNotFoundException {

        TrustDomainEntity trustDomain = trustDomainDAO.getTrustDomain(trustDomainName);
        return validateCertificate(trustDomain, certificate);
    }
}
