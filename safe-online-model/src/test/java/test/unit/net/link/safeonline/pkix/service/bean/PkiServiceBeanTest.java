/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.pkix.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.persistence.EntityManager;

import net.link.safeonline.Startable;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.pkix.TrustDomainEntity;
import net.link.safeonline.entity.pkix.TrustPointEntity;
import net.link.safeonline.keystore.SafeOnlineKeyStore;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.pkix.exception.CertificateEncodingException;
import net.link.safeonline.pkix.exception.TrustDomainNotFoundException;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.pkix.service.bean.PkiServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class PkiServiceBeanTest {

    private EntityTestManager entityTestManager;
    private KeyService        mockKeyService;
    private JndiTestUtils     jndiTestUtils;


    @Before
    protected void setUp()
            throws Exception {

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        final KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineKeyStore.class)).andReturn(
                new PrivateKeyEntry(olasKeyPair.getPrivate(), new Certificate[] { olasCertificate }));

        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        systemStartable.postStart();
        entityTestManager.refreshEntityManager();
    }

    @After
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testAddRemoveTrustDomain()
            throws Exception {

        EntityManager entityManager = entityTestManager.getEntityManager();
        PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager,
                "test-operator", SafeOnlineRoles.OPERATOR_ROLE);

        pkiService.addTrustDomain("test-trust-domain", true);
        TrustDomainEntity trustDomain = pkiService.getTrustDomain("test-trust-domain");
        assertEquals("test-trust-domain", trustDomain.getName());
        pkiService.removeTrustDomain("test-trust-domain");
        try {
            pkiService.getTrustDomain("test-trust-domain");
            fail();
        } catch (TrustDomainNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testAddTrustPointWithFakeCertificateThrowsException()
            throws Exception {

        EntityManager entityManager = entityTestManager.getEntityManager();
        PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager,
                "test-operator", SafeOnlineRoles.OPERATOR_ROLE);

        pkiService.addTrustDomain("test-trust-domain", true);
        try {
            pkiService.addTrustPoint("test-trust-domain", "foobar".getBytes());
            fail();
        } catch (CertificateEncodingException e) {
            // expected
        }
    }

    @Test
    public void testAddRemoveTrustPoint()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        PkiService pkiService = EJBTestUtils.newInstance(PkiServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager,
                "test-operator", SafeOnlineRoles.OPERATOR_ROLE);

        pkiService.addTrustDomain("test-trust-domain", true);
        List<TrustPointEntity> result = pkiService.listTrustPoints("test-trust-domain");
        assertNotNull(result);
        assertTrue(result.isEmpty());
        pkiService.addTrustPoint("test-trust-domain", certificate.getEncoded());
        pkiService.listTrustPoints("test-trust-domain");
        result = pkiService.listTrustPoints("test-trust-domain");
        assertEquals(1, result.size());
        assertEquals(certificate, result.get(0).getCertificate());
    }
}
