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
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.authentication.service.bean.AttributeProviderServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeProviderDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeProviderDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
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


public class AttributeProviderServiceBeanTest {

    private EntityTestManager entityTestManager;
    private KeyService        mockKeyService;
    private JndiTestUtils     jndiTestUtils;


    @Before
    public void setUp()
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
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testCreateAttributeRequiresAttributeProvider()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        String testLogin = "test-subject-login";
        String testAttributeName = "test-attribute-name";
        String[] testAttributeValue = { "hello", "world" };
        String testApplicationName = "test-application";
        String testApplicationOwner = "test-application-owner";
        String testApplicationAdmin = "test-application-admin";

        SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        SubjectEntity applicationAdminSubject = subjectDAO.addSubject(testApplicationAdmin);

        ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(ApplicationOwnerDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        ApplicationOwnerEntity testApplicationOwnerEntity = applicationOwnerDAO.addApplicationOwner(testApplicationOwner,
                applicationAdminSubject);

        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        AttributeTypeEntity testAttributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, false);
        attributeTypeDAO.addAttributeType(testAttributeType);

        ApplicationDAO applicationDAO = EJBTestUtils.newInstance(ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        ApplicationEntity application = applicationDAO.addApplication(testApplicationName, null, testApplicationOwnerEntity, null, null,
                null, null);

        AttributeProviderService attributeProviderService = EJBTestUtils.newInstance(AttributeProviderServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, Long.toString(application.getId()),
                SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate & verify
        try {
            attributeProviderService.createAttribute(testLogin, testAttributeName, testAttributeValue);
            fail();
        } catch (PermissionDeniedException e) {
            // expected
        }
    }

    @Test
    public void testMultivaluedAttribute()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        String testLogin = "test-subject-login";
        String testAttributeName = "test-attribute-name";
        String value1 = "hello";
        String value2 = "world";
        String[] testAttributeValue = { value1, value2 };
        String testApplicationName = "test-application";
        String testApplicationOwner = "test-application-owner";
        String testApplicationAdmin = "test-application-admin";

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity applicationAdminSubject = subjectService.addSubject(testApplicationAdmin);
        SubjectEntity testLoginSubject = subjectService.addSubject(testLogin);

        ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(ApplicationOwnerDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        ApplicationOwnerEntity testApplicationOwnerEntity = applicationOwnerDAO.addApplicationOwner(testApplicationOwner,
                applicationAdminSubject);

        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        AttributeTypeEntity testAttributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, false);
        testAttributeType.setMultivalued(true);
        attributeTypeDAO.addAttributeType(testAttributeType);

        ApplicationDAO applicationDAO = EJBTestUtils.newInstance(ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        ApplicationEntity testApplication = applicationDAO.addApplication(testApplicationName, null, testApplicationOwnerEntity, null,
                null, null, null);

        AttributeProviderDAO attributeProviderDAO = EJBTestUtils.newInstance(AttributeProviderDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        attributeProviderDAO.addAttributeProvider(testApplication, testAttributeType);

        AttributeProviderService attributeProviderService = EJBTestUtils.newInstance(AttributeProviderServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, Long.toString(testApplication.getId()),
                SafeOnlineApplicationRoles.APPLICATION_ROLE);

        try {
            attributeProviderService.setAttribute(testLoginSubject.getUserId(), testAttributeName, null);
            fail();
        } catch (AttributeNotFoundException e) {
            /*
             * Expected: cannot set an unexisting attribute value.
             */
        }

        try {
            attributeProviderService.setAttribute(testLoginSubject.getUserId(), testAttributeName, testAttributeValue);
            fail();
        } catch (AttributeNotFoundException e) {
            /*
             * Cannot create a nonexisting attribute via setAttribute.
             */
        }

        // operate
        attributeProviderService.createAttribute(testLoginSubject.getUserId(), testAttributeName, testAttributeValue);

        // verify
        String[] resultAttributes = (String[]) attributeProviderService.getAttributes(testLoginSubject.getUserId(), testAttributeName);
        assertEquals(testAttributeValue.length, resultAttributes.length);
        assertEquals(value1, resultAttributes[0]);
        assertEquals(value2, resultAttributes[1]);

        // operate
        attributeProviderService.setAttribute(testLoginSubject.getUserId(), testAttributeName, new String[] { value2 });

        // verify
        resultAttributes = (String[]) attributeProviderService.getAttributes(testLoginSubject.getUserId(), testAttributeName);
        assertEquals(1, resultAttributes.length);
        assertEquals(value2, resultAttributes[0]);

        // operate
        attributeProviderService.setAttribute(testLoginSubject.getUserId(), testAttributeName, new String[] { value1, value2, value1 });

        // verify
        resultAttributes = (String[]) attributeProviderService.getAttributes(testLoginSubject.getUserId(), testAttributeName);
        assertEquals(3, resultAttributes.length);
        assertEquals(value1, resultAttributes[0]);
        assertEquals(value2, resultAttributes[1]);
        assertEquals(value1, resultAttributes[2]);
    }

    @Test
    public void testCreateSingleValuedAttributes()
            throws Exception {

        // setup
        EntityManager entityManager = entityTestManager.getEntityManager();
        String testLogin = "test-subject-login";
        String testAttributeName = "test-attribute-name";
        String testAttributeValue = "test-value";
        String testApplicationName = "test-application";
        String testApplicationOwner = "test-application-owner";
        String testApplicationAdmin = "test-application-admin";

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity applicationAdminSubject = subjectService.addSubject(testApplicationAdmin);
        SubjectEntity testLoginSubject = subjectService.addSubject(testLogin);

        ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(ApplicationOwnerDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        ApplicationOwnerEntity testApplicationOwnerEntity = applicationOwnerDAO.addApplicationOwner(testApplicationOwner,
                applicationAdminSubject);

        AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(AttributeTypeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        AttributeTypeEntity testAttributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, false);
        attributeTypeDAO.addAttributeType(testAttributeType);

        ApplicationDAO applicationDAO = EJBTestUtils.newInstance(ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        ApplicationEntity testApplication = applicationDAO.addApplication(testApplicationName, null, testApplicationOwnerEntity, null,
                null, null, null);

        AttributeProviderDAO attributeProviderDAO = EJBTestUtils.newInstance(AttributeProviderDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        attributeProviderDAO.addAttributeProvider(testApplication, testAttributeType);

        AttributeProviderService attributeProviderService = EJBTestUtils.newInstance(AttributeProviderServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, Long.toString(testApplication.getId()),
                SafeOnlineApplicationRoles.APPLICATION_ROLE);

        try {
            attributeProviderService.setAttribute(testLoginSubject.getUserId(), testAttributeName, null);
            fail();
        } catch (AttributeNotFoundException e) {
            /*
             * Expected: cannot set an unexisting attribute value.
             */
        }

        try {
            attributeProviderService.setAttribute(testLoginSubject.getUserId(), testAttributeName, testAttributeValue);
            fail();
        } catch (AttributeNotFoundException e) {
            /*
             * Cannot create a nonexisting attribute via setAttribute.
             */
        }

        // operate
        attributeProviderService.createAttribute(testLoginSubject.getUserId(), testAttributeName, testAttributeValue);

        // verify
        String resultAttribute = (String) attributeProviderService.getAttributes(testLoginSubject.getUserId(), testAttributeName);
        assertEquals(testAttributeValue, resultAttribute);
    }
}
