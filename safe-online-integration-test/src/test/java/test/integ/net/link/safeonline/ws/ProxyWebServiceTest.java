/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertEquals;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAccountService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAttributeTypeService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPasswordDeviceService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPkiService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubjectService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubscriptionService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import javax.naming.InitialContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.data.AttributeDO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import test.integ.net.link.safeonline.IntegrationTestUtils;


/**
 * Integration tests for testing the OLAS attribute and data web service with more then 1 OLAS node.
 * 
 * @author wvdhaute
 * 
 */
public class ProxyWebServiceTest {

    private static final Log LOG                 = LogFactory.getLog(DataWebServiceTest.class);

    private X509Certificate  certificate;

    private AttributeClient  attributeClient1;
    private DataClient       dataClient1;

    private AttributeClient  attributeClient2;
    private DataClient       dataClient2;

    private String           location1           = "https://sebeco-dev-22:8443";
    private String           nodeName1           = "olas-sebeco-dev-22";

    private String           location2           = "https://192.168.5.11:8443";
    private String           nodeName2           = "olas-192.168.5.11";

    private String           testApplicationName = UUID.randomUUID().toString();

    private String           testAttributeName   = "urn:integration:test:attribute";
    private String           testAttributeValue  = "test-value";


    @Before
    public void setUp()
            throws Exception {

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        attributeClient1 = new AttributeClientImpl(location1, certificate, keyPair.getPrivate());
        dataClient1 = new DataClientImpl(location1, certificate, keyPair.getPrivate());

        attributeClient2 = new AttributeClientImpl(location2, certificate, keyPair.getPrivate());
        dataClient2 = new DataClientImpl(location2, certificate, keyPair.getPrivate());
    }

    @Test
    public void proxyService()
            throws Exception {

        // setup
        InitialContext initialContext = IntegrationTestUtils.getInitialContext();

        IntegrationTestUtils.setupLoginConfig();

        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        IdentityService identityService = getIdentityService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);
        SubjectService subjectService = getSubjectService(initialContext);
        AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
        AccountService accountService = getAccountService(initialContext);
        ApplicationService applicationService = getApplicationService(initialContext);

        // operate: register new attribute type
        String adminUserId = subjectService.getSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN).getUserId();
        IntegrationTestUtils.login(adminUserId, "admin");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        try {
            attributeTypeService.add(attributeType);
        } catch (ExistingAttributeTypeException e) {
            // no worries
        }

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName2, loginSubject.getUserId(), password);

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationEntity testApplication = null;
        try {
            testApplication = applicationService.getApplication(testApplicationName);
        } catch (ApplicationNotFoundException e) {
            // okidoki
        }
        if (null != testApplication) {
            // cleanup from failure before
            applicationService.removeApplication(testApplication.getId());
        }

        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(), Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(testAttributeName) }),
                false, false, false, null);
        testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate: set attribute
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        AttributeDO attributeDO = new AttributeDO(testAttributeName, DatatypeType.STRING);
        attributeDO.setStringValue(testAttributeValue);
        attributeDO.setEditable(true);
        identityService.saveAttribute(attributeDO);

        String resultValue = attributeClient2.getAttributeValue(loginSubject.getUserId(), testAttributeName, String.class);
        assertEquals(testAttributeValue, resultValue);

        // operate: retrieve all attributes
        attributeClient2.setCaptureMessages(true);
        Map<String, Object> resultAttributes = attributeClient2.getAttributeValues(loginSubject.getUserId());
        LOG.debug("outbound message: " + DomTestUtils.domToString(attributeClient2.getOutboundMessage()));
        LOG.info("resultAttributes: " + resultAttributes);
        assertEquals(1, resultAttributes.size());
        assertEquals(testAttributeValue, resultAttributes.get(testAttributeName));

        // operate: retrieve all attributes through other OLAS node
        attributeClient1.setCaptureMessages(true);
        resultAttributes = attributeClient1.getAttributeValues(loginSubject.getUserId());
        LOG.debug("outbound message: " + DomTestUtils.domToString(attributeClient1.getOutboundMessage()));
        LOG.info("resultAttributes: " + resultAttributes);
        assertEquals(1, resultAttributes.size());
        assertEquals(testAttributeValue, resultAttributes.get(testAttributeName));

        // cleanup
        // login as admin
        IntegrationTestUtils.login(adminUserId, "admin");

        // remove user
        accountService.removeAccount(loginSubject.getUserId());

        // remove application
        applicationService.removeApplication(testApplication.getId());

        // remove attributes
        attributeTypeService.remove(attributeType);
    }
}
