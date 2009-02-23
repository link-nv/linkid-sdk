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
import static test.integ.net.link.safeonline.IntegrationTestUtils.getIdentityService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getNodeMappingService;
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
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.IdentityService;
import net.link.safeonline.authentication.service.SubscriptionService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.pkix.service.PkiService;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.service.NodeMappingService;
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

    private AttributeClient  attributeClient2;

    private String           location1           = "sebeco-dev-22";
    private String           nodeName1           = "olas-sebeco-dev-22";

    private String           location2           = "192.168.5.11";
    private String           wslocation2         = "https://" + location2 + ":8443";
    private String           nodeName2           = "olas-192.168.5.11";

    private String           testApplicationName = "integration-test-app";


    @Before
    public void setUp()
            throws Exception {

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        attributeClient2 = new AttributeClientImpl(wslocation2, certificate, keyPair.getPrivate());
        attributeClient2.setCaptureMessages(true);
    }

    /**
     * This test will:
     * <ol>
     * <li>register application and its X5095 @ both locations
     * <li>register subject @ location 1 ( also add password, needed to login )
     * <li>subscribe subject to application @ location 2
     * <li>create node mapping @ location 1
     * <li>register password @ location2 using node mapping id from location 1( creates node mapping @ location 2 )
     * <li>subscribe subject to application @ location 2
     * <li>fetch attribute value @ location 2 through attribute ws ( proxies to location 1 )
     * </ol>
     * 
     */
    @Test
    public void proxyService()
            throws Exception {

        // setup
        InitialContext initialContext1 = IntegrationTestUtils.getInitialContext(location1);
        InitialContext initialContext2 = IntegrationTestUtils.getInitialContext(location2);

        IntegrationTestUtils.setupLoginConfig();

        SubjectService subjectService1 = getSubjectService(initialContext1);
        PkiService pkiService1 = getPkiService(initialContext1);
        ApplicationService applicationService1 = getApplicationService(initialContext1);
        UserRegistrationService userRegistrationService1 = getUserRegistrationService(initialContext1);
        PasswordDeviceService passwordDeviceService1 = getPasswordDeviceService(initialContext1);
        NodeMappingService nodeMappingService1 = getNodeMappingService(initialContext1);
        AccountService accountService1 = getAccountService(initialContext1);

        SubjectService subjectService2 = getSubjectService(initialContext2);
        PkiService pkiService2 = getPkiService(initialContext2);
        ApplicationService applicationService2 = getApplicationService(initialContext2);
        PasswordDeviceService passwordDeviceService2 = getPasswordDeviceService(initialContext2);
        NodeMappingService nodeMappingService2 = getNodeMappingService(initialContext2);
        SubscriptionService subscriptionService2 = getSubscriptionService(initialContext2);
        IdentityService identityService2 = getIdentityService(initialContext2);
        AccountService accountService2 = getAccountService(initialContext2);

        // operate: get admin user id's
        String adminUserId1 = subjectService1.getSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN).getUserId();
        String adminUserId2 = subjectService2.getSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN).getUserId();

        // operate: register application @ both locations
        ApplicationEntity testApplication1 = registerApplication(applicationService1, pkiService1, adminUserId1);
        ApplicationEntity testApplication2 = registerApplication(applicationService2, pkiService2, adminUserId2);

        // operate: register subject @ location 1
        String login = "login-" + UUID.randomUUID().toString();
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity subject1 = userRegistrationService1.registerUser(login);
        passwordDeviceService1.register(nodeName1, subject1.getUserId(), password);

        // operate: create node mapping @ location 1
        NodeMappingEntity nodeMapping1 = nodeMappingService1.getNodeMapping(subject1.getUserId(), nodeName2);

        // operate: register password @ location2 using node mapping id from location 1 ( creates node mapping and subject @ location 2 )
        passwordDeviceService2.register(nodeName1, nodeMapping1.getId(), password);
        NodeMappingEntity nodeMapping2 = nodeMappingService2.getNodeMapping(nodeMapping1.getId());
        SubjectEntity subject2 = nodeMapping2.getSubject();

        // operate: subscribe onto the application and confirm identity usage @ location 2
        IntegrationTestUtils.login(subject2.getUserId(), password);
        subscriptionService2.subscribe(testApplication2.getId());
        identityService2.confirmIdentity(testApplication2.getId());

        // operate: fetch attribute value @ location 2 ( should proxy to location 2 )
        String resultValue = attributeClient2.getAttributeValue(subject2.getUserId(), SafeOnlineConstants.LOGIN_ATTRIBTUE, String.class);
        assertEquals(login, resultValue);
        Map<String, Object> resultAttributes = attributeClient2.getAttributeValues(subject2.getUserId());
        LOG.debug("outbound message: " + DomTestUtils.domToString(attributeClient2.getOutboundMessage()));
        LOG.info("resultAttributes: " + resultAttributes);
        assertEquals(1, resultAttributes.size());
        assertEquals(login, resultAttributes.get(SafeOnlineConstants.LOGIN_ATTRIBTUE));

        // cleanup location 1
        IntegrationTestUtils.login(adminUserId1, "admin");
        accountService1.removeAccount(subject1.getUserId());
        applicationService1.removeApplication(testApplication1.getId());

        // cleanup location 2
        IntegrationTestUtils.login(adminUserId2, "admin");
        accountService2.removeAccount(subject2.getUserId());
        applicationService2.removeApplication(testApplication2.getId());

    }

    private ApplicationEntity registerApplication(ApplicationService applicationService, PkiService pkiService, String adminUserId)
            throws Exception {

        // operate: register certificate as application trust point @ both locations
        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: register new application @ both locations
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
                certificate.getEncoded(), Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(
                        SafeOnlineConstants.LOGIN_ATTRIBTUE) }), false, false, false, null);
        testApplication = applicationService.getApplication(testApplicationName);
        return testApplication;
    }
}
