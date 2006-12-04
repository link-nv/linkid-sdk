package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.bean.UserRegistrationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.test.util.EJBTestUtils;

public class UserRegistrationServiceBeanTest extends TestCase {

	private UserRegistrationServiceBean testedInstance;

	private SubjectDAO mockEntityDAO;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new UserRegistrationServiceBean();

		this.mockEntityDAO = createMock(SubjectDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockEntityDAO);

		this.mockApplicationDAO = createMock(ApplicationDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockApplicationDAO);

		this.mockSubscriptionDAO = createMock(SubscriptionDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubscriptionDAO);
	}

	public void testRegister() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";

		// stubs
		expect(this.mockEntityDAO.findSubject(login)).andStubReturn(null);

		SubjectEntity entity = new SubjectEntity(login, password);
		expect(this.mockEntityDAO.addSubject(login, password, name)).andReturn(
				entity);

		ApplicationEntity application = new ApplicationEntity(
				UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME);
		expect(
				this.mockApplicationDAO
						.findApplication(UserRegistrationService.SAFE_ONLINE_USER_APPLICATION_NAME))
				.andStubReturn(application);

		this.mockSubscriptionDAO.addSubscription(
				SubscriptionOwnerType.APPLICATION, entity, application);

		// prepare
		replay(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO);

		// operate
		this.testedInstance.registerUser(login, password, name);

		// verify
		verify(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO);
	}

	public void testRegisteringTwiceFails() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String name = "test-name";

		// stubs
		SubjectEntity existingEntity = new SubjectEntity(login, password);
		expect(this.mockEntityDAO.findSubject(login)).andStubReturn(
				existingEntity);

		// prepare
		replay(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO);

		// operate & verify
		try {
			this.testedInstance.registerUser(login, password, name);
			fail();
		} catch (ExistingUserException e) {
			// expected
			verify(this.mockEntityDAO, this.mockApplicationDAO,
					this.mockSubscriptionDAO);
		}
	}
}
