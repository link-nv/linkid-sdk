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
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.authentication.service.SingleSignOnService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.authentication.service.bean.LogoutServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.keystore.service.KeyService;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.sdk.auth.saml2.LogoutResponseFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.util.ee.FieldNamingStrategy;

import org.apache.xml.security.utils.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Document;


public class LogoutServiceBeanTest {

    private LogoutServiceBean                testedInstance;

    private Object[]                         mockObjects;

    private JndiTestUtils                    jndiTestUtils;

    private SubjectService                   mockSubjectService;
    private ApplicationDAO                   mockApplicationDAO;
    private ApplicationAuthenticationService mockApplicationAuthenticationService;
    private NodeAuthenticationService        mockNodeAuthenticationService;
    private PkiValidator                     mockPkiValidator;
    private UserIdMappingService             mockUserIdMappingService;
    private SingleSignOnService              mockSingleSignOnService;
    private KeyService                       mockKeyService;

    private KeyPair                          nodeKeyPair;
    private X509Certificate                  nodeCertificate;

    private KeyPair                          applicationKeyPair;
    private X509Certificate                  applicationCert;


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

        testedInstance = new LogoutServiceBean();

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.setNamingStrategy(new FieldNamingStrategy());

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockApplicationDAO = createMock(ApplicationDAO.class);
        EJBTestUtils.inject(testedInstance, mockApplicationDAO);

        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        EJBTestUtils.inject(testedInstance, mockApplicationAuthenticationService);

        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        EJBTestUtils.inject(testedInstance, mockNodeAuthenticationService);

        mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(testedInstance, mockPkiValidator);

        mockUserIdMappingService = createMock(UserIdMappingService.class);
        EJBTestUtils.inject(testedInstance, mockUserIdMappingService);

        mockSingleSignOnService = createMock(SingleSignOnService.class);
        jndiTestUtils.bindComponent(SingleSignOnService.JNDI_BINDING, mockSingleSignOnService);

        EJBTestUtils.init(testedInstance);

        nodeKeyPair = PkiTestUtils.generateKeyPair();
        nodeCertificate = PkiTestUtils.generateSelfSignedCertificate(nodeKeyPair, "CN=Test");
        mockKeyService = createMock(KeyService.class);
        jndiTestUtils.bindComponent(KeyService.JNDI_BINDING, mockKeyService);

        mockObjects = new Object[] { mockSubjectService, mockApplicationDAO, mockApplicationAuthenticationService,
                mockNodeAuthenticationService, mockPkiValidator, mockUserIdMappingService, mockKeyService, mockSingleSignOnService };

        applicationKeyPair = PkiTestUtils.generateKeyPair();
        applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
    }

    @After
    public void tearDown()
            throws Exception {

        jndiTestUtils.tearDown();

    }

    @Test
    public void testLogout()
            throws Exception {

        /*
         * Initialize
         */

        // setup
        String session = UUID.randomUUID().toString();

        ApplicationEntity application = new ApplicationEntity();
        application.setId(0L);
        application.setName("test-application-name");
        application.setCertificate(applicationCert);
        application.setSsoEnabled(true);
        application.setSsoLogoutUrl(new URL("http", "test.host", "logout"));

        String applicationUserId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        String destinationUrl = "http://test.destination.url";

        String encodedLogoutRequest = LogoutRequestFactory.createLogoutRequest(applicationUserId, application.getName(),
                applicationKeyPair, destinationUrl, null, session);
        LogoutRequest logoutRequest = getLogoutRequest(encodedLogoutRequest);

        // expectations
        expect(mockApplicationDAO.getApplication(application.getName())).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(application.getName())).andReturn(
                Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockUserIdMappingService.findUserId(application.getId(), applicationUserId)).andStubReturn(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);

        // prepare
        replay(mockObjects);

        // operate
        LogoutProtocolContext logoutProtocolContext = testedInstance.initialize(logoutRequest);

        // verify
        verify(mockObjects);

        assertEquals(application.getName(), logoutProtocolContext.getApplicationId());
        assertEquals(application.getSsoLogoutUrl().toString(), logoutProtocolContext.getTarget());

        // reset
        reset(mockObjects);

        /*
         * Logout
         */

        // setup
        List<Cookie> ssoCookies = new LinkedList<Cookie>();

        List<ApplicationEntity> applicationsToLogout = new LinkedList<ApplicationEntity>();
        applicationsToLogout.add(getApplicationToLogout("application-to-logout-1", 1L));
        applicationsToLogout.add(getApplicationToLogout("application-to-logout-2", 2L));
        applicationsToLogout.add(getApplicationToLogout("application-to-logout-3", 3L));

        // expectations
        expect(mockSingleSignOnService.getApplicationsToLogout(session, application, ssoCookies)).andReturn(applicationsToLogout);

        // prepare
        replay(mockObjects);

        // operate
        testedInstance.logout(ssoCookies);

        // verify
        verify(mockObjects);

        List<ApplicationEntity> resultApplicationToLogout = testedInstance.getSsoApplicationsToLogout();
        assertEquals(applicationsToLogout.size(), resultApplicationToLogout.size());
        for (ApplicationEntity applicationToLogout : applicationsToLogout) {
            assertTrue(resultApplicationToLogout.contains(applicationToLogout));
        }

        // reset
        reset(mockObjects);

        /*
         * Logout applications
         */
        for (ApplicationEntity applicationToLogout : applicationsToLogout) {
            logoutApplicationTest(applicationToLogout, subject);
        }

        /*
         * Finalize Logout
         */
        // setup
        NodeEntity localNode = new NodeEntity();
        localNode.setName("local-node");

        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);

        // replay
        replay(mockObjects);

        // operate
        String encodedLogoutResponse = testedInstance.finalizeLogout();

        // verify
        verify(mockObjects);

        assertNotNull(encodedLogoutResponse);
    }

    private void logoutApplicationTest(ApplicationEntity applicationToLogout, SubjectEntity subject)
            throws Exception {

        /*
         * get logout request
         */

        // setup
        String applicationUserId = UUID.randomUUID().toString();
        NodeEntity localNode = new NodeEntity();
        localNode.setName("local-node");

        // expectations
        expect(mockNodeAuthenticationService.getLocalNode()).andReturn(localNode);
        expect(mockKeyService.getPrivateKeyEntry(SafeOnlineNodeKeyStore.class)).andReturn(
                new PrivateKeyEntry(nodeKeyPair.getPrivate(), new Certificate[] { nodeCertificate })).times(2);
        expect(mockUserIdMappingService.getApplicationUserId(applicationToLogout, subject)).andReturn(applicationUserId);

        // prepare
        replay(mockObjects);

        // operate
        String encodedLogoutRequest = testedInstance.getLogoutRequest(applicationToLogout);
        LogoutRequest logoutRequest = getLogoutRequest(new String(Base64.decode(encodedLogoutRequest)));

        // verify
        verify(mockObjects);

        assertNotNull(encodedLogoutRequest);

        // reset
        reset(mockObjects);

        /*
         * handle logout response
         */

        // setup
        String encodedLogoutResponse = LogoutResponseFactory.createLogoutResponse(logoutRequest.getID(), applicationToLogout.getName(),
                applicationKeyPair, applicationToLogout.getSsoLogoutUrl().toString());
        LogoutResponse logoutResponse = getLogoutResponse(encodedLogoutResponse);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationToLogout.getName())).andReturn(applicationToLogout);
        expect(mockApplicationAuthenticationService.getCertificates(applicationToLogout.getName())).andReturn(
                Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);

        // prepare
        replay(mockObjects);

        // operate
        String resultApplicationName = testedInstance.handleLogoutResponse(logoutResponse);

        // verify
        verify(mockObjects);

        assertEquals(applicationToLogout.getName(), resultApplicationName);

        // reset
        reset(mockObjects);

    }

    private ApplicationEntity getApplicationToLogout(String applicationName, long id)
            throws MalformedURLException {

        ApplicationEntity application = new ApplicationEntity();
        application.setId(id);
        application.setName(applicationName);
        application.setSsoEnabled(true);
        application.setSsoLogoutUrl(new URL("http", "test.host", "logout"));
        return application;

    }

    private LogoutRequest getLogoutRequest(String encodedLogoutRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedLogoutRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        LogoutRequest logoutRequest = (LogoutRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return logoutRequest;
    }

    private LogoutResponse getLogoutResponse(String encodedLogoutResponse)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedLogoutResponse);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        LogoutResponse logoutResponse = (LogoutResponse) unmarshaller.unmarshall(doc.getDocumentElement());
        return logoutResponse;
    }
}
