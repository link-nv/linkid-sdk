/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.integ.net.link.safeonline.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAccountService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getApplicationService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getAttributeTypeService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getPasswordDeviceService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getSubjectService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUsageAgreementService;
import static test.integ.net.link.safeonline.IntegrationTestUtils.getUserRegistrationService;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.naming.InitialContext;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.auth.ws.AuthenticationStep;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.authentication.service.AccountService;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.IdentityAttributeTypeDO;
import net.link.safeonline.authentication.service.UsageAgreementService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.IdScopeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.password.PasswordConstants;
import net.link.safeonline.model.password.PasswordDeviceService;
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.sdk.ws.auth.AuthenticationClient;
import net.link.safeonline.sdk.ws.auth.AuthenticationClientImpl;
import net.link.safeonline.sdk.ws.auth.DataType;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClient;
import net.link.safeonline.sdk.ws.auth.GetAuthenticationClientImpl;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;
import oasis.names.tc.saml._2_0.assertion.AssertionType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import test.integ.net.link.safeonline.IntegrationTestUtils;


/**
 * Integration tests for SafeOnline authentication web service.
 * 
 * @author wvdhaute
 * 
 */
public class AuthenticationWebServiceTest {

    private static final Log        LOG        = LogFactory.getLog(AuthenticationWebServiceTest.class);

    private static final String     wsLocation = "https://localhost:8443/safe-online-auth-ws";

    private GetAuthenticationClient getAuthenticationClient;

    private AuthenticationClient    authenticationClient;


    @Before
    public void setUp()
            throws Exception {

        this.getAuthenticationClient = new GetAuthenticationClientImpl(wsLocation);

    }

    @Test
    public void authenticatePasswordSuccess()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();

        // operate: get instance of stateful authentication web service
        W3CEndpointReference endpoint = this.getAuthenticationClient.getInstance();
        this.authenticationClient = new AuthenticationClientImpl(endpoint);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, "admin");
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, "admin");

        // operate: authenticate admin user to olas-user application via web service
        String userId = this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
                PasswordConstants.PASSWORD_DEVICE_ID, Locale.ENGLISH.getLanguage(), nameValuePairs, keyPair.getPublic());
        assertNotNull(userId);
        AssertionType assertion = this.authenticationClient.getAssertion();
        assertNotNull(assertion);

        // operate: try authenticate on same instance again, should fail as previous authentication was successful and instance is removed.
        try {
            this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
                    PasswordConstants.PASSWORD_DEVICE_ID, Locale.ENGLISH.getLanguage(), null, keyPair.getPublic());
        } catch (Exception e) {
            // success
            return;
        }
        fail();
    }

    @Test
    public void authenticatePasswordWrongPassword()
            throws Exception {

        // setup
        KeyPair keyPair = PkiTestUtils.generateKeyPair();

        // operate: get instance of stateful authentication web service
        W3CEndpointReference endpoint = this.getAuthenticationClient.getInstance();
        this.authenticationClient = new AuthenticationClientImpl(endpoint);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, "admin");
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, "foo");

        // operate: authenticate with wrong password
        try {
            this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
                    PasswordConstants.PASSWORD_DEVICE_ID, Locale.ENGLISH.getLanguage(), nameValuePairs, keyPair.getPublic());
        } catch (WSAuthenticationException e) {
            assertEquals(WSAuthenticationErrorCode.AUTHENTICATION_FAILED, e.getErrorCode());
            // operate: try authenticate on same instance again, should fail, instance should be removed.
            try {
                this.authenticationClient.authenticate(SafeOnlineConstants.SAFE_ONLINE_USER_APPLICATION_NAME,
                        PasswordConstants.PASSWORD_DEVICE_ID, Locale.ENGLISH.getLanguage(), null, keyPair.getPublic());
            } catch (SOAPFaultException e2) {
                LOG.debug("soap fault: " + e2.getMessage());
                // expected
                return;
            }
            fail();
        }
        fail();
    }

    @Test
    public void authenticatePasswordAdditionalSteps()
            throws Exception {

        InitialContext initialContext = IntegrationTestUtils.getInitialContext();
        IntegrationTestUtils.setupLoginConfig();
        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);
        SubjectService subjectService = getSubjectService(initialContext);
        UsageAgreementService usageAgreementService = getUsageAgreementService(initialContext);
        ApplicationService applicationService = getApplicationService(initialContext);
        AccountService accountService = getAccountService(initialContext);
        AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);

        // setup
        String testGlobalUsageAgreement = "Test Global Usage Agreement Text";
        String testApplicationName = "test-application-" + UUID.randomUUID().toString();
        String testSingleStringAttributeName = "test-single-string-attribute-" + UUID.randomUUID().toString();
        String testMultiStringAttributeName = "test-multi-string-attribute-" + UUID.randomUUID().toString();
        String testMultiDateAttributeName = "test-multi-date-attribute-" + UUID.randomUUID().toString();
        String testCompoundAttributeName = "test-compound-attribute-" + UUID.randomUUID().toString();

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        X509Certificate certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test" + UUID.randomUUID().toString());

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(loginSubject.getUserId(), password);

        // login as admin
        String adminUserId = subjectService.getSubjectFromUserName(SafeOnlineConstants.ADMIN_LOGIN).getUserId();
        IntegrationTestUtils.login(adminUserId, "admin");

        // operate: add global usage agreement
        usageAgreementService.createDraftGlobalUsageAgreement();
        usageAgreementService.createDraftGlobalUsageAgreementText(Locale.ENGLISH.getLanguage(), testGlobalUsageAgreement);
        usageAgreementService.setDraftGlobalUsageAgreementText(Locale.ENGLISH.getLanguage(), testGlobalUsageAgreement);
        usageAgreementService.updateGlobalUsageAgreement();

        // operate: add attributes
        AttributeTypeEntity testSingleStringAttributeType = new AttributeTypeEntity(testSingleStringAttributeName, DatatypeType.STRING,
                true, true);
        testSingleStringAttributeType.setMultivalued(false);
        attributeTypeService.add(testSingleStringAttributeType);

        AttributeTypeEntity testMultiStringAttributeType = new AttributeTypeEntity(testMultiStringAttributeName, DatatypeType.STRING, true,
                true);
        testMultiStringAttributeType.setMultivalued(true);
        attributeTypeService.add(testMultiStringAttributeType);

        AttributeTypeEntity testMultiDateAttributeType = new AttributeTypeEntity(testMultiDateAttributeName, DatatypeType.DATE, true, true);
        testMultiDateAttributeType.setMultivalued(true);
        attributeTypeService.add(testMultiDateAttributeType);

        AttributeTypeEntity testCompoundAttributeType = new AttributeTypeEntity(testCompoundAttributeName, DatatypeType.COMPOUNDED, true,
                true);
        testCompoundAttributeType.setMultivalued(true);
        testCompoundAttributeType.addMember(testMultiStringAttributeType, 0, true);
        testCompoundAttributeType.addMember(testMultiDateAttributeType, 1, true);
        attributeTypeService.add(testCompoundAttributeType);

        // operate: add application with certificate
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(), Arrays
                                                .asList(new IdentityAttributeTypeDO[] {
                                                        new IdentityAttributeTypeDO(testSingleStringAttributeName),
                                                        new IdentityAttributeTypeDO(testCompoundAttributeName) }), false, false, false,
                null);

        // operate: get instance of stateful authentication web service
        W3CEndpointReference endpoint = this.getAuthenticationClient.getInstance();
        this.authenticationClient = new AuthenticationClientImpl(endpoint);

        Map<String, String> nameValuePairs = new HashMap<String, String>();
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_LOGIN_ATTRIBUTE, "admin");
        nameValuePairs.put(PasswordConstants.PASSWORD_WS_AUTH_PASSWORD_ATTRIBUTE, "admin");

        // operate: authenticate admin user to olas-user application via web service
        String userId = this.authenticationClient.authenticate(testApplicationName, PasswordConstants.PASSWORD_DEVICE_ID,
                Locale.ENGLISH.getLanguage(), nameValuePairs, keyPair.getPublic());
        assertNull(userId);
        List<AuthenticationStep> authenticationSteps = this.authenticationClient.getAuthenticationSteps();
        assertNotNull(authenticationSteps);
        assertTrue(authenticationSteps.contains(AuthenticationStep.GLOBAL_USAGE_AGREEMENT));

        // operate: request global usage agreement
        String globalUsageAgreement = this.authenticationClient.getGlobalUsageAgreement();
        assertEquals(testGlobalUsageAgreement, globalUsageAgreement);

        // operate: confirm global usage agreement
        userId = this.authenticationClient.confirmGlobalUsageAgreement(Confirmation.CONFIRM);
        assertNull(userId);
        authenticationSteps = this.authenticationClient.getAuthenticationSteps();
        assertNotNull(authenticationSteps);
        assertTrue(authenticationSteps.contains(AuthenticationStep.USAGE_AGREEMENT));

        // operate: request application usage agreement
        String usageAgreement = this.authenticationClient.getUsageAgreement();
        assertEquals("", usageAgreement);

        // operate: confirm usage agreement
        userId = this.authenticationClient.confirmUsageAgreement(Confirmation.CONFIRM);
        assertNull(userId);
        authenticationSteps = this.authenticationClient.getAuthenticationSteps();
        assertNotNull(authenticationSteps);
        assertTrue(authenticationSteps.contains(AuthenticationStep.IDENTITY_CONFIRMATION));

        // operate: request application identity
        List<Attribute> identity = this.authenticationClient.getIdentity();
        assertNotNull(identity);
        assertEquals(2, identity.size());
        for (Attribute attribute : identity) {
            assertFalse(attribute.isOptional());
            if (attribute.getDataType() == DataType.COMPOUNDED) {
                assertEquals(testCompoundAttributeName, attribute.getName());
                assertEquals(2, attribute.getMembers().size());
                for (Attribute memberAttribute : attribute.getMembers()) {
                    assertFalse(memberAttribute.isOptional());
                    if (memberAttribute.getDataType() == DataType.DATE) {
                        assertEquals(testMultiDateAttributeName, memberAttribute.getName());
                    } else if (memberAttribute.getDataType() == DataType.STRING) {
                        assertEquals(testMultiStringAttributeName, memberAttribute.getName());
                    } else {
                        fail();
                    }
                }
            } else {
                assertEquals(testSingleStringAttributeName, attribute.getName());
                assertEquals(DataType.STRING, attribute.getDataType());
            }
        }

        // operate: confirm application identity
        userId = this.authenticationClient.confirmIdentity(Confirmation.CONFIRM);
        assertNull(userId);
        authenticationSteps = this.authenticationClient.getAuthenticationSteps();
        assertNotNull(authenticationSteps);
        assertTrue(authenticationSteps.contains(AuthenticationStep.MISSING_ATTRIBUTES));

        // operate: request missing attributes
        List<Attribute> missingAttributes = this.authenticationClient.getMissingAttributes();
        assertNotNull(missingAttributes);
        assertEquals(2, missingAttributes.size());

        // fill in missing attributes
        for (Attribute missingAttribute : missingAttributes) {
            if (missingAttribute.getName().equals(testSingleStringAttributeName)) {
                missingAttribute.setValue("test-value");
            } else {
                for (Attribute memberAttribute : missingAttribute.getMembers()) {
                    if (memberAttribute.getName().equals(testMultiDateAttributeName)) {
                        memberAttribute.setValue(new Date());
                    } else {
                        memberAttribute.setValue("test-value");
                    }
                }
            }
        }

        // operate: save missing attributes
        userId = this.authenticationClient.saveMissingAttributes(missingAttributes);
        assertNotNull(userId);
        AssertionType assertion = this.authenticationClient.getAssertion();
        assertNotNull(assertion);

        // operate: login again, no further steps should be needed
        endpoint = this.getAuthenticationClient.getInstance();
        this.authenticationClient = new AuthenticationClientImpl(endpoint);
        userId = this.authenticationClient.authenticate(testApplicationName, PasswordConstants.PASSWORD_DEVICE_ID,
                Locale.ENGLISH.getLanguage(), nameValuePairs, keyPair.getPublic());
        assertNotNull(userId);
        assertion = this.authenticationClient.getAssertion();
        assertNotNull(assertion);

        // cleanup
        // login as admin
        IntegrationTestUtils.login(adminUserId, "admin");

        // set global usage agreement to empty
        usageAgreementService.createDraftGlobalUsageAgreement();
        usageAgreementService.setDraftGlobalUsageAgreementText(Locale.ENGLISH.getLanguage(), "");
        usageAgreementService.updateGlobalUsageAgreement();

        // remove user
        accountService.removeAccount(loginSubject.getUserId());

        // remove application
        applicationService.removeApplication(testApplicationName);

        // remove attributes
        attributeTypeService.remove(testSingleStringAttributeType);
        attributeTypeService.remove(testCompoundAttributeType);

    }
}
