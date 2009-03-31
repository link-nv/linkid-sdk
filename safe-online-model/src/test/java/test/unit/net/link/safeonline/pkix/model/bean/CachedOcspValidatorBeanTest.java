/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.model.bean;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Date;

import net.link.safeonline.entity.pkix.CachedOcspResponseEntity;
import net.link.safeonline.entity.pkix.CachedOcspResultType;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.pkix.dao.CachedOcspResponseDAO;
import net.link.safeonline.pkix.model.OcspValidator;
import net.link.safeonline.pkix.model.OcspValidator.OcspResult;
import net.link.safeonline.pkix.model.bean.CachedOcspValidatorBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.Before;
import org.junit.Test;


public class CachedOcspValidatorBeanTest {

    private CachedOcspValidatorBean testedInstance;

    private CachedOcspResponseDAO   mockCachedOcspResponseDAOBean;

    private OcspValidator           mockOcspValidatorBean;


    @Before
    public void setUp()
            throws Exception {

        testedInstance = new CachedOcspValidatorBean();

        mockOcspValidatorBean = createMock(OcspValidator.class);
        mockCachedOcspResponseDAOBean = createMock(CachedOcspResponseDAO.class);

        EJBTestUtils.inject(testedInstance, mockOcspValidatorBean);
        EJBTestUtils.inject(testedInstance, mockCachedOcspResponseDAOBean);
        EJBTestUtils.init(testedInstance);
    }

    @Test
    public void testValid()
            throws Exception {

        // setup
        URI ocspUri = new URI("http://test.ocsp.responder/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(ocspUri);

        CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(null, CachedOcspResultType.GOOD, null);

        TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain", true, 3600000);

        // stubs call 1
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(null);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.GOOD);
        expect(
                mockCachedOcspResponseDAOBean.addCachedOcspResponse((String) anyObject(), (CachedOcspResultType) anyObject(),
                        (TrustDomainEntity) anyObject())).andReturn(cachedOcspResponse);

        // stubs call 2
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(cachedOcspResponse);

        // prepare
        replay(mockCachedOcspResponseDAOBean);
        replay(mockOcspValidatorBean);

        // operate
        OcspResult firstLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);
        OcspResult secondLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);

        // verify
        verify(mockCachedOcspResponseDAOBean);
        verify(mockOcspValidatorBean);
        assertEquals(OcspResult.GOOD, firstLookup);
        assertEquals(OcspResult.GOOD, secondLookup);
    }

    @Test
    public void testRevoked()
            throws Exception {

        // setup
        URI ocspUri = new URI("http://test.ocsp.responder/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(ocspUri);

        CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(null, CachedOcspResultType.REVOKED, null);

        TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain", true, 3600000);

        // stubs call 1
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(null);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.REVOKED);
        expect(
                mockCachedOcspResponseDAOBean.addCachedOcspResponse((String) anyObject(), (CachedOcspResultType) anyObject(),
                        (TrustDomainEntity) anyObject())).andReturn(cachedOcspResponse);

        // stubs call 2
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(cachedOcspResponse);

        // prepare
        replay(mockCachedOcspResponseDAOBean);
        replay(mockOcspValidatorBean);

        // operate
        OcspResult firstLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);
        OcspResult secondLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);

        // verify
        verify(mockCachedOcspResponseDAOBean);
        verify(mockOcspValidatorBean);
        assertEquals(OcspResult.REVOKED, firstLookup);
        assertEquals(OcspResult.REVOKED, secondLookup);
    }

    @Test
    public void testFailed()
            throws Exception {

        // setup
        URI ocspUri = new URI("http://test.ocsp.responder/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(ocspUri);

        // stubs call 1
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(null);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.FAILED);

        // stubs call 2
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(null);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.FAILED);

        // prepare
        replay(mockCachedOcspResponseDAOBean);
        replay(mockOcspValidatorBean);

        // operate
        OcspResult firstLookup = testedInstance.performCachedOcspCheck(null, certificate, certificate);
        OcspResult secondLookup = testedInstance.performCachedOcspCheck(null, certificate, certificate);

        // verify
        verify(mockCachedOcspResponseDAOBean);
        verify(mockOcspValidatorBean);
        assertEquals(OcspResult.FAILED, firstLookup);
        assertEquals(OcspResult.FAILED, secondLookup);
    }

    @Test
    public void testExpiredValid()
            throws Exception {

        // setup
        URI ocspUri = new URI("http://test.ocsp.responder/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(ocspUri);

        CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(null, CachedOcspResultType.GOOD, null);
        cachedOcspResponse.setEntryDate(new Date(0));

        TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain", true, 3600000);

        // stubs call 1
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(null);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.GOOD);
        expect(
                mockCachedOcspResponseDAOBean.addCachedOcspResponse((String) anyObject(), (CachedOcspResultType) anyObject(),
                        (TrustDomainEntity) anyObject())).andReturn(cachedOcspResponse);

        // stubs call 2
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(cachedOcspResponse);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.GOOD);

        // prepare
        replay(mockCachedOcspResponseDAOBean);
        replay(mockOcspValidatorBean);

        // operate
        OcspResult firstLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);
        OcspResult secondLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);

        // verify
        verify(mockCachedOcspResponseDAOBean);
        verify(mockOcspValidatorBean);
        assertEquals(OcspResult.GOOD, firstLookup);
        assertEquals(OcspResult.GOOD, secondLookup);
    }

    @Test
    public void testExpiredRevoked()
            throws Exception {

        // setup
        URI ocspUri = new URI("http://test.ocsp.responder/");
        X509Certificate certificate = PkiTestUtils.generateTestSelfSignedCert(ocspUri);

        CachedOcspResponseEntity cachedOcspResponse = new CachedOcspResponseEntity(null, CachedOcspResultType.REVOKED, null);
        cachedOcspResponse.setEntryDate(new Date(0));

        TrustDomainEntity trustDomain = new TrustDomainEntity("test-domain", true, 3600000);

        // stubs call 1
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(null);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.REVOKED);
        expect(
                mockCachedOcspResponseDAOBean.addCachedOcspResponse((String) anyObject(), (CachedOcspResultType) anyObject(),
                        (TrustDomainEntity) anyObject())).andReturn(cachedOcspResponse);

        // stubs call 2
        expect(mockOcspValidatorBean.getOcspUri(certificate)).andReturn(ocspUri);
        expect(mockCachedOcspResponseDAOBean.findCachedOcspResponse((String) anyObject())).andReturn(cachedOcspResponse);
        expect(mockOcspValidatorBean.verifyOcspStatus(ocspUri, certificate, certificate)).andReturn(OcspResult.REVOKED);

        // prepare
        replay(mockCachedOcspResponseDAOBean);
        replay(mockOcspValidatorBean);

        // operate
        OcspResult firstLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);
        OcspResult secondLookup = testedInstance.performCachedOcspCheck(trustDomain, certificate, certificate);

        // verify
        verify(mockCachedOcspResponseDAOBean);
        verify(mockOcspValidatorBean);
        assertEquals(OcspResult.REVOKED, firstLookup);
        assertEquals(OcspResult.REVOKED, secondLookup);
    }
}
