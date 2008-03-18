package test.unit.net.link.safeonline.device.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.device.backend.PasswordManager;
import net.link.safeonline.device.bean.PasswordDeviceServiceBean;
import net.link.safeonline.entity.DeviceClassEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceRegistrationEntity;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.service.DeviceRegistrationService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.EasyMock;

public class PasswordDeviceServiceBeanTest extends TestCase {

	private PasswordDeviceServiceBean testedInstance;

	private Object[] mockObjects;

	private HistoryDAO mockHistoryDAO;

	private DeviceDAO mockDeviceDAO;

	private DeviceRegistrationService mockDeviceRegistrationService;

	private PasswordManager mockPasswordManager;

	private SubjectService mockSubjectService;

	private SecurityAuditLogger mockSecurityAuditLogger;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		this.testedInstance = new PasswordDeviceServiceBean();

		this.mockSubjectService = createMock(SubjectService.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSubjectService);

		this.mockHistoryDAO = createMock(HistoryDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockHistoryDAO);

		this.mockDeviceDAO = createMock(DeviceDAO.class);
		EJBTestUtils.inject(this.testedInstance, this.mockDeviceDAO);

		this.mockDeviceRegistrationService = createMock(DeviceRegistrationService.class);
		EJBTestUtils.inject(this.testedInstance,
				this.mockDeviceRegistrationService);

		this.mockPasswordManager = createMock(PasswordManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPasswordManager);

		this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSecurityAuditLogger);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockSubjectService,
				this.mockPasswordManager, this.mockHistoryDAO,
				this.mockDeviceDAO, this.mockSecurityAuditLogger,
				this.mockDeviceRegistrationService };
	}

	public void testAuthenticate() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";
		String deviceId = UUID.randomUUID().toString();

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectService.getSubjectFromUserName(login))
				.andStubReturn(subject);

		DeviceClassEntity deviceClass = new DeviceClassEntity(
				SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
				SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
		DeviceEntity device = new DeviceEntity(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, deviceClass,
				null, null, null, null, null, null);
		expect(
				this.mockDeviceDAO
						.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID))
				.andStubReturn(device);

		DeviceRegistrationEntity deviceRegistrationEntity = new DeviceRegistrationEntity(
				subject, deviceId, device);
		List<DeviceRegistrationEntity> deviceRegistrations = new LinkedList<DeviceRegistrationEntity>();
		deviceRegistrations.add(deviceRegistrationEntity);
		expect(
				this.mockDeviceRegistrationService.listDeviceRegistrations(
						subject, device)).andStubReturn(deviceRegistrations);

		SubjectEntity deviceSubject = new SubjectEntity(deviceId);
		expect(this.mockSubjectService.getSubject(deviceId)).andStubReturn(
				deviceSubject);

		expect(
				this.mockPasswordManager.validatePassword(deviceSubject,
						password)).andStubReturn(true);

		// prepare
		replay(this.mockObjects);

		// operate
		SubjectEntity resultSubject = this.testedInstance.authenticate(login,
				password);

		// verify
		verify(this.mockObjects);
		assertNotNull(resultSubject);
	}

	public void testAuthenticateWithWrongPasswordFails() throws Exception {
		// setup
		String login = "test-login";
		String wrongPassword = "foobar";
		String deviceId = UUID.randomUUID().toString();

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectService.getSubjectFromUserName(login))
				.andStubReturn(subject);
		DeviceClassEntity deviceClass = new DeviceClassEntity(
				SafeOnlineConstants.PASSWORD_DEVICE_CLASS,
				SafeOnlineConstants.PASSWORD_DEVICE_AUTH_CONTEXT_CLASS);
		DeviceEntity device = new DeviceEntity(
				SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID, deviceClass,
				null, null, null, null, null, null);
		expect(
				this.mockDeviceDAO
						.getDevice(SafeOnlineConstants.USERNAME_PASSWORD_DEVICE_ID))
				.andStubReturn(device);

		DeviceRegistrationEntity deviceRegistrationEntity = new DeviceRegistrationEntity(
				subject, deviceId, device);
		List<DeviceRegistrationEntity> deviceRegistrations = new LinkedList<DeviceRegistrationEntity>();
		deviceRegistrations.add(deviceRegistrationEntity);
		expect(
				this.mockDeviceRegistrationService.listDeviceRegistrations(
						subject, device)).andStubReturn(deviceRegistrations);

		SubjectEntity deviceSubject = new SubjectEntity(deviceId);
		expect(this.mockSubjectService.getSubject(deviceId)).andStubReturn(
				deviceSubject);

		expect(
				this.mockPasswordManager.validatePassword(deviceSubject,
						wrongPassword)).andStubReturn(false);

		// expectations
		this.mockHistoryDAO.addHistoryEntry(EasyMock.eq(subject),
				(HistoryEventType) EasyMock.anyObject(), (String) EasyMock
						.anyObject(), (String) EasyMock.anyObject());

		this.mockSecurityAuditLogger.addSecurityAudit(
				SecurityThreatType.DECEPTION, login, "incorrect password");

		// prepare
		replay(this.mockObjects);

		// operate
		SubjectEntity resultSubject = this.testedInstance.authenticate(login,
				wrongPassword);

		// verify
		verify(this.mockObjects);
		assertNull(resultSubject);
	}

}
