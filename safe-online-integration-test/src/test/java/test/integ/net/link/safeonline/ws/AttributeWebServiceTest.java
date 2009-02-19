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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.InitialContext;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ExistingAttributeTypeException;
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
import net.link.safeonline.sdk.ws.annotation.Compound;
import net.link.safeonline.sdk.ws.annotation.CompoundMember;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.attrib.annotation.IdentityAttribute;
import net.link.safeonline.sdk.ws.attrib.annotation.IdentityCard;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import test.integ.net.link.safeonline.IntegrationTestUtils;


/**
 * Integration tests for SafeOnline attribute web service.
 * 
 * @author fcorneli
 * 
 */
public class AttributeWebServiceTest {

    private static final Log LOG      = LogFactory.getLog(DataWebServiceTest.class);

    private X509Certificate  certificate;

    private AttributeClient  attributeClient;

    private String           nodeName = "olas-localhost";


    @Before
    public void setUp()
            throws Exception {

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        attributeClient = new AttributeClientImpl("https://localhost:8443", certificate, keyPair.getPrivate());
    }

    @Test
    public void attributeService()
            throws Exception {

        // setup
        InitialContext initialContext = IntegrationTestUtils.getInitialContext();

        IntegrationTestUtils.setupLoginConfig();

        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        IdentityService identityService = getIdentityService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);
        SubjectService subjectService = getSubjectService(initialContext);
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

        String testName = "test-name-" + UUID.randomUUID().toString();
        String testApplicationName = UUID.randomUUID().toString();

        String testAttributeName = "attr-" + UUID.randomUUID().toString();
        String testAttributeValue = "test-attribute-value";

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName, loginSubject.getUserId(), password);

        // operate: save name attribute
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        AttributeDO attribute = new AttributeDO(IntegrationTestUtils.NAME_ATTRIBUTE, DatatypeType.STRING);
        attribute.setEditable(true);
        /*
         * Marking the attribute as editable is important. Else the identity service will simply skip the saveAttribute operation.
         */
        attribute.setStringValue(testName);
        identityService.saveAttribute(attribute);

        List<AttributeDO> resultAttributeDOs = identityService.listAttributes(null);
        for (AttributeDO resultAttribute : resultAttributeDOs)
            if (IntegrationTestUtils.NAME_ATTRIBUTE.equals(resultAttribute.getName())) {
                LOG.debug("result name: " + resultAttribute.getStringValue());
                assertEquals(testName, resultAttribute.getStringValue());
            }

        // operate: register new attribute type
        IntegrationTestUtils.login(adminUserId, "admin");
        attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationService applicationService = getApplicationService(initialContext);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(),
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(IntegrationTestUtils.NAME_ATTRIBUTE),
                        new IdentityAttributeTypeDO(testAttributeName) }), false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate: retrieve name attribute via web service
        String result = attributeClient.getAttributeValue(loginSubject.getUserId(), IntegrationTestUtils.NAME_ATTRIBUTE, String.class);

        // verify
        LOG.debug("result attribute value: " + result);
        LOG.debug("application name: " + testApplicationName);
        assertEquals(testName, result);

        // operate: retrieve all accessible attributes.
        Map<String, Object> resultAttributes = attributeClient.getAttributeValues(loginSubject.getUserId());

        // verify
        assertEquals(1, resultAttributes.size());
        LOG.info("resultAttributes: " + resultAttributes);
        result = (String) resultAttributes.get(IntegrationTestUtils.NAME_ATTRIBUTE);
        assertEquals(testName, result);
        assertNull(resultAttributes.get(testAttributeName));

        // operate: set attribute
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        AttributeDO attributeDO = new AttributeDO(testAttributeName, DatatypeType.STRING);
        attributeDO.setStringValue(testAttributeValue);
        attributeDO.setEditable(true);
        identityService.saveAttribute(attributeDO);

        String resultValue = attributeClient.getAttributeValue(loginSubject.getUserId(), testAttributeName, String.class);
        assertEquals(testAttributeValue, resultValue);

        // operate: retrieve all attributes
        attributeClient.setCaptureMessages(true);
        resultAttributes = attributeClient.getAttributeValues(loginSubject.getUserId());
        LOG.debug("outbound message: " + DomTestUtils.domToString(attributeClient.getOutboundMessage()));
        LOG.info("resultAttributes: " + resultAttributes);
        assertEquals(2, resultAttributes.size());
        assertEquals(testAttributeValue, resultAttributes.get(testAttributeName));
    }

    @Test
    public void nullAttributeValue()
            throws Exception {

        // setup
        InitialContext initialContext = IntegrationTestUtils.getInitialContext();

        IntegrationTestUtils.setupLoginConfig();

        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        IdentityService identityService = getIdentityService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);
        SubjectService subjectService = getSubjectService(initialContext);
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

        String testApplicationName = UUID.randomUUID().toString();

        String testAttributeName = "attr-" + UUID.randomUUID().toString();

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName, loginSubject.getUserId(), password);

        // operate: register new attribute type
        IntegrationTestUtils.login(adminUserId, "admin");
        attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeTypeService.add(attributeType);

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationService applicationService = getApplicationService(initialContext);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(),
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(IntegrationTestUtils.NAME_ATTRIBUTE),
                        new IdentityAttributeTypeDO(testAttributeName) }), false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate: retrieve attribute via web service
        attributeClient.setCaptureMessages(true);
        String result = attributeClient.getAttributeValue(loginSubject.getUserId(), testAttributeName, String.class);

        // verify
        LOG.debug("result message: " + DomTestUtils.domToString(attributeClient.getInboundMessage()));
        LOG.debug("result attribute value: " + result);
        assertNull(result);
    }

    @Test
    public void retrievingMultivaluedAttributes()
            throws Exception {

        // setup
        InitialContext initialContext = IntegrationTestUtils.getInitialContext();

        IntegrationTestUtils.setupLoginConfig();

        UserRegistrationService userRegistrationService = getUserRegistrationService(initialContext);
        IdentityService identityService = getIdentityService(initialContext);
        PasswordDeviceService passwordDeviceService = getPasswordDeviceService(initialContext);

        String testApplicationName = UUID.randomUUID().toString();

        String testAttributeName = "attr-" + UUID.randomUUID().toString();

        // operate: register user
        String login = "login-" + UUID.randomUUID().toString();
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName, loginSubject.getUserId(), password);

        SubjectService subjectService = getSubjectService(initialContext);
        String adminUserId = subjectService.getSubjectFromUserName("admin").getUserId();

        // operate: register new multivalued attribute type
        AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");
        AttributeTypeEntity attributeType = new AttributeTypeEntity(testAttributeName, DatatypeType.STRING, true, true);
        attributeType.setMultivalued(true);
        attributeTypeService.add(attributeType);

        // operate: add multivalued attributes
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        String attributeValue1 = "value 1";
        AttributeDO attribute1 = new AttributeDO(testAttributeName, DatatypeType.STRING, true, -1, null, null, true, true, attributeValue1,
                null);
        identityService.addAttribute(Collections.singletonList(attribute1));

        String attributeValue2 = "value 2";
        AttributeDO attribute2 = new AttributeDO(testAttributeName, DatatypeType.STRING, true, -1, null, null, true, true, attributeValue2,
                null);
        identityService.addAttribute(Collections.singletonList(attribute2));

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationService applicationService = getApplicationService(initialContext);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(),
                Arrays.asList(new IdentityAttributeTypeDO[] { new IdentityAttributeTypeDO(IntegrationTestUtils.NAME_ATTRIBUTE),
                        new IdentityAttributeTypeDO(testAttributeName) }), false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate: retrieve name attribute via web service
        attributeClient.setCaptureMessages(true);
        String[] result = attributeClient.getAttributeValue(loginSubject.getUserId(), testAttributeName, String[].class);

        // verify
        Document resultDocument = attributeClient.getInboundMessage();
        LOG.debug("result message: " + DomTestUtils.domToString(resultDocument));
        LOG.debug("result: " + result);

        // verify number of attribute values returned.
        Element nsElement = resultDocument.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        XObject xObject = XPathAPI.eval(resultDocument,
                "count(/soap:Envelope/soap:Body/samlp:Response/saml:Assertion/saml:AttributeStatement/saml:Attribute/saml:AttributeValue)",
                nsElement);
        double countResult = xObject.num();
        LOG.debug("count result: " + countResult);
        assertEquals(2.0, countResult, 0);
        assertTrue(contains(attributeValue1, result));
        assertTrue(contains(attributeValue2, result));
        assertFalse(contains("foo-bar", result));
    }

    @Test
    public void retrievingCompoundedAttributes()
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
        String password = "pwd-" + UUID.randomUUID().toString();
        SubjectEntity loginSubject = userRegistrationService.registerUser(login);
        passwordDeviceService.register(nodeName, loginSubject.getUserId(), password);

        SubjectService subjectService = getSubjectService(initialContext);
        String adminUserId = subjectService.getSubjectFromUserName("admin").getUserId();

        // operate: register new multivalued attribute type
        AttributeTypeService attributeTypeService = getAttributeTypeService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");

        List<AttributeTypeEntity> existingAttributeTypes = attributeTypeService.listAttributeTypes();
        AttributeTypeEntity member0AttributeType = new AttributeTypeEntity(TEST_MEMBER_0_NAME, DatatypeType.STRING, true, true);
        if (false == existingAttributeTypes.contains(member0AttributeType)) {
            member0AttributeType.setMultivalued(true);
            attributeTypeService.add(member0AttributeType);
        }

        AttributeTypeEntity member1AttributeType = new AttributeTypeEntity(TEST_MEMBER_1_NAME, DatatypeType.STRING, true, true);
        if (false == existingAttributeTypes.contains(member1AttributeType)) {
            member1AttributeType.setMultivalued(true);
            attributeTypeService.add(member1AttributeType);
        }

        AttributeTypeEntity compoundAttributeType = new AttributeTypeEntity(TEST_COMP_NAME, DatatypeType.COMPOUNDED, true, true);
        if (false == existingAttributeTypes.contains(compoundAttributeType)) {
            compoundAttributeType.setMultivalued(true);
            compoundAttributeType.addMember(member0AttributeType, 0, true);
            compoundAttributeType.addMember(member1AttributeType, 1, true);
            attributeTypeService.add(compoundAttributeType);
        }

        // operate: add multivalued attributes
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        AttributeDO compAttribute = new AttributeDO(TEST_COMP_NAME, DatatypeType.COMPOUNDED, true, -1, null, null, true, true, null, null);
        String attributeValue1 = "value 00";
        AttributeDO attribute1 = new AttributeDO(TEST_MEMBER_0_NAME, DatatypeType.STRING, true, -1, null, null, true, true,
                attributeValue1, null);
        String attributeValue2 = "value 01";
        AttributeDO attribute2 = new AttributeDO(TEST_MEMBER_1_NAME, DatatypeType.STRING, true, -1, null, null, true, true,
                attributeValue2, null);
        List<AttributeDO> attributes = new LinkedList<AttributeDO>();
        attributes.add(compAttribute);
        attributes.add(attribute1);
        attributes.add(attribute2);
        identityService.addAttribute(attributes);

        attribute1.setStringValue("value 10");
        attribute2.setStringValue("value 11");
        identityService.addAttribute(attributes);

        // operate: register certificate as application trust point
        PkiService pkiService = getPkiService(initialContext);
        IntegrationTestUtils.login(adminUserId, "admin");
        pkiService.addTrustPoint(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate.getEncoded());

        // operate: add application with certificate
        ApplicationService applicationService = getApplicationService(initialContext);
        applicationService.addApplication(testApplicationName, null, "owner", null, false, IdScopeType.USER, null, null,
                certificate.getEncoded(), Arrays.asList(new IdentityAttributeTypeDO[] {
                        new IdentityAttributeTypeDO(IntegrationTestUtils.NAME_ATTRIBUTE), new IdentityAttributeTypeDO(TEST_COMP_NAME) }),
                false, false, false, null);
        ApplicationEntity testApplication = applicationService.getApplication(testApplicationName);

        // operate: subscribe onto the application and confirm identity usage
        SubscriptionService subscriptionService = getSubscriptionService(initialContext);
        IntegrationTestUtils.login(loginSubject.getUserId(), password);
        subscriptionService.subscribe(testApplication.getId());
        identityService.confirmIdentity(testApplication.getId());

        // operate: retrieve name attribute via web service

        attributeClient.setCaptureMessages(true);
        CompoundedTestClass[] result = attributeClient.getAttributeValue(loginSubject.getUserId(), TEST_COMP_NAME,
                CompoundedTestClass[].class);

        // verify
        Document resultDocument = attributeClient.getInboundMessage();
        LOG.debug("result message: " + DomTestUtils.domToString(resultDocument));
        CompoundedTestClass result0 = result[0];
        LOG.debug("result0: " + result0);
        assertEquals("value 00", result0.getMember0());
        assertEquals("value 01", result0.getMember1());
        CompoundedTestClass result1 = result[1];
        LOG.debug("result1: " + result1);
        assertEquals("value 10", result1.getMember0());
        assertEquals("value 11", result1.getMember1());

        // verify number of attribute values returned.
        Element nsElement = resultDocument.createElement("nsElement");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:samlp", "urn:oasis:names:tc:SAML:2.0:protocol");
        nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        XObject xObject = XPathAPI.eval(resultDocument,
                "count(/soap:Envelope/soap:Body/samlp:Response/saml:Assertion/saml:AttributeStatement/saml:Attribute/saml:AttributeValue)",
                nsElement);
        double countResult = xObject.num();
        LOG.debug("count result: " + countResult);
        assertEquals(2.0, countResult, 0);

        // operate: retrieve identity
        IdentityCardTestClass identityCard = attributeClient.getIdentity(loginSubject.getUserId(), IdentityCardTestClass.class);

        // verify
        assertNotNull(identityCard);
        assertNull(identityCard.getName());
        result0 = identityCard.getCompoundData()[0];
        LOG.debug("result0: " + result0);
        assertEquals("value 00", result0.getMember0());
        assertEquals("value 01", result0.getMember1());
        result1 = identityCard.getCompoundData()[1];
        LOG.debug("result1: " + result1);
        assertEquals("value 10", result1.getMember0());
        assertEquals("value 11", result1.getMember1());
    }


    public static final String TEST_COMP_NAME     = "test-comp-name-1234";

    public static final String TEST_MEMBER_0_NAME = "test-member-0-name-5678";

    public static final String TEST_MEMBER_1_NAME = "test-member-1-name-4321";


    @Compound(TEST_COMP_NAME)
    public static class CompoundedTestClass {

        private String member0;

        private String member1;


        public CompoundedTestClass() {

        }

        @CompoundMember(TEST_MEMBER_0_NAME)
        public String getMember0() {

            return member0;
        }

        public void setMember0(String member0) {

            this.member0 = member0;
        }

        @CompoundMember(TEST_MEMBER_1_NAME)
        public String getMember1() {

            return member1;
        }

        public void setMember1(String member1) {

            this.member1 = member1;
        }

        @Override
        public String toString() {

            return new ToStringBuilder(this).append("member0", member0).append("member1", member1).toString();
        }
    }

    @IdentityCard
    public static class IdentityCardTestClass {

        private String                name;

        private CompoundedTestClass[] compoundData;


        @IdentityAttribute(TEST_COMP_NAME)
        public CompoundedTestClass[] getCompoundData() {

            return compoundData;
        }

        public void setCompoundData(CompoundedTestClass[] compoundData) {

            this.compoundData = compoundData;
        }

        @IdentityAttribute(IntegrationTestUtils.NAME_ATTRIBUTE)
        public String getName() {

            return name;
        }

        public void setName(String name) {

            this.name = name;
        }
    }


    @Test
    public void instantiation()
            throws Exception {

        Class<?> clazz = CompoundedTestClass.class;
        clazz.newInstance();
    }

    private boolean contains(String value, Object[] items) {

        for (Object item : items)
            if (value.equals(item))
                return true;
        return false;
    }
}
