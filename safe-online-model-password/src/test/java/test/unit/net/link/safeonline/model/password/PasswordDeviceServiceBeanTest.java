package test.unit.net.link.safeonline.model.password;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.dao.HistoryDAO;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.model.password.PasswordManager;
import net.link.safeonline.model.password.bean.PasswordDeviceServiceBean;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

import org.junit.Before;
import org.junit.Test;


public class PasswordDeviceServiceBeanTest {

    private PasswordDeviceServiceBean testedInstance;

    private Object[]                  mockObjects;

    private HistoryDAO                mockHistoryDAO;

    private PasswordManager           mockPasswordManager;

    private SubjectService            mockSubjectService;

    private SecurityAuditLogger       mockSecurityAuditLogger;

    private AttributeDAO              mockAttributeDAO;

    private AttributeTypeDAO          mockAttributeTypeDAO;


    @Before
    public void setUp()
            throws Exception {

        testedInstance = new PasswordDeviceServiceBean();

        mockAttributeDAO = createMock(AttributeDAO.class);
        EJBTestUtils.inject(testedInstance, mockAttributeDAO);

        mockAttributeTypeDAO = createMock(AttributeTypeDAO.class);
        EJBTestUtils.inject(testedInstance, mockAttributeTypeDAO);

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockPasswordManager = createMock(PasswordManager.class);
        EJBTestUtils.inject(testedInstance, mockPasswordManager);

        mockHistoryDAO = createMock(HistoryDAO.class);
        EJBTestUtils.inject(testedInstance, mockHistoryDAO);

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockAttributeDAO, mockAttributeTypeDAO, mockSubjectService, mockPasswordManager, mockHistoryDAO,
                mockSecurityAuditLogger };
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String password = "test-password";

        // stubs
        SubjectEntity subject = new SubjectEntity(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);
        expect(mockPasswordManager.validatePassword(subject, password)).andStubReturn(true);

        // prepare
        replay(mockObjects);

        // operate
        String resultUserId = testedInstance.authenticate(userId, password);

        // verify
        verify(mockObjects);
        assertNotNull(resultUserId);
    }

    @Test
    public void testAuthenticateWithWrongPasswordFails()
            throws Exception {

        // setup
        String userId = UUID.randomUUID().toString();
        String wrongPassword = "foobar";

        // stubs
        SubjectEntity subject = new SubjectEntity(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);
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
