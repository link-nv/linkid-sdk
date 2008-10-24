/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import java.security.KeyPair;
import java.security.cert.X509Certificate;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.device.backend.bean.PasswordManagerBean;
import net.link.safeonline.device.bean.PasswordDeviceServiceBean;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.service.bean.SubjectServiceBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;
import net.link.safeonline.test.util.MBeanActionHandler;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.AuthIdentityServiceClient;
import net.link.safeonline.util.ee.IdentityServiceClient;
import test.unit.net.link.safeonline.SafeOnlineTestContainer;


public class UserRegistrationServiceBeanTest extends TestCase {

    private EntityTestManager entityTestManager;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.entityTestManager = new EntityTestManager();
        this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

        EntityManager entityManager = this.entityTestManager.getEntityManager();

        JmxTestUtils jmxTestUtils = new JmxTestUtils();
        jmxTestUtils.setUp(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE);

        final KeyPair authKeyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate authCertificate = PkiTestUtils.generateSelfSignedCertificate(authKeyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(AuthIdentityServiceClient.AUTH_IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return authCertificate;
            }
        });

        jmxTestUtils.setUp(IdentityServiceClient.IDENTITY_SERVICE);

        final KeyPair keyPair = PkiTestUtils.generateKeyPair();
        final X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        jmxTestUtils.registerActionHandler(IdentityServiceClient.IDENTITY_SERVICE, "getCertificate", new MBeanActionHandler() {

            public Object invoke(@SuppressWarnings("unused") Object[] arguments) {

                return certificate;
            }
        });

        SystemInitializationStartableBean systemInit = EJBTestUtils.newInstance(SystemInitializationStartableBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        systemInit.postStart();

        EntityTransaction entityTransaction = entityManager.getTransaction();
        entityTransaction.commit();
        entityTransaction.begin();
    }

    @Override
    protected void tearDown() throws Exception {

        this.entityTestManager.tearDown();

        super.tearDown();
    }

    public void testRegister() throws Exception {

        // setup
        String testLogin = "test-login";
        String testPassword = "test-password";

        EntityManager entityManager = this.entityTestManager.getEntityManager();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        PasswordDeviceService passwordDeviceService = EJBTestUtils.newInstance(PasswordDeviceServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        // operate
        SubjectEntity testSubject = userRegistrationService.registerUser(testLogin);
        passwordDeviceService.register(testSubject, testPassword);

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

        PasswordManager passwordManager = EJBTestUtils.newInstance(PasswordManagerBean.class, SafeOnlineTestContainer.sessionBeans,
                entityManager);

        boolean isPasswordConfigured = passwordManager.isPasswordConfigured(resultSubject);
        assertTrue(isPasswordConfigured);

        boolean isPasswordCorrect = passwordManager.validatePassword(resultSubject, testPassword);

        assertTrue(isPasswordCorrect);

    }

    public void testRegisteringTwiceFails() throws Exception {

        // setup
        String testLogin = "test-login";
        String testPassword = "test-password";

        EntityManager entityManager = this.entityTestManager.getEntityManager();
        UserRegistrationService userRegistrationService = EJBTestUtils.newInstance(UserRegistrationServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);
        PasswordDeviceService passwordDeviceService = EJBTestUtils.newInstance(PasswordDeviceServiceBean.class,
                SafeOnlineTestContainer.sessionBeans, entityManager);

        // operate
        SubjectEntity testSubject = userRegistrationService.registerUser(testLogin);
        passwordDeviceService.register(testSubject, testPassword);

        // operate & verify
        try {
            userRegistrationService.registerUser(testLogin);
            fail();
        } catch (ExistingUserException e) {
            // expected
        }
    }
}
