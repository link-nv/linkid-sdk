/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.checkOrder;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.persistence.EntityManager;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.UsageAgreementServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.GlobalUsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementEntity;
import net.link.safeonline.entity.UsageAgreementPK;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class UsageAgreementServiceBeanTest {

    private EntityTestManager entityTestManager;
    private KeyService        mockKeyService;
    private JndiTestUtils     jndiTestUtils;


    @Before
    protected void setUp()
            throws Exception {

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);
        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate }));

        checkOrder(mockKeyService, false);
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
    public void testUsageAgreementUseCase()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        String ownerId = subjectService.findSubjectFromUserName("owner").getUserId();

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, ownerId, SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE);
        ApplicationEntity application = applicationService.getApplication(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME);

        UsageAgreementService usageAgreementService = EJBTestUtils.newInstance(UsageAgreementServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, ownerId, SafeOnlineRoles.OWNER_ROLE, SafeOnlineRoles.OPERATOR_ROLE);

        // operate
        UsageAgreementEntity usageAgreement = usageAgreementService.getCurrentUsageAgreement(application.getId());

        // verify
        assertNull(usageAgreement);

        // operate
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();

        usageAgreementService.createDraftUsageAgreement(application.getId(), UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION);
        usageAgreementService.createDraftUsageAgreementText(application.getId(), Locale.ENGLISH.getLanguage(), "test-usage-agreement");
        usageAgreementService.updateUsageAgreement(application.getId());
        entityManager.getTransaction().commit();
        usageAgreement = usageAgreementService.getCurrentUsageAgreement(application.getId());

        // verify
        assertEquals(new Long(UsageAgreementPK.EMPTY_USAGE_AGREEMENT_VERSION + 1), usageAgreement.getUsageAgreementVersion());
    }

    @Test
    public void testGlobalUsageAgreementUseCase()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        String operId = subjectService.findSubjectFromUserName("admin").getUserId();

        UsageAgreementService usageAgreementService = EJBTestUtils.newInstance(UsageAgreementServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, operId, SafeOnlineRoles.OPERATOR_ROLE);

        // operate
        GlobalUsageAgreementEntity usageAgreement = usageAgreementService.getCurrentGlobalUsageAgreement();

        // verify
        assertNull(usageAgreement);

        // operate
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();

        usageAgreementService.createDraftGlobalUsageAgreement();
        usageAgreementService.createDraftGlobalUsageAgreementText(Locale.ENGLISH.getLanguage(), "test-usage-agreement");
        usageAgreementService.updateGlobalUsageAgreement();
        entityManager.getTransaction().commit();

        // verify
        usageAgreement = usageAgreementService.getCurrentGlobalUsageAgreement();

        // verify
        assertEquals(GlobalUsageAgreementEntity.INITIAL_GLOBAL_USAGE_AGREEMENT_VERSION, usageAgreement.getUsageAgreementVersion());

    }
}
