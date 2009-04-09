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
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.link.safeonline.SafeOnlineApplicationRoles;
import net.link.safeonline.Startable;
import net.link.safeonline.authentication.service.ApplicationPoolService;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.authentication.service.SessionTrackingService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.ApplicationPoolServiceBean;
import net.link.safeonline.authentication.service.bean.ApplicationServiceBean;
import net.link.safeonline.authentication.service.bean.NodeServiceBean;
import net.link.safeonline.authentication.service.bean.SessionTrackingServiceBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.common.SafeOnlineRoles;
import net.link.safeonline.dao.SessionTrackingDAO;
import net.link.safeonline.dao.bean.SessionTrackingDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.DeviceService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.AttributeTypeServiceBean;
import net.link.safeonline.service.bean.DeviceServiceBean;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.SafeOnlineTestConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class SessionTrackingServiceBeanTest {

    private static final Log  LOG = LogFactory.getLog(SessionTrackingServiceBeanTest.class);

    private EntityTestManager entityTestManager;

    private KeyService        mockKeyService;

    private JndiTestUtils     jndiTestUtils;


    @Before
    public void setUp()
            throws Exception {

        entityTestManager = new EntityTestManager();
        entityTestManager.setUp(SafeOnlineTestContainer.entities);

        EntityManager entityManager = entityTestManager.getEntityManager();

        mockKeyService = createMock(KeyService.class);

        final KeyPair nodeKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        checkOrder(mockKeyService, false);
        replay(mockKeyService);

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        SafeOnlineTestConfig.loadTestNode(new URL("http://127.0.0.1/"));
        Startable systemStartable = EJBTestUtils.newInstance(SystemInitializationStartableBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        systemStartable.postStart();

        verify(mockKeyService);

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();
    }

    @After
    public void tearDown()
            throws Exception {

        entityTestManager.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void testGetAssertions()
            throws Exception {

        // setup
        String session = UUID.randomUUID().toString();
        String ssoId = UUID.randomUUID().toString();

        String testSubjectLogin = UUID.randomUUID().toString();
        String testApplicationName = "test-application";
        String testApplicationPoolName = "test-application-pool";
        String testNodeName = "test-node";
        String testDeviceName = "test-device";
        String testDeviceClassName = "tes-device-class";

        EntityManager entityManager = entityTestManager.getEntityManager();

        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        userRegistrationService.registerUser(testSubjectLogin);

        SubjectService subjectService = EJBTestUtils.newInstance(SubjectServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);
        SubjectEntity subject = subjectService.findSubjectFromUserName(testSubjectLogin);

        ApplicationService applicationService = EJBTestUtils.newInstance(ApplicationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        ApplicationEntity testApplication = applicationService.addApplication(testApplicationName, null, "owner", null, false, false,
                IdScopeType.USER, null, null, null, null, false, false, false, null, 0L);

        ApplicationPoolService applicationPoolService = EJBTestUtils.newInstance(ApplicationPoolServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        ApplicationPoolEntity testApplicationPool = applicationPoolService.addApplicationPool(testApplicationPoolName, 100 * 60 * 60 * 24L,
                Collections.singletonList(testApplicationName));

        AttributeTypeEntity deviceAttributeType = new AttributeTypeEntity("test-attribute-type", DatatypeType.STRING, true, false);
        AttributeTypeService attributeTypeService = EJBTestUtils.newInstance(AttributeTypeServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, "test-operator", SafeOnlineRoles.GLOBAL_OPERATOR_ROLE);
        attributeTypeService.add(deviceAttributeType);

        NodeService nodeService = EJBTestUtils.newInstance(NodeServiceBean.class, SafeOnlineTestContainer.sessionBeans, entityManager,
                "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        nodeService.addNode(testNodeName, "http", "localhost", 8080, 8443, null);

        DeviceService deviceService = EJBTestUtils.newInstance(DeviceServiceBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager, "test-operator", SafeOnlineRoles.OPERATOR_ROLE);
        deviceService.addDeviceClass(testDeviceClassName, testDeviceClassName);
        deviceService.addDevice(testDeviceName, testDeviceClassName, testNodeName, "foo", null, "foo", null, null, null, null,
                deviceAttributeType.getName(), null, null);
        DeviceEntity testDevice = deviceService.getDevice(testDeviceName);

        // add session trackers + session assertions
        SessionTrackingDAO sessionTrackingDAO = EJBTestUtils.newInstance(SessionTrackingDAOBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        SessionTrackingEntity tracker = sessionTrackingDAO.addTracker(testApplication, session, ssoId, testApplicationPool);
        Date origTimestamp = tracker.getTimestamp();
        SessionAssertionEntity assertion = sessionTrackingDAO.addAssertion(ssoId, testApplicationPool);
        assertion.setSubject(subject);
        SessionAuthnStatementEntity statement = sessionTrackingDAO.addAuthnStatement(assertion, new DateTime(), testDevice);
        assertion.getStatements().add(statement);

        Thread.sleep(1000);

        SessionTrackingService sessionTrackingService = EJBTestUtils.newInstance(SessionTrackingServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager, Long.toString(testApplication.getId()),
                SafeOnlineApplicationRoles.APPLICATION_ROLE);

        // operate
        List<SessionAssertionEntity> assertions = sessionTrackingService.getAssertions(session, subject.getUserId(),
                new LinkedList<String>());

        // verify
        assertEquals(1, assertions.size());
        SessionAssertionEntity resultAssertion = assertions.get(0);
        assertEquals(subject, resultAssertion.getSubject());
        assertEquals(testApplicationPool, resultAssertion.getApplicationPool());
        assertEquals(1, resultAssertion.getStatements().size());
        SessionAuthnStatementEntity resultStatement = resultAssertion.getStatements().get(0);
        assertEquals(testDevice, resultStatement.getDevice());

        sessionTrackingDAO = EJBTestUtils.newInstance(SessionTrackingDAOBean.class, SafeOnlineTestContainer.sessionBeans, entityManager);
        SessionTrackingEntity resultTracker = sessionTrackingDAO.findTracker(testApplication, session, ssoId, testApplicationPool);
        LOG.debug("orig tracker time: " + origTimestamp.toString());
        LOG.debug("new  tracker time: " + resultTracker.getTimestamp().toString());
        assertTrue(origTimestamp.compareTo(resultTracker.getTimestamp()) < 0);
    }
}
