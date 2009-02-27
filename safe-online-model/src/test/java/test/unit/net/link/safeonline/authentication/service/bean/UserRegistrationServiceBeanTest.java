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

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class UserRegistrationServiceBeanTest {

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

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        SystemInitializationStartableBean systemInit = EJBTestUtils.newInstance(SystemInitializationStartableBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        systemInit.postStart();

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();
    }

    @After
    protected void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testRegister()
            throws Exception {

        // setup
        String testLogin = "test-login";

        EntityManager entityManager = entityTestManager.getEntityManager();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        // operate
        userRegistrationService.registerUser(testLogin);

        // verify
        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity resultSubject = subjectService.getSubjectFromUserName(testLogin);
        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        AttributeTypeEntity loginAttributeType = attributeTypeDAO.getAttributeType(SafeOnlineConstants.LOGIN_ATTRIBTUE);
        AttributeDAO attributeDAO = EJBTestUtils.newInstance(AttributeDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        AttributeEntity loginAttribute = attributeDAO.getAttribute(loginAttributeType, resultSubject);
        assertEquals(testLogin, loginAttribute.getValue());
    }
}
