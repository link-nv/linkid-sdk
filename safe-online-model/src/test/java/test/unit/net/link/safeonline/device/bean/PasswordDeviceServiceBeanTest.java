package test.unit.net.link.safeonline.device.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.service.PasswordManager;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.device.bean.PasswordDeviceServiceBean;
import net.link.safeonline.entity.HistoryEventType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

import org.easymock.EasyMock;

public class PasswordDeviceServiceBeanTest extends TestCase {

	private PasswordDeviceServiceBean testedInstance;

	private Object[] mockObjects;

	private HistoryDAO mockHistoryDAO;

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

		this.mockPasswordManager = createMock(PasswordManager.class);
		EJBTestUtils.inject(this.testedInstance, this.mockPasswordManager);

		this.mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
		EJBTestUtils.inject(this.testedInstance, this.mockSecurityAuditLogger);

		EJBTestUtils.init(this.testedInstance);

		this.mockObjects = new Object[] { this.mockSubjectService,
				this.mockPasswordManager, this.mockHistoryDAO,
				this.mockSecurityAuditLogger };
	}

	public void testAuthenticate() throws Exception {
		// setup
		String login = "test-login";
		String password = "test-password";

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectService.getSubjectFromUserName(login))
				.andStubReturn(subject);

		expect(this.mockPasswordManager.validatePassword(subject, password))
				.andStubReturn(true);

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

		// stubs
		SubjectEntity subject = new SubjectEntity(login);
		expect(this.mockSubjectService.getSubjectFromUserName(login))
				.andStubReturn(subject);

		expect(
				this.mockPasswordManager.validatePassword(subject,
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
