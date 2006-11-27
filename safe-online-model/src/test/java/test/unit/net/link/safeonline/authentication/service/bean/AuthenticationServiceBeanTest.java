package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.EntityDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.EntityEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.EasyMock;

public class AuthenticationServiceBeanTest extends TestCase {

	private AuthenticationServiceBean testedInstance;

	private EntityDAO mockEntityDAO;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	private HistoryDAO mockHistoryDAO;

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

		this.mockHistoryDAO = createMock(HistoryDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockHistoryDAO);
	}

	public void testAuthenticate() throws Exception {
		// setup
		String applicationName = "test-application";
		String login = "test-login";
		String password = "test-password";

		// stubs
		EntityEntity entity = new EntityEntity(login, password);
		expect(this.mockEntityDAO.findEntity(login)).andStubReturn(entity);

		ApplicationEntity application = new ApplicationEntity(applicationName);
		expect(this.mockApplicationDAO.findApplication(applicationName))
				.andStubReturn(application);

		SubscriptionEntity subscription = new SubscriptionEntity();
		expect(this.mockSubscriptionDAO.findSubscription(entity, application))
				.andStubReturn(subscription);

		this.mockHistoryDAO.addHistoryEntry((Date) EasyMock.anyObject(),
				EasyMock.eq(entity), (String) EasyMock.anyObject());

		// prepare
		replay(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);

		// operate
		boolean result = this.testedInstance.authenticate(applicationName,
				login, password);

		// verify
		verify(this.mockEntityDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);
		assertTrue(result);
	}
}
