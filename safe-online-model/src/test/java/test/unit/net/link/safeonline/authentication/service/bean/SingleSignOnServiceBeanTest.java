/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package test.unit.net.link.safeonline.authentication.service.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.security.Security;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;

import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.service.AuthenticationAssertion;
import net.link.safeonline.authentication.service.SingleSignOnState;
import net.link.safeonline.authentication.service.bean.SingleSignOnServiceBean;
import net.link.safeonline.authentication.service.bean.SingleSignOnServiceBean.SingleSignOn;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.dao.ApplicationPoolDAO;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.dao.SessionTrackingDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationPoolEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.entity.sessiontracking.SessionAssertionEntity;
import net.link.safeonline.entity.sessiontracking.SessionAuthnStatementEntity;
import net.link.safeonline.entity.sessiontracking.SessionTrackingEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * <h2>{@link SingleSignOnServiceBeanTest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Mar 27, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class SingleSignOnServiceBeanTest {

    public SingleSignOnServiceBean testedInstance;

    private Object[]               mockObjects;

    private SecurityAuditLogger    mockSecurityAuditLogger;

    private SubjectService         mockSubjectService;

    private ApplicationPoolDAO     mockApplicationPoolDAO;

    private ApplicationDAO         mockApplicationDAO;

    private DeviceDAO              mockDeviceDAO;

    private SessionTrackingDAO     mockSessionTrackingDAO;

    private Long                   ssoTimeout = 1000L * 60 * 5;


    @BeforeClass
    public static void oneTimeSetup()
            throws Exception {

        if (null == Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Before
    public void setUp()
            throws Exception {

        testedInstance = new SingleSignOnServiceBean();

        mockSecurityAuditLogger = createMock(SecurityAuditLogger.class);
        EJBTestUtils.inject(testedInstance, mockSecurityAuditLogger);

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockApplicationPoolDAO = createMock(ApplicationPoolDAO.class);
        EJBTestUtils.inject(testedInstance, mockApplicationPoolDAO);

        mockApplicationDAO = createMock(ApplicationDAO.class);
        EJBTestUtils.inject(testedInstance, mockApplicationDAO);

        mockDeviceDAO = createMock(DeviceDAO.class);
        EJBTestUtils.inject(testedInstance, mockDeviceDAO);

        mockSessionTrackingDAO = createMock(SessionTrackingDAO.class);
        EJBTestUtils.inject(testedInstance, mockSessionTrackingDAO);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockSecurityAuditLogger, mockSubjectService, mockApplicationPoolDAO, mockApplicationDAO,
                mockDeviceDAO, mockSessionTrackingDAO };
    }

    @Test
    public void testLoginSsoDisabled()
            throws Exception {

        // setup
        String applicationPoolName = "test-application-pool";
        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(false);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPoolName), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(null);

        // verify
        assertNull(assertions);
        assertEquals(SingleSignOnState.FAILED, testedInstance.getState());
    }

    @Test
    public void testInvalidAudience()
            throws Exception {

        // setup
        String applicationPoolName = "test-application-pool";
        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPoolName)).andReturn(null);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION,
                SingleSignOnServiceBean.SECURITY_MESSAGE_INVALID_APPLICATION_POOL + applicationPoolName);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPoolName), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(null);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FAILED, testedInstance.getState());
    }

    @Test
    public void testEmptyApplicationPools()
            throws Exception {

        // setup
        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        // expectations
        expect(mockApplicationPoolDAO.listApplicationPools(application)).andReturn(new LinkedList<ApplicationPoolEntity>());

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, null, application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(null);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FAILED, testedInstance.getState());
    }

    @Test
    public void testAllInvalidCookies()
            throws Exception {

        // setup
        String applicationPoolName = "test-application-pool";
        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity(applicationPoolName, ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        List<Cookie> cookies = new LinkedList<Cookie>();
        Cookie invalidCookie1 = getInvalidSsoCookie(applicationPoolName);
        Cookie invalidCookie2 = new Cookie("foo", "bar");
        cookies.add(invalidCookie1);
        cookies.add(invalidCookie2);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPoolName)).andReturn(applicationPool);
        mockSecurityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, SingleSignOnServiceBean.SECURITY_MESSAGE_INVALID_COOKIE);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPoolName), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FAILED, testedInstance.getState());
        assertEquals(cookies, testedInstance.getInvalidCookies());
    }

    @Test
    public void testLoginNoCommonApplicationPools()
            throws Exception {

        // setup
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationPoolEntity cookieApplicationPool = new ApplicationPoolEntity("test-cookie-application-pool", ssoTimeout);
        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        Cookie cookie = getSsoCookie(subject, cookieApplicationPool, cookieApplication, cookieDevice);
        cookies.add(cookie);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(cookieApplicationPool.getName())).andReturn(cookieApplicationPool);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPool.getName()), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FORCE_AUTHENTICATION, testedInstance.getState());
    }

    @Test
    public void testLoginEmptyAfterDeviceRestriction()
            throws Exception {

        // setup
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());
        DeviceEntity device = new DeviceEntity();
        device.setName("test-device-name");

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        Cookie cookie = getSsoCookie(subject, applicationPool, cookieApplication, cookieDevice);
        cookies.add(cookie);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPool.getName()), application,
                Collections.singleton(device));
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FORCE_AUTHENTICATION, testedInstance.getState());
    }

    @Test
    public void testLoginExpiredCookie()
            throws Exception {

        // setup
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        Cookie expiredCookie = getExpiredSsoCookie(subject, applicationPool, cookieApplication, cookieDevice);
        cookies.add(expiredCookie);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPool.getName()), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FORCE_AUTHENTICATION, testedInstance.getState());
        assertEquals(1, testedInstance.getInvalidCookies().size());
        assertTrue(testedInstance.getInvalidCookies().contains(expiredCookie));
    }

    @Test
    public void testLoginForceAuthentication()
            throws Exception {

        // setup
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        Cookie cookie = getSsoCookie(subject, applicationPool, cookieApplication, cookieDevice);
        cookies.add(cookie);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(true, null, Collections.singletonList(applicationPool.getName()), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNull(assertions);
        assertEquals(SingleSignOnState.FORCE_AUTHENTICATION, testedInstance.getState());
        assertEquals(0, testedInstance.getInvalidCookies().size());
    }

    @Test
    public void testLoginSuccess()
            throws Exception {

        // setup
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        cookies.add(getSsoCookie(subject, applicationPool, cookieApplication, cookieDevice));

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPool.getName()), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNotNull(assertions);
        assertEquals(1, assertions.size());
        AuthenticationAssertion assertion = assertions.get(0);
        assertEquals(subject, assertion.getSubject());
        assertTrue(assertion.getAuthentications().values().contains(cookieDevice));
        assertEquals(SingleSignOnState.SUCCESS, testedInstance.getState());
        assertEquals(0, testedInstance.getInvalidCookies().size());
        assertEquals(1, testedInstance.getCookies().size());
    }

    @Test
    public void testLoginSuccessWithSessionTracking()
            throws Exception {

        // setup
        String session = UUID.randomUUID().toString();

        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        cookies.add(getSsoCookie(subject, applicationPool, cookieApplication, cookieDevice));

        SessionTrackingEntity tracker = new SessionTrackingEntity();
        SessionAssertionEntity sessionAssertion = new SessionAssertionEntity();
        sessionAssertion.setStatements(new LinkedList<SessionAuthnStatementEntity>());
        SessionAuthnStatementEntity sessionStatement = new SessionAuthnStatementEntity();

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice);
        expect(
                mockSessionTrackingDAO.findTracker(EasyMock.eq(application), EasyMock.eq(session), (String) EasyMock.anyObject(),
                        EasyMock.eq(applicationPool))).andReturn(null);
        expect(
                mockSessionTrackingDAO.addTracker(EasyMock.eq(application), EasyMock.eq(session), (String) EasyMock.anyObject(),
                        EasyMock.eq(applicationPool))).andReturn(tracker);
        expect(mockSessionTrackingDAO.findAssertion((String) EasyMock.anyObject(), EasyMock.eq(applicationPool))).andReturn(null);
        expect(mockSessionTrackingDAO.addAssertion((String) EasyMock.anyObject(), EasyMock.eq(applicationPool)))
                                                                                                                .andReturn(sessionAssertion);
        expect(
                mockSessionTrackingDAO.addAuthnStatement(EasyMock.eq(sessionAssertion), (DateTime) EasyMock.anyObject(),
                        EasyMock.eq(cookieDevice))).andReturn(sessionStatement);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, session, Collections.singletonList(applicationPool.getName()), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNotNull(assertions);
        assertEquals(1, assertions.size());
        AuthenticationAssertion assertion = assertions.get(0);
        assertEquals(subject, assertion.getSubject());
        assertTrue(assertion.getAuthentications().values().contains(cookieDevice));
        assertEquals(SingleSignOnState.SUCCESS, testedInstance.getState());
        assertEquals(0, testedInstance.getInvalidCookies().size());
        List<Cookie> resultCookies = testedInstance.getCookies();
        assertEquals(2, resultCookies.size());
        boolean ssoIdCookieFound = false;
        for (Cookie cookie : resultCookies) {
            if (cookie.getName().equals(SingleSignOnServiceBean.SSO_ID_COOKIE_NAME)) {
                ssoIdCookieFound = true;
                break;
            }
        }
        assertTrue(ssoIdCookieFound);
    }

    @Test
    public void testLoginSuccessMultipleUsers()
            throws Exception {

        // setup
        SubjectEntity subject1 = new SubjectEntity(UUID.randomUUID().toString());
        SubjectEntity subject2 = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setSsoEnabled(true);

        ApplicationEntity cookieApplication = new ApplicationEntity();
        cookieApplication.setId(1L);
        cookieApplication.setSsoEnabled(true);
        DeviceEntity cookieDevice = new DeviceEntity();
        cookieDevice.setName("test-cookie-device-name");
        List<Cookie> cookies = new LinkedList<Cookie>();
        Cookie cookie1 = getSsoCookie(subject1, applicationPool, cookieApplication, cookieDevice);
        Cookie cookie2 = getSsoCookie(subject2, applicationPool, cookieApplication, cookieDevice);
        cookies.add(cookie1);
        cookies.add(cookie2);

        // expectations
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockSubjectService.findSubject(subject1.getUserId())).andReturn(subject1);
        expect(mockSubjectService.findSubject(subject2.getUserId())).andReturn(subject2);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool).times(2);
        expect(mockApplicationDAO.findApplication(cookieApplication.getId())).andReturn(cookieApplication).times(2);
        expect(mockDeviceDAO.findDevice(cookieDevice.getName())).andReturn(cookieDevice).times(2);

        // replay
        replay(mockObjects);

        // operate
        testedInstance.initialize(false, null, Collections.singletonList(applicationPool.getName()), application, null);
        List<AuthenticationAssertion> assertions = testedInstance.signOn(cookies);

        // verify
        verify(mockObjects);

        assertNotNull(assertions);
        assertEquals(2, assertions.size());
        for (AuthenticationAssertion assertion : assertions) {
            assertTrue(assertion.getSubject().equals(subject1) || assertion.getSubject().equals(subject2));
            assertTrue(assertion.getAuthentications().values().contains(cookieDevice));
        }
        assertEquals(SingleSignOnState.SELECT_USER, testedInstance.getState());
        assertEquals(0, testedInstance.getInvalidCookies().size());
    }

    @Test
    public void testGetApplicationToLogout()
            throws Exception {

        // setup
        SubjectEntity subject = new SubjectEntity(UUID.randomUUID().toString());

        ApplicationPoolEntity applicationPool = new ApplicationPoolEntity("test-application-pool", ssoTimeout);

        ApplicationEntity application = new ApplicationEntity();
        application.setId(1L);
        application.setSsoEnabled(true);

        DeviceEntity device = new DeviceEntity();
        device.setName("test-device-name");

        List<ApplicationEntity> ssoApplications = new LinkedList<ApplicationEntity>();
        ApplicationEntity ssoApplication1 = new ApplicationEntity();
        ssoApplication1.setId(2L);
        ssoApplication1.setSsoLogoutUrl(new URL("http", "test", "foo"));
        ApplicationEntity ssoApplication2 = new ApplicationEntity();
        ssoApplication2.setId(3L);
        ssoApplication2.setSsoLogoutUrl(new URL("http", "test", "foo"));
        ApplicationEntity ssoApplication3 = new ApplicationEntity();
        ssoApplication3.setId(4L);
        ssoApplication3.setSsoLogoutUrl(new URL("http", "test", "foo"));
        ssoApplications.add(ssoApplication1);
        ssoApplications.add(ssoApplication2);
        ssoApplications.add(ssoApplication3);

        List<Cookie> cookies = new LinkedList<Cookie>();
        cookies.add(getSsoCookie(subject, applicationPool, application, device, ssoApplications));

        // expectations
        expect(mockSubjectService.findSubject(subject.getUserId())).andReturn(subject);
        expect(mockApplicationPoolDAO.findApplicationPool(applicationPool.getName())).andReturn(applicationPool);
        expect(mockApplicationDAO.findApplication(application.getId())).andReturn(application);
        expect(mockDeviceDAO.findDevice(device.getName())).andReturn(device);
        expect(mockApplicationDAO.findApplication(ssoApplication1.getId())).andReturn(ssoApplication1);
        expect(mockApplicationDAO.findApplication(ssoApplication2.getId())).andReturn(ssoApplication2);
        expect(mockApplicationDAO.findApplication(ssoApplication3.getId())).andReturn(ssoApplication3);

        // replay
        replay(mockObjects);

        // operate
        List<ApplicationEntity> resultApplications = testedInstance.getApplicationsToLogout(application, cookies);

        // verify
        verify(mockObjects);

        assertNotNull(resultApplications);
        assertEquals(3, resultApplications.size());
        assertEquals(0, testedInstance.getInvalidCookies().size());
        for (ApplicationEntity resultApplication : resultApplications) {
            assertTrue(resultApplication.equals(ssoApplication1) || resultApplication.equals(ssoApplication2)
                    || resultApplication.equals(ssoApplication3));
        }
    }

    private Cookie getSsoCookie(SubjectEntity subject, ApplicationPoolEntity applicationPool, ApplicationEntity application,
                                DeviceEntity device)
            throws Exception {

        DateTime now = new DateTime();
        SingleSignOn sso = testedInstance.new SingleSignOn(subject, applicationPool, application, device, now);
        sso.setCookie();
        return sso.ssoCookie;
    }

    private Cookie getSsoCookie(SubjectEntity subject, ApplicationPoolEntity applicationPool, ApplicationEntity application,
                                DeviceEntity device, List<ApplicationEntity> ssoApplications)
            throws Exception {

        DateTime now = new DateTime();
        SingleSignOn sso = testedInstance.new SingleSignOn(subject, applicationPool, application, device, now);
        for (ApplicationEntity ssoApplication : ssoApplications) {
            sso.addSsoApplication(ssoApplication);
        }
        sso.setCookie();
        return sso.ssoCookie;
    }

    private Cookie getExpiredSsoCookie(SubjectEntity subject, ApplicationPoolEntity applicationPool, ApplicationEntity application,
                                       DeviceEntity device)
            throws Exception {

        DateTime expired = new DateTime().minusDays(7);
        SingleSignOn sso = testedInstance.new SingleSignOn(subject, applicationPool, application, device, expired);
        sso.setCookie();
        return sso.ssoCookie;
    }

    private Cookie getInvalidSsoCookie(String applicationPoolName)
            throws Exception {

        String value = "foo";

        BouncyCastleProvider bcp = (BouncyCastleProvider) Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        Cipher encryptCipher = Cipher.getInstance("AES", bcp);
        encryptCipher.init(Cipher.ENCRYPT_MODE, SafeOnlineNodeKeyStore.getSSOKey());
        byte[] encryptedBytes = encryptCipher.doFinal(value.getBytes("UTF-8"));
        String encryptedValue = new sun.misc.BASE64Encoder().encode(encryptedBytes);
        Cookie ssoCookie = new Cookie(SafeOnlineCookies.SINGLE_SIGN_ON_COOKIE_PREFIX + "." + applicationPoolName, encryptedValue);
        ssoCookie.setPath("/olas-auth/");
        ssoCookie.setMaxAge(-1);
        return ssoCookie;
    }

}
