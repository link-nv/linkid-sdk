package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.test.util.EJBTestUtils;

public class AuthenticationServiceBeanTest extends TestCase {

	private AuthenticationServiceBean testedInstance;

	private EntityDAO mockEntityDAO;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new AuthenticationServiceBean();

		this.mockEntityDAO = createMock(EntityDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockEntityDAO);

		this.mockApplicationDAO = createMock(ApplicationDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockApplicationDAO);

		this.mockSubscriptionDAO = createMock(SubscriptionDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubscriptionDAO);
	}

	public void testAuthenticate() throws Exception {
		// setup
		String applicationName = "test-application";
		String username = "test-username";
		String password = "test-password";

		// stubs
		EntityEntity entity = new EntityEntity(username, password);
		expect(this.mockEntityDAO.findEntity(username)).andStubReturn(entity);

		ApplicationEntity application = new ApplicationEntity(applicationName);
		expect(this.mockApplicationDAO.findApplication(applicationName))
				.andStubReturn(application);

		SubscriptionEntity subscription = new SubscriptionEntity();
		expect(this.mockSubscriptionDAO.findSubscription(entity, application))
				.andStubReturn(subscription);

		// prepare
		replay(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO);

		// operate
		boolean result = this.testedInstance.authenticate(applicationName,
				username, password);

		// verify
		verify(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO);
		assertTrue(result);
	}
}
