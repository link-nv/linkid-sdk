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
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.UUID;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.LogoutProtocolContext;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.authentication.service.bean.LogoutServiceBean;
import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.ApplicationOwnerEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.saml.common.DomUtils;
import net.link.safeonline.sdk.auth.saml2.LogoutRequestFactory;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.EJBTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.w3c.dom.Document;


public class LogoutServiceBeanTest {

    private LogoutServiceBean                testedInstance;

    private SubjectService                   mockSubjectService;

    private ApplicationDAO                   mockApplicationDAO;

    private Object[]                         mockObjects;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private PkiValidator                     mockPkiValidator;

    private UserIdMappingService             mockUserIdMappingService;


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

        mockSubjectService = createMock(SubjectService.class);
        EJBTestUtils.inject(testedInstance, mockSubjectService);

        mockApplicationDAO = createMock(ApplicationDAO.class);
        EJBTestUtils.inject(testedInstance, mockApplicationDAO);

        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        EJBTestUtils.inject(testedInstance, mockApplicationAuthenticationService);

        mockPkiValidator = createMock(PkiValidator.class);
        EJBTestUtils.inject(testedInstance, mockPkiValidator);

        mockUserIdMappingService = createMock(UserIdMappingService.class);
        EJBTestUtils.inject(testedInstance, mockUserIdMappingService);

        EJBTestUtils.init(testedInstance);

        mockObjects = new Object[] { mockSubjectService, mockApplicationDAO, mockApplicationAuthenticationService, mockPkiValidator,
                mockUserIdMappingService };
    }

    @Test
    public void initializeLogout()
            throws Exception {

        // setup
        String applicationName = "test-application-id";
        long applicationId = 1234567890;
        KeyPair applicationKeyPair = PkiTestUtils.generateKeyPair();
        X509Certificate applicationCert = PkiTestUtils.generateSelfSignedCertificate(applicationKeyPair, "CN=TestApplication");
        ApplicationEntity application = new ApplicationEntity(applicationName, null, new ApplicationOwnerEntity(), null, null, null,
                applicationCert);
        application.setId(applicationId);
        application.setSsoLogoutUrl(new URL("http", "test.host", "logout"));

        String applicationUserId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        SubjectEntity subject = new SubjectEntity(userId);

        String destinationUrl = "http://test.destination.url";

        String encodedLogoutRequest = LogoutRequestFactory.createLogoutRequest(applicationUserId, applicationName, applicationKeyPair,
                destinationUrl, null);
        LogoutRequest logoutRequest = getLogoutRequest(encodedLogoutRequest);

        // expectations
        expect(mockApplicationDAO.getApplication(applicationName)).andStubReturn(application);
        expect(mockApplicationAuthenticationService.getCertificates(applicationName)).andReturn(Collections.singletonList(applicationCert));
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, applicationCert)).andReturn(
                PkiResult.VALID);
        expect(mockUserIdMappingService.findUserId(applicationId, applicationUserId)).andStubReturn(userId);
        expect(mockSubjectService.getSubject(userId)).andStubReturn(subject);

        // prepare
        replay(mockObjects);

        // operate
        LogoutProtocolContext logoutProtocolContext = testedInstance.initialize(logoutRequest);

        // verify
        verify(mockObjects);

        assertEquals(applicationName, logoutProtocolContext.getApplicationId());
        assertEquals(application.getSsoLogoutUrl().toString(), logoutProtocolContext.getTarget());
    }

    private LogoutRequest getLogoutRequest(String encodedLogoutRequest)
            throws Exception {

        Document doc = DomUtils.parseDocument(encodedLogoutRequest);
        UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        LogoutRequest logoutRequest = (LogoutRequest) unmarshaller.unmarshall(doc.getDocumentElement());
        return logoutRequest;
    }

    // XXX: TODO: unit test handle logout response ...
}
