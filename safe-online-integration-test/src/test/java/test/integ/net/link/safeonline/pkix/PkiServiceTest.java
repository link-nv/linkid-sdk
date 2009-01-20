/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.pkix;

import static test.integ.net.link.safeonline.IntegrationTestUtils.getPkiService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubjectService;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import javax.naming.InitialContext;

import junit.framework.TestCase;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.integ.net.link.safeonline.IntegrationTestUtils;


/**
 * PKIX service integration tests.
 * 
 * @author fcorneli
 * 
 */
public class PkiServiceTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(PkiServiceTest.class);

    private InitialContext   initialContext;

    private PkiService       pkiService;

    private SubjectService   subjectService;


    @Override
    protected void setUp()
            throws Exception {

        super.setUp();

        initialContext = IntegrationTestUtils.getInitialContext();
        IntegrationTestUtils.setupLoginConfig();
        pkiService = getPkiService(initialContext);
        subjectService = getSubjectService(initialContext);
    }

    public void testTrustDomain()
            throws Exception {

        // setup
        String trustDomainName = UUID.randomUUID().toString();
        SubjectEntity adminSubject = subjectService.findSubjectFromUserName("admin");

        // operate
        IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
        List<TrustDomainEntity> trustDomains = pkiService.listTrustDomains();
        int origSize = trustDomains.size();
        LOG.debug("number of trust domains: " + origSize);
        pkiService.addTrustDomain(trustDomainName, true);
        trustDomains = pkiService.listTrustDomains();
        assertEquals(origSize + 1, trustDomains.size());
        boolean containsAddedTrustDomain = false;
        for (TrustDomainEntity trustDomain : trustDomains) {
            LOG.debug(trustDomain.toString());
            if (trustDomainName.equals(trustDomain.getName())) {
                containsAddedTrustDomain = true;
            }
        }
        assertTrue(containsAddedTrustDomain);

        // operate: adding twice does not work
        try {
            pkiService.addTrustDomain(trustDomainName, true);
            fail();
        } catch (Exception e) {
            // expected
            LOG.debug("expected exception type: " + e.getClass().getName());
        }

        // operate: remove trust domain
        pkiService.removeTrustDomain(trustDomainName);

        // verify
        trustDomains = pkiService.listTrustDomains();
        containsAddedTrustDomain = false;
        for (TrustDomainEntity trustDomain : trustDomains) {
            LOG.debug(trustDomain.toString());
            if (trustDomainName.equals(trustDomain.getName())) {
                containsAddedTrustDomain = true;
            }
        }
        assertFalse(containsAddedTrustDomain);

        // operate & verify: removing twice does not work
        try {
            pkiService.removeTrustDomain(trustDomainName);
            fail();
        } catch (TrustDomainNotFoundException e) {
            // expected
        }
    }

    public void testTrustPoint()
            throws Exception {

        // setup
        String trustDomainName = "domain-" + UUID.randomUUID().toString();
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        String dn = "CN=Test";
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, dn);
        SubjectEntity adminSubject = subjectService.findSubjectFromUserName("admin");

        // operate: add trust domain
        IntegrationTestUtils.login(adminSubject.getUserId(), "admin");
        pkiService.addTrustDomain(trustDomainName, true);

        // operate: add trust point
        pkiService.addTrustPoint(trustDomainName, certificate.getEncoded());

        // operate: get trust points
        List<TrustPointEntity> trustPoints = pkiService.listTrustPoints(trustDomainName);

        // verify
        assertEquals(1, trustPoints.size());
        assertEquals(certificate, trustPoints.get(0).getCertificate());

        // operate: remove trust point
        pkiService.removeTrustPoint(trustPoints.get(0));

        // operate: remove trust domain
        pkiService.removeTrustDomain(trustDomainName);
    }
}
