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

        testedInstance = new PasswordDeviceServiceBean();

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(testedInstance, mockHistoryDAO);

        mockPasswordManager = createMock(PasswordManager.class);
        EJBTestUtils.inject(testedInstance, mockPasswordManager);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockSubjectService, mockPasswordManager, mockHistoryDAO,
                mockSecurityAuditLogger };
    }

    public void testAuthenticate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";

        // stubs
        SubjectEntity subject = new SubjectEntity(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);
        expect(mockPasswordManager.isDisabled(subject)).andStubReturn(false);
        expect(mockPasswordManager.validatePassword(subject, password)).andStubReturn(true);

        // prepare
        replay(mockObjects);

        // operate
        String resultUserId = testedInstance.authenticate(userId, password);

        // verify
        verify(mockObjects);
        assertNotNull(resultUserId);
    }

    public void testAuthenticateWithWrongPasswordFails()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String wrongPassword = "foobar";

        // stubs
        SubjectEntity subject = new SubjectEntity(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);
        expect(mockPasswordManager.isDisabled(subject)).andStubReturn(false);
        expect(mockPasswordManager.validatePassword(subject, wrongPassword)).andStubReturn(false);

        // expectations
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, userId, "incorrect password");

        // prepare
        replay(mockObjects);

        // operate
        String resultUserId = testedInstance.authenticate(userId, wrongPassword);

        // verify
        verify(mockObjects);
        assertNull(resultUserId);
    }

}
