/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.data.ws;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;

import liberty.dst._2006_08.ref.safe_online.AppDataType;
import liberty.dst._2006_08.ref.safe_online.CreateItemType;
import liberty.dst._2006_08.ref.safe_online.CreateResponseType;
import liberty.dst._2006_08.ref.safe_online.CreateType;
import liberty.dst._2006_08.ref.safe_online.DataService;
import liberty.dst._2006_08.ref.safe_online.DataServicePort;
import liberty.dst._2006_08.ref.safe_online.DeleteItemType;
import liberty.dst._2006_08.ref.safe_online.DeleteResponseType;
import liberty.dst._2006_08.ref.safe_online.DeleteType;
import liberty.dst._2006_08.ref.safe_online.ModifyItemType;
import liberty.dst._2006_08.ref.safe_online.ModifyResponseType;
import liberty.dst._2006_08.ref.safe_online.ModifyType;
import liberty.dst._2006_08.ref.safe_online.ObjectFactory;
import liberty.dst._2006_08.ref.safe_online.QueryItemType;
import liberty.dst._2006_08.ref.safe_online.QueryResponseType;
import liberty.dst._2006_08.ref.safe_online.QueryType;
import liberty.dst._2006_08.ref.safe_online.SelectType;
import liberty.util._2006_08.StatusType;
import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.ApplicationIdentifierMappingService;
import net.link.safeonline.authentication.service.AttributeProviderService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.data.ws.DataServiceConstants;
import net.link.safeonline.data.ws.DataServiceFactory;
import net.link.safeonline.data.ws.DataServicePortImpl;
import net.link.safeonline.data.ws.SecondLevelStatusCode;
import net.link.safeonline.data.ws.TopLevelStatusCode;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.pkix.model.PkiValidator.PkiResult;
import net.link.safeonline.sdk.ws.LoggingHandler;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.sdk.ws.data.TargetIdentityClientHandler;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.common.WebServiceConstants;
import net.link.safeonline.ws.util.ri.InjectionInstanceResolver;
import oasis.names.tc.saml._2_0.assertion.AttributeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.XPathAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class DataServicePortImplTest {

    private static final Log                    LOG = LogFactory.getLog(DataServicePortImplTest.class);

    private WebServiceTestUtils                 webServiceTestUtils;

    private JndiTestUtils                       jndiTestUtils;

    private DataServicePort                     dataServicePort;

    private WSSecurityConfigurationService      mockWSSecurityConfigurationService;

    private AttributeProviderService            mockAttributeProviderService;

    private ApplicationAuthenticationService    mockApplicationAuthenticationService;

    private DeviceAuthenticationService         mockDeviceAuthenticationService;

    private NodeAuthenticationService           mockNodeAuthenticationService;

    private PkiValidator                        mockPkiValidator;

    private ConfigurationManager                mockConfigurationManager;

    private ApplicationIdentifierMappingService mockApplicationIdentifierMappingService;

    private Object[]                            mockObjects;

    private X509Certificate                     certificate;

    private X509Certificate                     olasCertificate;

    private PrivateKey                          olasPrivateKey;

    private String                              targetIdentity;

    private long                                applicationId;

    private String                              testSubjectId;


    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
            throws Exception {

        targetIdentity = "test-target-identity-" + UUID.randomUUID().toString();
        applicationId = 1234567890;
        testSubjectId = UUID.randomUUID().toString();

        jndiTestUtils = new JndiTestUtils();
        jndiTestUtils.setUp();
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName", "SafeOnline/WSSecurityConfigurationBean/local");
        jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", false);

        mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", mockWSSecurityConfigurationService);

        mockAttributeProviderService = createMock(AttributeProviderService.class);
        jndiTestUtils.bindComponent("SafeOnline/AttributeProviderServiceBean/local", mockAttributeProviderService);

        mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        jndiTestUtils.bindComponent("SafeOnline/ApplicationAuthenticationServiceBean/local", mockApplicationAuthenticationService);

        mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        jndiTestUtils.bindComponent("SafeOnline/DeviceAuthenticationServiceBean/local", mockDeviceAuthenticationService);

        mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        jndiTestUtils.bindComponent("SafeOnline/NodeAuthenticationServiceBean/local", mockNodeAuthenticationService);

        mockPkiValidator = createMock(PkiValidator.class);
        jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local", mockPkiValidator);

        mockConfigurationManager = createMock(ConfigurationManager.class);
        jndiTestUtils.bindComponent("SafeOnline/ConfigurationManagerBean/local", mockConfigurationManager);

        expect(mockWSSecurityConfigurationService.getMaximumWsSecurityTimestampOffset()).andStubReturn(Long.MAX_VALUE);

        mockApplicationIdentifierMappingService = createMock(ApplicationIdentifierMappingService.class);
        jndiTestUtils.bindComponent("SafeOnline/ApplicationIdentifierMappingServiceBean/local", mockApplicationIdentifierMappingService);
        expect(mockApplicationIdentifierMappingService.findUserId(applicationId, targetIdentity)).andStubReturn(testSubjectId);

        mockObjects = new Object[] { mockWSSecurityConfigurationService, mockAttributeProviderService,
                mockApplicationAuthenticationService, mockDeviceAuthenticationService, mockNodeAuthenticationService, mockPkiValidator,
                mockConfigurationManager, mockApplicationIdentifierMappingService };

        webServiceTestUtils = new WebServiceTestUtils();

        DataServicePortImpl wsPort = new DataServicePortImpl();
        webServiceTestUtils.setUp(wsPort);
        InjectionInstanceResolver.clearInstanceCache();

        DataService dataService = DataServiceFactory.newInstance();
        dataServicePort = dataService.getDataServicePort();
        webServiceTestUtils.setEndpointAddress(dataServicePort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");
        olasPrivateKey = olasKeyPair.getPrivate();

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler wsSecurityHandler = new WSSecurityClientHandler(certificate, keyPair.getPrivate());
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);
    }

    @After
    public void tearDown()
            throws Exception {

        webServiceTestUtils.tearDown();
        jndiTestUtils.tearDown();
    }

    @Test
    public void queryInvalidCertificate()
            throws Exception {

        // setup
        QueryType query = new QueryType();

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.INVALID);
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_DEVICES_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.INVALID);
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_OLAS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.INVALID);

        // prepare
        replay(mockObjects);

        // operate
        try {
            dataServicePort.query(query);
            fail();
        } catch (SOAPFaultException e) {
            // expected
        }

        // verify
        verify(mockObjects);
    }

    @Test
    public void queryUnknownApplication()
            throws Exception {

        // setup
        QueryType query = new QueryType();

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andThrow(new ApplicationNotFoundException());

        // prepare
        replay(mockObjects);

        // operate
        try {
            dataServicePort.query(query);
            fail();
        } catch (SOAPFaultException e) {
            // expected
        }

        // verify
        verify(mockObjects);
    }

    @Test
    public void queryUnsupportedObjectType()
            throws Exception {

        // setup
        QueryType query = new QueryType();
        QueryItemType queryItem = new QueryItemType();
        query.getQueryItem().add(queryItem);
        queryItem.setObjectType("foo-bar-object-type");

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        QueryResponseType result = dataServicePort.query(query);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.UNSUPPORTED_OBJECT_TYPE, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @Test
    public void queryMissingSelect()
            throws Exception {

        // setup
        QueryType query = new QueryType();
        QueryItemType queryItem = new QueryItemType();
        query.getQueryItem().add(queryItem);
        queryItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        QueryResponseType result = dataServicePort.query(query);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.MISSING_SELECT, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @Test
    public void queryMissingTargetIdentity()
            throws Exception {

        // setup
        QueryType query = new QueryType();
        QueryItemType queryItem = new QueryItemType();
        query.getQueryItem().add(queryItem);
        queryItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue("select-value");
        queryItem.setSelect(select);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        QueryResponseType result = dataServicePort.query(query);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.MISSING_CREDENTIALS, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void queryTargetIdentityNotSigned()
            throws Exception {

        // setup
        QueryType query = new QueryType();
        QueryItemType queryItem = new QueryItemType();
        query.getQueryItem().add(queryItem);
        queryItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue("select-value");
        queryItem.setSelect(select);

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate & verify
        try {
            dataServicePort.query(query);
            fail();
        } catch (SOAPFaultException e) {
            // expected
        }

        // verify
        verify(mockObjects);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void queryMultivaluedAttribute()
            throws Exception {

        // setup
        QueryType query = new QueryType();
        QueryItemType queryItem = new QueryItemType();
        query.getQueryItem().add(queryItem);
        queryItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        String attributeName = "test-attribute-name";
        select.setValue(attributeName);
        queryItem.setSelect(select);

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        for (Handler handler : handlerChain) {
            LOG.debug("handler in chain: " + handler.getClass().getName());
        }

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        AttributeTypeEntity attributeType = new AttributeTypeEntity();
        attributeType.setName(attributeName);
        attributeType.setType(DatatypeType.STRING);
        attributeType.setMultivalued(true);
        SubjectEntity subject = new SubjectEntity();
        List<AttributeEntity> attributes = new LinkedList<AttributeEntity>();
        AttributeEntity attribute1 = new AttributeEntity(attributeType, subject, 0);
        String attributeValue1 = "value1";
        attribute1.setStringValue(attributeValue1);
        attributes.add(attribute1);
        AttributeEntity attribute2 = new AttributeEntity(attributeType, subject, 1);
        String attributeValue2 = "value2";
        attribute2.setStringValue(attributeValue2);
        attributes.add(attribute2);

        expect(mockAttributeProviderService.getAttributes(testSubjectId, attributeName)).andReturn(attributes);

        // prepare
        replay(mockObjects);

        // operate
        QueryResponseType response = dataServicePort.query(query);

        // verify
        verify(mockObjects);
        StatusType status = response.getStatus();
        assertEquals(TopLevelStatusCode.OK, TopLevelStatusCode.fromCode(status.getCode()));

        // marshall the result to a DOM
        JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
        ObjectFactory objectFactory = new ObjectFactory();
        Marshaller marshaller = context.createMarshaller();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        marshaller.marshal(objectFactory.createQueryResponse(response), document);
        LOG.debug("result document: " + DomTestUtils.domToString(document));

        // verify the content of the DOM result
        Element nsElement = document.createElement("nsElement");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:dstref", "urn:liberty:dst:2006-08:ref:safe-online");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:lu", "urn:liberty:util:2006-08");
        nsElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:saml", "urn:oasis:names:tc:SAML:2.0:assertion");
        Node resultNode = XPathAPI.selectSingleNode(document, "/dstref:QueryResponse/dstref:Data/saml:Attribute/saml:AttributeValue[1]",
                nsElement);
        assertNotNull(resultNode);
        assertEquals(attributeValue1, resultNode.getTextContent());
        resultNode = XPathAPI.selectSingleNode(document, "/dstref:QueryResponse/dstref:Data/saml:Attribute/saml:AttributeValue[2]",
                nsElement);
        assertNotNull(resultNode);
        assertEquals(attributeValue2, resultNode.getTextContent());

        // verify multivalued attribute on Attribute
        assertEquals(Boolean.TRUE.toString(), response.getData().get(0).getAttribute().getOtherAttributes().get(
                WebServiceConstants.MULTIVALUED_ATTRIBUTE));
    }

    @Test
    public void modifyMissingModifyItem()
            throws Exception {

        // setup
        ModifyType request = new ModifyType();

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.EMPTY_REQUEST, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @Test
    public void modifyMissingObjectType()
            throws Exception {

        // setup
        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.MISSING_OBJECT_TYPE, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @Test
    public void modifyMissingSelect()
            throws Exception {

        // setup
        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.MISSING_SELECT, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void modifyMissingNewData()
            throws Exception {

        // setup
        String attributeName = "test-attribute-name";

        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        modifyItem.setSelect(select);

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.MISSING_NEW_DATA_ELEMENT, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void modifyAttributeNameMismatch()
            throws Exception {

        // setup
        String attributeName = "test-attribute-name";

        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        modifyItem.setSelect(select);
        AppDataType newData = new AppDataType();
        modifyItem.setNewData(newData);
        AttributeType attribute = new AttributeType();
        newData.setAttribute(attribute);

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.FAILED, TopLevelStatusCode.fromCode(status.getCode()));
        assertEquals(SecondLevelStatusCode.INVALID_DATA, SecondLevelStatusCode.fromCode(status.getStatus().get(0).getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void modifySingleValuesAttribute()
            throws Exception {

        // setup
        String attributeName = "test-attribute-name";

        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        modifyItem.setSelect(select);
        AppDataType newData = new AppDataType();
        modifyItem.setNewData(newData);
        AttributeType attribute = new AttributeType();
        newData.setAttribute(attribute);
        attribute.setName(attributeName);
        String attributeValue = "test-attribute-value";
        attribute.getAttributeValue().add(attributeValue);

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        mockAttributeProviderService.setAttribute(testSubjectId, attributeName, attributeValue);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.OK, TopLevelStatusCode.fromCode(status.getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void modifyNullAttributeValue()
            throws Exception {

        // setup
        String attributeName = "test-attribute-name";

        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        modifyItem.setSelect(select);
        AppDataType newData = new AppDataType();
        modifyItem.setNewData(newData);
        AttributeType attribute = new AttributeType();
        newData.setAttribute(attribute);
        attribute.setName(attributeName);
        String attributeValue = null;

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        mockAttributeProviderService.setAttribute(testSubjectId, attributeName, attributeValue);

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.OK, TopLevelStatusCode.fromCode(status.getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void modifyMultivaluedAttribute()
            throws Exception {

        // setup
        String attributeName = "test-attribute-name";

        ModifyType request = new ModifyType();
        ModifyItemType modifyItem = new ModifyItemType();
        request.getModifyItem().add(modifyItem);
        modifyItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        modifyItem.setSelect(select);
        AppDataType newData = new AppDataType();
        modifyItem.setNewData(newData);
        AttributeType attribute = new AttributeType();
        newData.setAttribute(attribute);
        attribute.setName(attributeName);
        String attributeValue1 = "test-attribute-value-1";
        String attributeValue2 = "test-atribute-value-2";
        List<Object> attributeValues = attribute.getAttributeValue();
        attributeValues.add(attributeValue1);
        attributeValues.add(attributeValue2);
        attribute.getOtherAttributes().put(WebServiceConstants.MULTIVALUED_ATTRIBUTE, Boolean.TRUE.toString());

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        mockAttributeProviderService.setAttribute(eq(testSubjectId), eq(attributeName), aryEq(new String[] { attributeValue1,
                attributeValue2 }));

        // prepare
        replay(mockObjects);

        // operate
        ModifyResponseType result = dataServicePort.modify(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.OK, TopLevelStatusCode.fromCode(status.getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void createMultivaluedAttribute()
            throws Exception {

        // setup
        String attributeName = "test-attribute-name";

        CreateType request = new CreateType();
        CreateItemType createItem = new CreateItemType();
        request.getCreateItem().add(createItem);
        createItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        AppDataType newData = new AppDataType();
        createItem.setNewData(newData);
        AttributeType attribute = new AttributeType();
        newData.setAttribute(attribute);
        attribute.setName(attributeName);
        String attributeValue1 = "test-attribute-value-1";
        String attributeValue2 = "test-atribute-value-2";
        List<Object> attributeValues = attribute.getAttributeValue();
        attributeValues.add(attributeValue1);
        attributeValues.add(attributeValue2);
        attribute.getOtherAttributes().put(WebServiceConstants.MULTIVALUED_ATTRIBUTE, Boolean.TRUE.toString());

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);
        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        mockAttributeProviderService.createAttribute(eq(testSubjectId), eq(attributeName), aryEq(new String[] { attributeValue1,
                attributeValue2 }));

        // prepare
        replay(mockObjects);

        // operate
        CreateResponseType result = dataServicePort.create(request);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.OK, TopLevelStatusCode.fromCode(status.getCode()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void delete()
            throws Exception {

        // setup
        DeleteType delete = new DeleteType();
        String attributeName = "test-attribute-name";
        DeleteItemType deleteItem = new DeleteItemType();
        delete.getDeleteItem().add(deleteItem);
        deleteItem.setObjectType(DataServiceConstants.ATTRIBUTE_OBJECT_TYPE);
        SelectType select = new SelectType();
        select.setValue(attributeName);
        deleteItem.setSelect(select);

        BindingProvider bindingProvider = (BindingProvider) dataServicePort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        TargetIdentityClientHandler targetIdentityClientHandler = new TargetIdentityClientHandler();
        targetIdentityClientHandler.setTargetIdentity(targetIdentity);
        handlerChain.add(0, targetIdentityClientHandler);
        LoggingHandler loggingHandler = new LoggingHandler();
        handlerChain.add(loggingHandler);
        binding.setHandlerChain(handlerChain);

        // expectations
        expect(mockPkiValidator.validateCertificate(SafeOnlineConstants.SAFE_ONLINE_APPLICATIONS_TRUST_DOMAIN, certificate)).andReturn(
                PkiResult.VALID);

        expect(mockApplicationAuthenticationService.authenticate(certificate)).andReturn(applicationId);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.skipMessageIntegrityCheck(certificate)).andReturn(false);
        expect(mockWSSecurityConfigurationService.getCertificate()).andStubReturn(olasCertificate);
        expect(mockWSSecurityConfigurationService.getPrivateKey()).andStubReturn(olasPrivateKey);

        mockAttributeProviderService.removeAttribute(testSubjectId, attributeName);

        // prepare
        replay(mockObjects);

        // operate
        DeleteResponseType result = dataServicePort.delete(delete);

        // verify
        verify(mockObjects);
        StatusType status = result.getStatus();
        assertEquals(TopLevelStatusCode.OK, TopLevelStatusCode.fromCode(status.getCode()));
    }
}
