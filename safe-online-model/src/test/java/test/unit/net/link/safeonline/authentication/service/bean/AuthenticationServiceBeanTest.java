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

import java.util.Date;

import junit.framework.TestCase;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.EasyMock;

public class AuthenticationServiceBeanTest extends TestCase {

	private AuthenticationServiceBean testedInstance;

	private SubjectDAO mockSubjectDAO;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	private HistoryDAO mockHistoryDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new AuthenticationServiceBean();

		this.mockSubjectDAO = createMock(SubjectDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectDAO);

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
		SubjectEntity subject = new SubjectEntity(login, password);
		expect(this.mockSubjectDAO.findSubject(login)).andStubReturn(subject);

		SubjectEntity adminSubject = new SubjectEntity("admin-login",
				"admin-password");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"test-application-owner", adminSubject);

		ApplicationEntity application = new ApplicationEntity(applicationName,
				applicationOwner);
		expect(this.mockApplicationDAO.findApplication(applicationName))
				.andStubReturn(application);

		SubscriptionEntity subscription = new SubscriptionEntity();
		expect(this.mockSubscriptionDAO.findSubscription(subject, application))
				.andStubReturn(subscription);

		this.mockHistoryDAO.addHistoryEntry((Date) EasyMock.anyObject(),
				EasyMock.eq(subject), (String) EasyMock.anyObject());

		// prepare
		replay(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);

		// operate
		boolean result = this.testedInstance.authenticate(applicationName,
				login, password);

		// verify
		verify(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);
		assertTrue(result);
	}

	public void testAuthenticateWithWrongPasswordFails() throws Exception {
		// setup
		String applicationName = "test-application";
		String login = "test-login";
		String password = "test-password";
		String wrongPassword = "foobar";

		// stubs
		SubjectEntity subject = new SubjectEntity(login, password);
		expect(this.mockSubjectDAO.findSubject(login)).andStubReturn(subject);

		// expectations
		this.mockHistoryDAO.addHistoryEntry((Date) EasyMock.anyObject(),
				EasyMock.eq(subject), (String) EasyMock.anyObject());

		// prepare
		replay(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);

		// operate
		boolean result = this.testedInstance.authenticate(applicationName,
				login, wrongPassword);

		// verify
		verify(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);
		assertFalse(result);
	}

	public void testAuthenticateWithWrongUsernameFails() throws Exception {
		// setup
		String applicationName = "test-application";
		String wrongLogin = "foobar-login";
		String password = "test-password";

		// stubs
		expect(this.mockSubjectDAO.findSubject(wrongLogin)).andStubReturn(null);

		// prepare
		replay(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);

		// operate
		boolean result = this.testedInstance.authenticate(applicationName,
				wrongLogin, password);

		// verify
		verify(this.mockSubjectDAO, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO);
		assertFalse(result);
	}
}
