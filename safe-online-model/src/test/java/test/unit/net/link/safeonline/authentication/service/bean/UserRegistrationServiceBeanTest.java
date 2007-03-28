/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import junit.framework.TestCase;

public class UserRegistrationServiceBeanTest extends TestCase {

	private EntityTestManager entityTestManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);

		EntityManager entityManager = this.entityTestManager.getEntityManager();

		SystemInitializationStartableBean systemInit = EJBTestUtils
				.newInstance(SystemInitializationStartableBean.class,
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
		String testName = "test-name";

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);

		// operate
		userRegistrationService.registerUser(testLogin, testPassword, testName);

		// verify
		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		SubjectEntity resultSubject = subjectDAO.getSubject(testLogin);
		assertEquals(testLogin, resultSubject.getLogin());

		AttributeDAO attributeDAO = EJBTestUtils.newInstance(
				AttributeDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		AttributeEntity resultPasswordAttribute = attributeDAO.findAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, testLogin);
		assertNotNull(resultPasswordAttribute);
		assertEquals(testPassword, resultPasswordAttribute.getStringValue());
	}

	public void testRegisteringTwiceFails() throws Exception {
		// setup
		String testLogin = "test-login";
		String testPassword = "test-password";
		String testName = "test-name";

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);

		// operate
		userRegistrationService.registerUser(testLogin, testPassword, testName);

		// operate & verify
		try {
			userRegistrationService.registerUser(testLogin, testPassword,
					testName);
			fail();
		} catch (ExistingUserException e) {
			// expected
		}
	}
}
