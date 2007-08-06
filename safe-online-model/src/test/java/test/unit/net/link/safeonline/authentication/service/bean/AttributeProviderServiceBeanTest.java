/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

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
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.bean.SystemInitializationStartableBean;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;
import net.link.safeonline.test.util.JmxTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.net.link.safeonline.SafeOnlineTestContainer;

public class AttributeProviderServiceBeanTest {

	private EntityTestManager entityTestManager;

	@Before
	public void setUp() throws Exception {
		JmxTestUtils.setUp("jboss.security:service=JaasSecurityManager");

		this.entityTestManager = new EntityTestManager();
		this.entityTestManager.setUp(SafeOnlineTestContainer.entities);
		EntityManager entityManager = this.entityTestManager.getEntityManager();

		Startable systemStartable = EJBTestUtils.newInstance(
				SystemInitializationStartableBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		systemStartable.postStart();
		this.entityTestManager.refreshEntityManager();
	}

	@After
	public void tearDown() throws Exception {
		this.entityTestManager.tearDown();
	}

	@Test
	public void testCreateAttributeRequiresAttributeProvider() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		String testLogin = "test-subject-login";
		String testAttributeName = "test-attribute-name";
		String[] testAttributeValue = { "hello", "world" };
		String testApplicationName = "test-application";
		String testApplicationOwner = "test-application-owner";
		String testApplicationAdmin = "test-application-admin";

		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		SubjectEntity applicationAdminSubject = subjectDAO
				.addSubject(testApplicationAdmin);

		ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(
				ApplicationOwnerDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		ApplicationOwnerEntity testApplicationOwnerEntity = applicationOwnerDAO
				.addApplicationOwner(testApplicationOwner,
						applicationAdminSubject);

		AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(
				AttributeTypeDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		AttributeTypeEntity testAttributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, false);
		attributeTypeDAO.addAttributeType(testAttributeType);

		ApplicationDAO applicationDAO = EJBTestUtils.newInstance(
				ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		applicationDAO.addApplication(testApplicationName, null,
				testApplicationOwnerEntity, null, null);

		AttributeProviderService attributeProviderService = EJBTestUtils
				.newInstance(AttributeProviderServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager,
						"test-application",
						SafeOnlineApplicationRoles.APPLICATION_ROLE);

		// operate & verify
		try {
			attributeProviderService.createAttribute(testLogin,
					testAttributeName, testAttributeValue);
			fail();
		} catch (PermissionDeniedException e) {
			// expected
		}
	}

	@Test
	public void testMultivaluedAttribute() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		String testLogin = "test-subject-login";
		String testAttributeName = "test-attribute-name";
		String value1 = "hello";
		String value2 = "world";
		String[] testAttributeValue = { value1, value2 };
		String testApplicationName = "test-application";
		String testApplicationOwner = "test-application-owner";
		String testApplicationAdmin = "test-application-admin";

		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		SubjectEntity applicationAdminSubject = subjectDAO
				.addSubject(testApplicationAdmin);
		subjectDAO.addSubject(testLogin);

		ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(
				ApplicationOwnerDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		ApplicationOwnerEntity testApplicationOwnerEntity = applicationOwnerDAO
				.addApplicationOwner(testApplicationOwner,
						applicationAdminSubject);

		AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(
				AttributeTypeDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		AttributeTypeEntity testAttributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, false);
		testAttributeType.setMultivalued(true);
		attributeTypeDAO.addAttributeType(testAttributeType);

		ApplicationDAO applicationDAO = EJBTestUtils.newInstance(
				ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		ApplicationEntity testApplication = applicationDAO.addApplication(
				testApplicationName, null, testApplicationOwnerEntity, null,
				null);

		AttributeProviderDAO attributeProviderDAO = EJBTestUtils.newInstance(
				AttributeProviderDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		attributeProviderDAO.addAttributeProvider(testApplication,
				testAttributeType);

		AttributeProviderService attributeProviderService = EJBTestUtils
				.newInstance(AttributeProviderServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager,
						"test-application",
						SafeOnlineApplicationRoles.APPLICATION_ROLE);

		try {
			attributeProviderService.setAttribute(testLogin, testAttributeName,
					null);
			fail();
		} catch (AttributeNotFoundException e) {
			/*
			 * Expected: cannot set an unexisting attribute value.
			 */
		}

		try {
			attributeProviderService.setAttribute(testLogin, testAttributeName,
					testAttributeValue);
			fail();
		} catch (AttributeNotFoundException e) {
			/*
			 * Cannot create a nonexisting attribute via setAttribute.
			 */
		}

		// operate
		attributeProviderService.createAttribute(testLogin, testAttributeName,
				testAttributeValue);

		// verify
		List<AttributeEntity> resultAttributes = attributeProviderService
				.getAttributes(testLogin, testAttributeName);
		assertEquals(testAttributeValue.length, resultAttributes.size());
		assertEquals(value1, resultAttributes.get(0).getStringValue());
		assertEquals(value2, resultAttributes.get(1).getStringValue());

		// operate
		attributeProviderService.setAttribute(testLogin, testAttributeName,
				new String[] { value2 });

		// verify
		resultAttributes = attributeProviderService.getAttributes(testLogin,
				testAttributeName);
		assertEquals(1, resultAttributes.size());
		assertEquals(value2, resultAttributes.get(0).getStringValue());

		// operate
		attributeProviderService.setAttribute(testLogin, testAttributeName,
				new String[] { value1, value2, value1 });

		// verify
		resultAttributes = attributeProviderService.getAttributes(testLogin,
				testAttributeName);
		assertEquals(3, resultAttributes.size());
		assertEquals(value1, resultAttributes.get(0).getStringValue());
		assertEquals(value2, resultAttributes.get(1).getStringValue());
		assertEquals(value1, resultAttributes.get(2).getStringValue());
	}

	@Test
	public void testCreateSingleValuedAttributes() throws Exception {
		// setup
		EntityManager entityManager = this.entityTestManager.getEntityManager();
		String testLogin = "test-subject-login";
		String testAttributeName = "test-attribute-name";
		String testAttributeValue = "test-value";
		String testApplicationName = "test-application";
		String testApplicationOwner = "test-application-owner";
		String testApplicationAdmin = "test-application-admin";

		SubjectDAO subjectDAO = EJBTestUtils.newInstance(SubjectDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		SubjectEntity applicationAdminSubject = subjectDAO
				.addSubject(testApplicationAdmin);
		subjectDAO.addSubject(testLogin);

		ApplicationOwnerDAO applicationOwnerDAO = EJBTestUtils.newInstance(
				ApplicationOwnerDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		ApplicationOwnerEntity testApplicationOwnerEntity = applicationOwnerDAO
				.addApplicationOwner(testApplicationOwner,
						applicationAdminSubject);

		AttributeTypeDAO attributeTypeDAO = EJBTestUtils.newInstance(
				AttributeTypeDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		AttributeTypeEntity testAttributeType = new AttributeTypeEntity(
				testAttributeName, DatatypeType.STRING, true, false);
		attributeTypeDAO.addAttributeType(testAttributeType);

		ApplicationDAO applicationDAO = EJBTestUtils.newInstance(
				ApplicationDAOBean.class, SafeOnlineTestContainer.sessionBeans,
				entityManager);
		ApplicationEntity testApplication = applicationDAO.addApplication(
				testApplicationName, null, testApplicationOwnerEntity, null,
				null);

		AttributeProviderDAO attributeProviderDAO = EJBTestUtils.newInstance(
				AttributeProviderDAOBean.class,
				SafeOnlineTestContainer.sessionBeans, entityManager);
		attributeProviderDAO.addAttributeProvider(testApplication,
				testAttributeType);

		AttributeProviderService attributeProviderService = EJBTestUtils
				.newInstance(AttributeProviderServiceBean.class,
						SafeOnlineTestContainer.sessionBeans, entityManager,
						"test-application",
						SafeOnlineApplicationRoles.APPLICATION_ROLE);

		try {
			attributeProviderService.setAttribute(testLogin, testAttributeName,
					null);
			fail();
		} catch (AttributeNotFoundException e) {
			/*
			 * Expected: cannot set an unexisting attribute value.
			 */
		}

		try {
			attributeProviderService.setAttribute(testLogin, testAttributeName,
					testAttributeValue);
			fail();
		} catch (AttributeNotFoundException e) {
			/*
			 * Cannot create a nonexisting attribute via setAttribute.
			 */
		}

		// operate
		attributeProviderService.createAttribute(testLogin, testAttributeName,
				testAttributeValue);

		// verify
		List<AttributeEntity> resultAttributes = attributeProviderService
				.getAttributes(testLogin, testAttributeName);
		assertEquals(testAttributeValue, resultAttributes.get(0)
				.getStringValue());
	}
}
