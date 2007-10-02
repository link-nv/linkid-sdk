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
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.PasswordManagerBean;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.bean.SubjectDAOBean;
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

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);

		// operate
		userRegistrationService.registerUser(testLogin, testPassword);

		// verify
		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		SubjectEntity resultSubject = subjectDAO.getSubject(testLogin);
		assertEquals(testLogin, resultSubject.getLogin());

		PasswordManager passwordManager = EJBTestUtils.newInstance(
				PasswordManagerBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);

		boolean isPasswordConfigured = passwordManager
				.isPasswordConfigured(resultSubject);
		assertTrue(isPasswordConfigured);

		boolean isPasswordCorrect = passwordManager.validatePassword(
				resultSubject, testPassword);

		assertTrue(isPasswordCorrect);

	}

	public void testRegisteringTwiceFails() throws Exception {
		// setup
		String testLogin = "test-login";
		String testPassword = "test-password";

		EntityManager entityManager = this.entityTestManager.getEntityManager();
		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager);

		// operate
		userRegistrationService.registerUser(testLogin, testPassword);

		// operate & verify
		try {
			userRegistrationService.registerUser(testLogin, testPassword);
			fail();
		} catch (ExistingUserException e) {
			// expected
		}
	}
}
