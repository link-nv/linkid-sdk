/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.ejb.EJBException;
import javax.persistence.EntityManager;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationOwnerDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.dao.bean.ApplicationDAOBean;
import net.link.safeonline.dao.bean.ApplicationOwnerDAOBean;
import net.link.safeonline.dao.bean.AttributeDAOBean;
import net.link.safeonline.dao.bean.AttributeTypeDAOBean;
import net.link.safeonline.dao.bean.SubjectDAOBean;
import net.link.safeonline.dao.bean.SubscriptionDAOBean;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.EntityTestManager;

public class UserRegistrationServiceBeanTest extends TestCase {

	private UserRegistrationServiceBean testedInstance;

	private SubjectDAO mockSubjectDAO;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	private AttributeDAO mockAttributeDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new UserRegistrationServiceBean();

		this.mockSubjectDAO = createMock(SubjectDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectDAO);

		this.mockApplicationDAO = createMock(ApplicationDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockApplicationDAO);

		this.mockSubscriptionDAO = createMock(SubscriptionDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubscriptionDAO);

		this.mockAttributeDAO = createMock(AttributeDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockAttributeDAO);
	}

	public void testRegister() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";

		// stubs
		expect(this.mockSubjectDAO.findSubject(login)).andStubReturn(null);

		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectDAO.addSubject(login)).andReturn(subject);

		SubjectEntity adminSubject = new SubjectEntity("admin-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"test-application-owner", adminSubject);

		ApplicationEntity application = new ApplicationEntity(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
				applicationOwner);
		expect(
				this.mockApplicationDAO
						.findApplication(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME))
				.andStubReturn(application);

		this.mockAttributeDAO.addAttribute(
				SafeOnlineConstants.PASSWORD_ATTRIBUTE, login, password);
		this.mockAttributeDAO.addAttribute(SafeOnlineConstants.NAME_ATTRIBUTE,
				login, name);

		this.mockSubscriptionDAO.addSubscription(
				SubscriptionOwnerType.APPLICATION, subject, application);

		// prepare
		replay(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockAttributeDAO);

		// operate
		this.testedInstance.registerUser(login, password, name);

		// verify
		verify(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockAttributeDAO);
	}

	public void testRegisteringTwiceFails() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";

		// stubs
		SubjectEntity existingSubject = new SubjectEntity(login);
		expect(this.mockSubjectDAO.findSubject(login)).andStubReturn(
				existingSubject);

		// prepare
		replay(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockAttributeDAO);

		// operate & verify
		try {
			this.testedInstance.registerUser(login, password, name);
			fail();
		} catch (ExistingUserException e) {
			// expected
			verify(this.mockSubjectDAO, this.mockApplicationDAO,
					this.mockSubscriptionDAO, this.mockAttributeDAO);
		}
	}

	public void testNonExistingSafeOnlineUserApplicationThrowsException()
			throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";

		// stubs
		expect(this.mockSubjectDAO.findSubject(login)).andStubReturn(null);

		expect(this.mockApplicationDAO.findApplication("safe-online-user"))
				.andStubReturn(null);

		// prepare
		replay(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockAttributeDAO);

		// operate & verify
		try {
			this.testedInstance.registerUser(login, password, name);
			fail();
		} catch (EJBException e) {
			// expected
			verify(this.mockSubjectDAO, this.mockApplicationDAO,
					this.mockSubscriptionDAO, this.mockAttributeDAO);
		}
	}

	public void testRegisterViaEjb3TestMicroContainer() throws Exception {
		String login = "login";
		String password = "password";
		String name = "name";
		Class[] container = new Class[] { SubjectDAOBean.class,
				ApplicationDAOBean.class, SubscriptionDAOBean.class,
				AttributeDAOBean.class };

		EntityTestManager entityTestManager = new EntityTestManager();
		entityTestManager.setUp(SubjectEntity.class, ApplicationEntity.class,
				ApplicationOwnerEntity.class, AttributeEntity.class,
				AttributeTypeEntity.class, SubscriptionEntity.class);
		EntityManager entityManager = entityTestManager.getEntityManager();

		UserRegistrationService userRegistrationService = EJBTestUtils
				.newInstance(UserRegistrationServiceBean.class, container,
						entityManager);

		/*
		 * Basically we're doing the work of SystemInitializationStartableBean
		 * here.
		 */
		ApplicationDAO applicationDAO = entityTestManager
				.newInstance(ApplicationDAOBean.class);
		ApplicationOwnerDAO applicationOwnerDAO = entityTestManager
				.newInstance(ApplicationOwnerDAOBean.class);
		SubjectDAO subjectDAO = entityTestManager
				.newInstance(SubjectDAOBean.class);
		SubjectEntity testAdmin = subjectDAO.addSubject("test-admin");
		applicationOwnerDAO.addApplicationOwner("test-owner", testAdmin);
		ApplicationOwnerEntity applicationOwner = applicationOwnerDAO
				.findApplicationOwner("test-owner");
		AttributeTypeDAO attributeTypeDAO = entityTestManager
				.newInstance(AttributeTypeDAOBean.class);
		attributeTypeDAO
				.addAttributeType(new AttributeTypeEntity(
						SafeOnlineConstants.PASSWORD_ATTRIBUTE, "string",
						false, false));
		attributeTypeDAO.addAttributeType(new AttributeTypeEntity(
				SafeOnlineConstants.NAME_ATTRIBUTE, "string", false, false));

		applicationDAO.addApplication(
				SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
				applicationOwner, null, null);

		userRegistrationService.registerUser(login, password, name);

		entityTestManager.tearDown();
	}
}
