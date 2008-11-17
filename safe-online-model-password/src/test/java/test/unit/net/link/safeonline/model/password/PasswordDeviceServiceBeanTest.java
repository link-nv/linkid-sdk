package test.unit.net.link.safeonline.model.password;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.UUID;

import junit.framework.TestCase;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.model.password.bean.PasswordDeviceServiceBean;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;


public class PasswordDeviceServiceBeanTest extends TestCase {

    private PasswordDeviceServiceBean testedInstance;

    private Object[]                  mockObjects;

    private HistoryDAO                mockHistoryDAO;

    private PasswordManager           mockPasswordManager;

    private SubjectService            mockSubjectService;

    private SecurityAuditLogger       mockSecurityAuditLogger;


    @Override
    protected void setUp()
            throws Exception {

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

        this.mockObjects = new Object[] { this.mockSubjectService, this.mockPasswordManager, this.mockHistoryDAO,
                this.mockSecurityAuditLogger };
    }

    public void testAuthenticate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";

        // stubs
        SubjectEntity subject = new SubjectEntity(userId);
        expect(this.mockSubjectService.getSubject(userId)).andStubReturn(subject);
        expect(this.mockPasswordManager.isDisabled(subject)).andStubReturn(false);
        expect(this.mockPasswordManager.validatePassword(subject, password)).andStubReturn(true);

        // prepare
        replay(this.mockObjects);

        // operate
        String resultUserId = this.testedInstance.authenticate(userId, password);

        // verify
        verify(this.mockObjects);
        assertNotNull(resultUserId);
    }

    public void testAuthenticateWithWrongPasswordFails()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String wrongPassword = "foobar";

        // stubs
        SubjectEntity subject = new SubjectEntity(userId);
        expect(this.mockSubjectService.getSubject(userId)).andStubReturn(subject);
        expect(this.mockPasswordManager.isDisabled(subject)).andStubReturn(false);
        expect(this.mockPasswordManager.validatePassword(subject, wrongPassword)).andStubReturn(false);

        // expectations
        this.mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, "incorrect password");

        // prepare
        replay(this.mockObjects);

        // operate
        String resultUserId = this.testedInstance.authenticate(userId, wrongPassword);

        // verify
        verify(this.mockObjects);
        assertNull(resultUserId);
    }

}
