/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.model.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.Startable;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class SystemInitializationStartableBeanTest extends TestCase {

    public void testPostStart() throws Exception {

        // setup
        EntityTestManager entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate",
                new MBeanActionHandler() {

                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return authCertificate;
                    }
                });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate",
                new MBeanActionHandler() {

                    public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                        return certificate;
                    }
                });

        Startable testedInstance = EJBTestUtils.newInstance(SystemInitializationStartableBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        // operate
        testedInstance.postStart();
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
        /*
         * We run postStart twice since the system must be capable of rebooting using an persistent database.
         */
        testedInstance.postStart();
        entityTestManager.tearDown();
    }
}
