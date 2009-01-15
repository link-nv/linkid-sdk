/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.auth.ws;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.lin_k.safe_online.auth.AuthenticationPort;
import net.lin_k.safe_online.auth.AuthenticationService;
import net.lin_k.safe_online.auth.DeviceCredentialsType;
import net.lin_k.safe_online.auth.NameValuePairType;
import net.lin_k.safe_online.auth.WSAuthenticationRequestType;
import net.lin_k.safe_online.auth.WSAuthenticationResponseType;
import net.link.safeonline.auth.ws.AuthenticationPortImpl;
import net.link.safeonline.auth.ws.AuthenticationServiceFactory;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityClientHandler;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.test.util.DummyLoginModule;
import net.link.safeonline.test.util.JaasTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.PkiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;
import net.link.safeonline.ws.util.ri.InjectionInstanceResolver;
import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.protocol.RequestAbstractType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.w3._2000._09.xmldsig_.DSAKeyValueType;
import org.w3._2000._09.xmldsig_.KeyInfoType;
import org.w3._2000._09.xmldsig_.RSAKeyValueType;

import com.sun.xml.ws.developer.StatefulWebServiceManager;


public class AuthenticationPortImplTest {

    private static final Log                              LOG               = LogFactory.getLog(AuthenticationPortImplTest.class);

    private WebServiceTestUtils                           webServiceTestUtils;

    private AuthenticationPort                            clientPort;

    private JndiTestUtils                                 jndiTestUtils;

    private WSSecurityConfigurationService                mockWSSecurityConfigurationService;

    private PkiValidator                                  mockPkiValidator;

    private SamlAuthorityService                          mockSamlAuthorityService;

    private DevicePolicyService                           mockDevicePolicyService;

    private StatefulWebServiceManager<AuthenticationPort> mockStatefulWebServiceManager;

    private Object[]                                      mockObjects;

    private X509Certificate                               certificate;

    private PublicKey                                     publicKey;

    private X509Certificate                               olasCertificate;

    private PrivateKey                                    olasPrivateKey;

    private String                                        testSubjectLogin;

    private String                                        testSubjectId;

    private String                                        testApplicationId = "test-application-name";

    private String                                        testDeviceName    = "test-device-name";


    class TimeoutCallback implements StatefulWebServiceManager.Callback<AuthenticationPort> {

        /**
         * {@inheritDoc}
         */
        public void onTimeout(AuthenticationPort timedOutObject, StatefulWebServiceManager<AuthenticationPort> serviceManager) {

            // XXX: notify stateful device ws of timeout ?
        }

    }


    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
            throws Exception {

        LOG.debug("setup");

        this.testSubjectLogin = "test-subject-login-" + UUID.randomUUID().toString();
        this.testSubjectId = UUID.randomUUID().toString();

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityOptionalInboudSignature", true);

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfigurationService.class);
        this.mockPkiValidator = createMock(PkiValidator.class);
        this.mockSamlAuthorityService = createMock(SamlAuthorityService.class);
        this.mockDevicePolicyService = createMock(DevicePolicyService.class);
        this.mockStatefulWebServiceManager = createMock(StatefulWebServiceManager.class);

        this.mockObjects = new Object[] { this.mockWSSecurityConfigurationService, this.mockPkiValidator, this.mockSamlAuthorityService,
                this.mockDevicePolicyService, this.mockStatefulWebServiceManager };

        this.jndiTestUtils.bindComponent(WSSecurityConfiguration.JNDI_BINDING, this.mockWSSecurityConfigurationService);
        this.jndiTestUtils.bindComponent(PkiValidator.JNDI_BINDING, this.mockPkiValidator);
        this.jndiTestUtils.bindComponent(SamlAuthorityService.JNDI_BINDING, this.mockSamlAuthorityService);
        this.jndiTestUtils.bindComponent(DevicePolicyService.JNDI_BINDING, this.mockDevicePolicyService);

        JaasTestUtils.initJaasLoginModule(DummyLoginModule.class);

        // set StatefulWebServiceManager
        AuthenticationPortImpl.manager = this.mockStatefulWebServiceManager;
        this.mockStatefulWebServiceManager.setTimeout(1000 * 60 * 30, new TimeoutCallback());

        AuthenticationPort wsPort = new AuthenticationPortImpl();
        this.webServiceTestUtils = new WebServiceTestUtils();
        this.webServiceTestUtils.setUp(wsPort);

        /*
         * Next is required, else the wsPort will get old mocks injected when running multiple tests.
         */
        InjectionInstanceResolver.clearInstanceCache();
        AuthenticationService service = AuthenticationServiceFactory.newInstance();
        this.clientPort = service.getAuthenticationPort();
        this.webServiceTestUtils.setEndpointAddress(this.clientPort);

        KeyPair keyPair = PkiTestUtils.generateKeyPair();
        this.certificate = PkiTestUtils.generateSelfSignedCertificate(keyPair, "CN=Test");
        this.publicKey = keyPair.getPublic();

        KeyPair olasKeyPair = PkiTestUtils.generateKeyPair();
        this.olasCertificate = PkiTestUtils.generateSelfSignedCertificate(olasKeyPair, "CN=OLAS");
        this.olasPrivateKey = olasKeyPair.getPrivate();

        BindingProvider bindingProvider = (BindingProvider) this.clientPort;
        Binding binding = bindingProvider.getBinding();
        List<Handler> handlerChain = binding.getHandlerChain();
        Handler<SOAPMessageContext> wsSecurityHandler = new WSSecurityClientHandler(null, null);
        handlerChain.add(wsSecurityHandler);
        binding.setHandlerChain(handlerChain);
    }

    @After
    public void tearDown()
            throws Exception {

        LOG.debug("tearDown");
        this.webServiceTestUtils.tearDown();
        this.jndiTestUtils.tearDown();
    }

    @Test
    public void testAuthenticate()
            throws Exception {

        // setup
        String testIssuerName = "test-issuer-name";

        WSAuthenticationRequestType request = new WSAuthenticationRequestType();
        setRequest(request);

        // Issuer
        NameIDType issuerName = new NameIDType();
        issuerName.setValue(this.testApplicationId);
        request.setIssuer(issuerName);

        request.setApplicationId(this.testApplicationId);
        request.setDeviceName(this.testDeviceName);
        request.setLanguage(Locale.ENGLISH.getLanguage());

        DeviceCredentialsType deviceCredentialsType = new DeviceCredentialsType();
        NameValuePairType nameValuePair = new NameValuePairType();
        nameValuePair.setName("foo");
        nameValuePair.setValue("bar");
        deviceCredentialsType.getNameValuePair().add(nameValuePair);
        request.setDeviceCredentials(deviceCredentialsType);

        request.setKeyInfo(getKeyInfo());

        // expectations
        expect(this.mockDevicePolicyService.getAuthenticationWSURL(this.testDeviceName)).andStubReturn("foo");
        expect(this.mockSamlAuthorityService.getIssuerName()).andStubReturn(testIssuerName);

        // prepare
        replay(this.mockObjects);

        // operate
        WSAuthenticationResponseType response = this.clientPort.authenticate(request);

        // verify
        verify(this.mockObjects);
        assertNotNull(response);

        JAXBContext context = JAXBContext.newInstance(net.lin_k.safe_online.auth.ObjectFactory.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        net.lin_k.safe_online.auth.ObjectFactory objectFactory = new net.lin_k.safe_online.auth.ObjectFactory();
        marshaller.marshal(objectFactory.createWSAuthenticationResponse(response), stringWriter);
        LOG.debug("response: " + stringWriter);

    }

    private void setRequest(RequestAbstractType request) {

        SecureRandomIdentifierGenerator idGenerator;
        try {
            idGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("secure random init error: " + e.getMessage(), e);
        }
        String id = idGenerator.generateIdentifier();
        XMLGregorianCalendar now = getCurrentXmlGregorianCalendar();

        request.setID(id);
        request.setVersion(SAMLVersion.VERSION_20.toString());
        request.setIssueInstant(now);
    }

    private XMLGregorianCalendar getCurrentXmlGregorianCalendar() {

        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            LOG.error("datatype configuration exception", e);
            throw new RuntimeException("datatype configuration exception: " + e.getMessage());
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        Date now = new Date();
        gregorianCalendar.setTime(now);
        XMLGregorianCalendar currentXmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        return currentXmlGregorianCalendar;
    }

    /**
     * Converts public key to XML DSig KeyInfoType
     * 
     */
    private KeyInfoType getKeyInfo() {

        KeyInfoType keyInfo = new KeyInfoType();
        org.w3._2000._09.xmldsig_.ObjectFactory dsigObjectFactory = new org.w3._2000._09.xmldsig_.ObjectFactory();

        if (this.publicKey instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) this.publicKey;
            RSAKeyValueType rsaKeyValue = new RSAKeyValueType();
            rsaKeyValue.setModulus(rsaPublicKey.getModulus().toByteArray());
            rsaKeyValue.setExponent(rsaPublicKey.getPublicExponent().toByteArray());
            keyInfo.getContent().add(dsigObjectFactory.createRSAKeyValue(rsaKeyValue));
        } else if (this.publicKey instanceof DSAPublicKey) {
            DSAPublicKey dsaPublicKey = (DSAPublicKey) this.publicKey;
            DSAKeyValueType dsaKeyValue = new DSAKeyValueType();
            dsaKeyValue.setY(dsaPublicKey.getY().toByteArray());
            dsaKeyValue.setG(dsaPublicKey.getParams().getG().toByteArray());
            dsaKeyValue.setP(dsaPublicKey.getParams().getP().toByteArray());
            dsaKeyValue.setQ(dsaPublicKey.getParams().getQ().toByteArray());
            keyInfo.getContent().add(dsaKeyValue);
        } else
            throw new IllegalArgumentException("Only RSAPublicKey and DSAPublicKey are supported");

        return keyInfo;
    }
}
