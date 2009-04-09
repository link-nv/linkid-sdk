/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAttributeTypeService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPasswordDeviceService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPkiService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubjectService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubscriptionService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.UUID;

import javax.naming.InitialContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import test.integ.net.link.safeonline.IntegrationTestUtils;


/**
 * Integration test for SafeOnline Identifier Mapping Web Service.
 * 
 * @author fcorneli
 * 
 */
public class IdentifierMappingWebServiceTest {

    private static final Log LOG      = LogFactory.getLog(IdentifierMappingWebServiceTest.class);

    private X509Certificate  certificate;

    private PrivateKey       privateKey;

    private String           nodeName = "olas-192.168.5.11";


    @Before
    public void setUp()
            throws Exception {

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test" + UUID.randomUUID().toString());
        privateKey = keyPair.getPrivate();
    }

    @Test
    public void testMap()
            throws Exception {

        // setup
        InitialContext initialContext = IntegrationTestUtils.getInitialContext();

        IntegrationTestUtils.setupLoginConfig();

        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        IdentityService identityService = getIdentityService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);
        SubjectService subjectService = getSubjectService(initialContext);

        String testApplicationName = UUID.randomUUID().toString();

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName, loginSubject.getUserId(), password);

        AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);

        // operate: register new attribute type
        String adminUserId = subjectService.getSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN).getUserId();
        IntegrationTestUtils.login(adminUserId, "admin");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(IntegrationTestUtils.NAME_ATTRIBUTE, DatatypeType.STRING, true, true);
        try {
            attributeTypeService.add(attributeType);
        } catch (ExistingAttributeTypeException e) {
            // no worries
        }

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationService applicationService = getApplicationService(initialContext);
        applicationService.addApplication(testApplicationName, null, "owner", null, true, IdScopeType.USER, null, null,
                certificate.getEncoded(), Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                        IntegrationTestUtils.NAME_ATTRIBUTE) }), false, false, false, null, 0L);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);

        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate & verify
        NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl("https://localhost:8443", certificate, privateKey);
        client.setCaptureMessages(false);
        String resultUserId = client.getUserId(login);
        LOG.debug("userId: " + resultUserId);
        assertNotNull(resultUserId);
        assertEquals(loginSubject.getUserId(), resultUserId);
        LOG.debug("client outbound message: " + DomTestUtils.domToString(client.getOutboundMessage()));
        File tmpFile = File.createTempFile("idmapping-outbound-", ".xml");
        IOUtils.write(DomTestUtils.domToString(client.getOutboundMessage()), new FileOutputStream(tmpFile));
        LOG.debug("client inbound message: " + DomTestUtils.domToString(client.getInboundMessage()));
        tmpFile = File.createTempFile("idmapping-inbound-", ".xml");
        IOUtils.write(DomTestUtils.domToString(client.getInboundMessage()), new FileOutputStream(tmpFile));
    }

    @Test
    public void testPermissionDenied()
            throws Exception {

        // setup
        InitialContext initialContext = IntegrationTestUtils.getInitialContext();

        IntegrationTestUtils.setupLoginConfig();

        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        IdentityService identityService = getIdentityService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);

        String testApplicationName = UUID.randomUUID().toString();

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName, loginSubject.getUserId(), password);

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);

        SubjectService subjectService = getSubjectService(initialContext);
        String adminUserId = subjectService.getSubjectFromUserName("admin").getUserId();
        LOG.debug("admin userId: " + adminUserId);

        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationService applicationService = getApplicationService(initialContext);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(), Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                        IntegrationTestUtils.NAME_ATTRIBUTE) }), false, false, false, null, 0L);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);

        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate & verify
        NameIdentifierMappingClient client = new NameIdentifierMappingClientImpl("https://localhost:8443", certificate, privateKey);
        try {
            client.getUserId(login);
            fail();
        } catch (RequestDeniedException e) {
            // expected
        }
    }
}
