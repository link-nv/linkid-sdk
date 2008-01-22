/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.authentication.service.bean;

import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticDomain;
import static net.link.safeonline.model.bean.UsageStatisticTaskBean.statisticName;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.service.bean.AuthenticationServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.dao.StatisticDAO;
import net.link.safeonline.dao.StatisticDataPointDAO;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.device.PasswordDeviceService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.StatisticDataPointEntity;
import net.link.safeonline.entity.StatisticEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

public class AuthenticationServiceBeanTest extends TestCase {

	private AuthenticationServiceBean testedInstance;

	private SubjectService mockSubjectService;

	private PasswordDeviceService mockPasswordDeviceService;

	private ApplicationDAO mockApplicationDAO;

	private SubscriptionDAO mockSubscriptionDAO;

	private HistoryDAO mockHistoryDAO;

	private Object[] mockObjects;

	private StatisticDAO mockStatisticDAO;

	private StatisticDataPointDAO mockStatisticDataPointDAO;

	private DeviceDAO mockDeviceDAO;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new AuthenticationServiceBean();

		this.mockSubjectService = createMock(SubjectService.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectService);

		this.mockPasswordDeviceService = createMock(PasswordDeviceService.class);
		EJBTestUtils
				.inject(this.testedInstance, this.mockPasswordDeviceService);

		this.mockApplicationDAO = createMock(ApplicationDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockApplicationDAO);

		this.mockSubscriptionDAO = createMock(SubscriptionDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubscriptionDAO);

		this.mockHistoryDAO = createMock(HistoryDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockHistoryDAO);

		this.mockStatisticDAO = createMock(StatisticDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockStatisticDAO);

		this.mockStatisticDataPointDAO = createMock(StatisticDataPointDAO.class);
		EJBTestUtils
				.inject(this.testedInstance, this.mockStatisticDataPointDAO);

		this.mockDeviceDAO = createMock(DeviceDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockDeviceDAO);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockSubjectService,
				this.mockPasswordDeviceService, this.mockApplicationDAO,
				this.mockSubscriptionDAO, this.mockHistoryDAO,
				this.mockStatisticDAO, this.mockStatisticDataPointDAO,
				this.mockDeviceDAO };
	}

	public void testAuthenticate() throws Exception {
		// setup
		String applicationName = "test-application";
		String login = "test-login";
		String password = "test-password";

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectService.getSubjectFromUserName(login))
				.andStubReturn(subject);

		SubjectEntity adminSubject = new SubjectEntity("admin-login");
		ApplicationOwnerEntity applicationOwner = new ApplicationOwnerEntity(
				"test-application-owner", adminSubject);

		ApplicationEntity application = new ApplicationEntity(applicationName,
				null, applicationOwner, null, null, null, null, null);
		expect(this.mockApplicationDAO.findApplication(applicationName))
				.andStubReturn(application);

		SubscriptionEntity subscription = new SubscriptionEntity();
		expect(this.mockSubscriptionDAO.findSubscription(subject, application))
				.andStubReturn(subscription);

		expect(this.mockPasswordDeviceService.authenticate(login, password))
				.andStubReturn(subject);

		StatisticEntity statistic = new StatisticEntity();
		expect(
				this.mockStatisticDAO
						.findOrAddStatisticByNameDomainAndApplication(
								statisticName, statisticDomain, application))
				.andStubReturn(statistic);
		StatisticDataPointEntity dataPoint = new StatisticDataPointEntity();
		expect(
				this.mockStatisticDataPointDAO.findOrAddStatisticDataPoint(
						"Login counter", statistic)).andStubReturn(dataPoint);

		DeviceClassEntity deviceClass = new DeviceClassEntity(
				SafeOnlineConstants.PASSWORD_DEVICE_CLASS);
		DeviceEntity device = new DeviceEntity(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, deviceClass,
				null, null, null, null, null);
		expect(
				this.mockDeviceDAO
						.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID))
				.andReturn(device);

		// prepare
		replay(this.mockObjects);

		// operate
		boolean result = this.testedInstance.authenticate(login, password);

		// verify
		verify(this.mockObjects);
		assertTrue(result);
	}
}
