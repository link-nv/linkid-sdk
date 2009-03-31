/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Copyright 2005-2006 Frank Cornelis.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.model.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.dao.TrustPointDAO;
import net.link.safeonline.pkix.model.CachedOcspValidator;
import net.link.safeonline.pkix.model.OcspValidator.OcspResult;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.pkix.model.bean.PkiValidatorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;


public class PkiValidatorBeanTest {

    private static final Log    LOG = LogFactory.getLog(PkiValidatorBeanTest.class);

    private PkiValidatorBean    testedInstance;

    private TrustPointDAO       mockTrustPointDAO;

    private CachedOcspValidator mockCachedOcspValidatorBean;

    private URI                 ocspUri;


    @Before
    public void setUp()
            throws Exception {

        testedInstance = new PkiValidatorBean();

        mockTrustPointDAO = createMock(TrustPointDAO.class);

        mockCachedOcspValidatorBean = createMock(CachedOcspValidator.class);

        EJBTestUtils.inject(testedInstance, mockTrustPointDAO);
        EJBTestUtils.inject(testedInstance, mockCachedOcspValidatorBean);
        EJBTestUtils.init(testedInstance);
    }

    @Test
    public void testValidateCertificateOnEmptyTrustDomainFails()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);

        // prepare
        replay(mockTrustPointDAO);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);
        assertEquals(PkiResult.INVALID, result);
    }

    @Test
    public void testValidateNullCertificateThrowsIllegalArgumentException()
            throws Exception {

        // operate & verify
        try {
            testedInstance.validateCertificate(new TrustDomainEntity(), null);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testValidateCertificate()
            throws Exception {

        // setup
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(10);
        DateTime caNotAfter = now.plusDays(10);
        X509Certificate caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA", caNotBefore, caNotAfter, null,
                true, true, false);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = now.minusDays(1);
        DateTime notAfter = now.plusDays(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", caKeyPair.getPrivate(),
                caCertificate, notBefore, notAfter, null, true, false, false, ocspUri);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain, caCertificate);
        LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
        trustPoints.add(caTrustPoint);

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, certificate, caCertificate)).andReturn(OcspResult.GOOD);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.VALID, result);
    }

    @Test
    public void testValidateTrustPointCertificate()
            throws Exception {

        // setup
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(10);
        DateTime caNotAfter = now.plusDays(10);
        X509Certificate caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA", caNotBefore, caNotAfter, null,
                true, true, false);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain, caCertificate);
        LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
        trustPoints.add(caTrustPoint);

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, caCertificate, caCertificate)).andReturn(OcspResult.GOOD);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, caCertificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.VALID, result);
    }

    @Test
    public void testValidateCertificateFailsIfOCSPRevokes()
            throws Exception {

        // setup
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(10);
        DateTime caNotAfter = now.plusDays(10);
        X509Certificate caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA", caNotBefore, caNotAfter, null,
                true, true, false);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = now.minusDays(1);
        DateTime notAfter = now.plusDays(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", caKeyPair.getPrivate(),
                caCertificate, notBefore, notAfter, null, true, false, false, ocspUri);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        trustPoints.add(new TrustPointEntity(trustDomain, caCertificate));

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, certificate, caCertificate)).andReturn(OcspResult.REVOKED);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.REVOKED, result);
    }

    @Test
    public void testValidateCertificateRootCaAndInterCa()
            throws Exception {

        // setup
        KeyPair rootCaKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime rootCaNotBefore = now.minusDays(10);
        DateTime rootCaNotAfter = now.plusDays(10);
        X509Certificate rootCaCertificate = PkiTestUtils.generateSelfSignedCertificate(rootCaKeyPair, "CN=TestRootCA", rootCaNotBefore,
                rootCaNotAfter, null, true, true, false);

        KeyPair interCaKeyPair = PkiTestUtils.generateKeyPair();
        DateTime interCaNotBefore = now.minusDays(5);
        DateTime interCaNotAfter = now.plusDays(5);
        X509Certificate interCaCertificate = PkiTestUtils.generateCertificate(interCaKeyPair.getPublic(), "CN=TestInterCA",
                rootCaKeyPair.getPrivate(), rootCaCertificate, interCaNotBefore, interCaNotAfter, null, true, true, false, null);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = now.minusDays(1);
        DateTime notAfter = now.plusDays(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", interCaKeyPair.getPrivate(),
                interCaCertificate, notBefore, notAfter, null, true, false, false, ocspUri);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        trustPoints.add(new TrustPointEntity(trustDomain, rootCaCertificate));
        trustPoints.add(new TrustPointEntity(trustDomain, interCaCertificate));

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, certificate, interCaCertificate)).andReturn(OcspResult.GOOD);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.VALID, result);
    }

    @Test
    public void testValidationFailsIfTrustPointIsNotCA()
            throws Exception {

        // setup
        KeyPair rootCaKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime rootCaNotBefore = now.minusDays(10);
        DateTime rootCaNotAfter = now.plusDays(10);
        X509Certificate rootCaCertificate = PkiTestUtils.generateSelfSignedCertificate(rootCaKeyPair, "CN=TestRootCA", rootCaNotBefore,
                rootCaNotAfter, null, true, true, false);

        KeyPair interCaKeyPair = PkiTestUtils.generateKeyPair();
        DateTime interCaNotBefore = now.minusDays(5);
        DateTime interCaNotAfter = now.plusDays(5);
        X509Certificate interCaCertificate = PkiTestUtils.generateCertificate(interCaKeyPair.getPublic(), "CN=TestInterCA",
                rootCaKeyPair.getPrivate(), rootCaCertificate, interCaNotBefore, interCaNotAfter, null, true, false, false, null);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = now.minusDays(1);
        DateTime notAfter = now.plusDays(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", interCaKeyPair.getPrivate(),
                interCaCertificate, notBefore, notAfter, null, true, false, false, ocspUri);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        trustPoints.add(new TrustPointEntity(trustDomain, rootCaCertificate));
        trustPoints.add(new TrustPointEntity(trustDomain, interCaCertificate));

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, certificate, interCaCertificate)).andStubReturn(
                OcspResult.GOOD);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.INVALID, result);
    }

    @Test
    public void testValidateCertificateIfRootIsNotSelfSignedFails()
            throws Exception {

        // setup
        KeyPair rootKeyPair = PkiTestUtils.generateKeyPair();
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(5);
        DateTime caNotAfter = now.plusDays(5);
        X509Certificate caCertificate = PkiTestUtils.generateCertificate(caKeyPair.getPublic(), "CN=TestCA", rootKeyPair.getPrivate(),
                null, caNotBefore, caNotAfter, null, true, false, false, ocspUri);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = now.minusDays(1);
        DateTime notAfter = now.plusDays(1);
        X509Certificate certificate = PkiTestUtils.generateCertificate(keyPair.getPublic(), "CN=Test", caKeyPair.getPrivate(),
                caCertificate, notBefore, notAfter, null, true, false, false, ocspUri);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        trustPoints.add(new TrustPointEntity(trustDomain, caCertificate));

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);

        // prepare
        replay(mockTrustPointDAO);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);
        assertEquals(PkiResult.INVALID, result);
    }

    @Test
    public void testValidateSelfSignedCertificateNoAuthorityKeyIdentifier()
            throws Exception {

        // setup
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(10);
        DateTime caNotAfter = now.plusDays(10);
        X509Certificate caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA", caNotBefore, caNotAfter, null,
                false, true, false);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain, caCertificate);
        LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
        trustPoints.add(caTrustPoint);

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, caCertificate, caCertificate))
                                                                                                             .andStubReturn(OcspResult.GOOD);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, caCertificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.VALID, result);
    }

    @Test
    public void testValidateSelfSignedCertificateNoCA()
            throws Exception {

        // setup
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(10);
        DateTime caNotAfter = now.plusDays(10);
        X509Certificate caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA", caNotBefore, caNotAfter, null,
                false, false, false);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain, caCertificate);
        LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
        trustPoints.add(caTrustPoint);

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);
        expect(mockCachedOcspValidatorBean.performCachedOcspCheck(trustDomain, caCertificate, caCertificate))
                                                                                                             .andStubReturn(OcspResult.GOOD);

        // prepare
        replay(mockTrustPointDAO);
        replay(mockCachedOcspValidatorBean);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, caCertificate);

        // verify
        verify(mockTrustPointDAO);
        verify(mockCachedOcspValidatorBean);
        assertEquals(PkiResult.VALID, result);
    }

    @Test
    public void testValidateSelfSignedCertificateNotTrusted()
            throws Exception {

        // setup
        KeyPair caKeyPair = PkiTestUtils.generateKeyPair();
        DateTime now = new DateTime();
        DateTime caNotBefore = now.minusDays(10);
        DateTime caNotAfter = now.plusDays(10);
        X509Certificate caCertificate = PkiTestUtils.generateSelfSignedCertificate(caKeyPair, "CN=TestCA", caNotBefore, caNotAfter, null,
                false, true, false);

        String trustDomainName = "test-trust-domain";
        TrustDomainEntity trustDomain = new TrustDomainEntity(trustDomainName, true);
        List<TrustPointEntity> trustPoints = new LinkedList<TrustPointEntity>();
        TrustPointEntity caTrustPoint = new TrustPointEntity(trustDomain, caCertificate);
        LOG.debug("ca key id: " + caTrustPoint.getPk().getKeyId());
        trustPoints.add(caTrustPoint);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        DateTime notBefore = now.minusDays(1);
        DateTime notAfter = now.plusDays(1);
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test", notBefore, notAfter, null, false,
                false, false);

        // stubs
        expect(mockTrustPointDAO.listTrustPoints(trustDomain)).andStubReturn(trustPoints);

        // prepare
        replay(mockTrustPointDAO);

        // operate
        PkiResult result = testedInstance.validateCertificate(trustDomain, certificate);

        // verify
        verify(mockTrustPointDAO);

        assertEquals(PkiResult.INVALID, result);
    }
}
