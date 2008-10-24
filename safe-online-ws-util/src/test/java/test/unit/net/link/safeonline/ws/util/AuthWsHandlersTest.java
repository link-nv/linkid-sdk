/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.util;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.authentication.service.ApplicationAuthenticationService;
import net.link.safeonline.authentication.service.DeviceAuthenticationService;
import net.link.safeonline.authentication.service.NodeAuthenticationService;
import net.link.safeonline.model.WSSecurityConfiguration;
import net.link.safeonline.pkix.model.PkiValidator;
import net.link.safeonline.sdk.ws.WSSecurityConfigurationService;
import net.link.safeonline.test.util.DomTestUtils;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.utils.Constants;
import org.apache.xpath.XPathAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class AuthWsHandlersTest {

    private static final Log                 LOG = LogFactory.getLog(AuthWsHandlersTest.class);

    private WebServiceTestUtils              webServiceTestUtils;

    private JndiTestUtils                    jndiTestUtils;

    private WSSecurityConfigurationService   mockWSSecurityConfigurationService;

    private ApplicationAuthenticationService mockApplicationAuthenticationService;

    private DeviceAuthenticationService      mockDeviceAuthenticationService;

    private NodeAuthenticationService        mockNodeAuthenticationService;

    private PkiValidator                     mockPkiValidator;


    @Before
    public void setUp() throws Exception {

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.jndiTestUtils.bindComponent("java:comp/env/wsSecurityConfigurationServiceJndiName",
                "SafeOnline/WSSecurityConfigurationBean/local");

        this.mockWSSecurityConfigurationService = createMock(WSSecurityConfiguration.class);
        this.jndiTestUtils.bindComponent("SafeOnline/WSSecurityConfigurationBean/local", this.mockWSSecurityConfigurationService);

        this.mockApplicationAuthenticationService = createMock(ApplicationAuthenticationService.class);
        this.jndiTestUtils
                          .bindComponent("SafeOnline/ApplicationAuthenticationServiceBean/local", this.mockApplicationAuthenticationService);

        this.mockDeviceAuthenticationService = createMock(DeviceAuthenticationService.class);
        this.jndiTestUtils.bindComponent("SafeOnline/DeviceAuthenticationServiceBean/local", this.mockDeviceAuthenticationService);

        this.mockNodeAuthenticationService = createMock(NodeAuthenticationService.class);
        this.jndiTestUtils.bindComponent("SafeOnline/NodeAuthenticationServiceBean/local", this.mockNodeAuthenticationService);

        this.mockPkiValidator = createMock(PkiValidator.class);
        this.jndiTestUtils.bindComponent("SafeOnline/PkiValidatorBean/local", this.mockPkiValidator);

        this.webServiceTestUtils = new WebServiceTestUtils();
        TestEndpoint testEndpoint = new TestEndpoint();
        this.webServiceTestUtils.setUp(testEndpoint);
    }

    @After
    public void tearDown() throws Exception {

        this.webServiceTestUtils.tearDown();

        this.jndiTestUtils.tearDown();
    }


    @WebService(name = "Test", targetNamespace = "urn:test", serviceName = "TestService", endpointInterface = "test.unit.net.link.safeonline.ws.util.AuthWsHandlersTest$TestEndpointInterface")
    @HandlerChain(file = "auth-ws-handlers.xml")
    public static class TestEndpoint implements TestEndpointInterface {

        public String echo(String param) {

            return param;
        }
    }

    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebService
    public static interface TestEndpointInterface {

        String echo(String param);
    }

    public static class TestEndpointService extends Service {

        public TestEndpointService(URL wsdlDocumentLocation, QName serviceName) {

            super(wsdlDocumentLocation, serviceName);
        }

        public TestEndpointInterface getPort() {

            return super.getPort(new QName("urn:test", "TestPort"), TestEndpointInterface.class);
        }
    }

    public static class TestHandler implements SOAPHandler<SOAPMessageContext> {

        private Document faultDocument;


        public Set<QName> getHeaders() {

            return null;
        }

        public void close(MessageContext context) {

        }

        public Document getFaultDocument() {

            return this.faultDocument;
        }

        public boolean handleFault(SOAPMessageContext soapContext) {

            Boolean outboundProperty = (Boolean) soapContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            if (true == outboundProperty)
                return true;

            SOAPMessage soapMessage = soapContext.getMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            this.faultDocument = soapPart;

            return true;
        }

        public boolean handleMessage(SOAPMessageContext soapContext) {

            return true;
        }
    }


    @Test
    public void testMissingWsSecuritySignature() throws Exception {

        // setup
        String location = this.webServiceTestUtils.getEndpointAddress();

        TestEndpointService service = new TestEndpointService(new URL(location + "?wsdl"), new QName("urn:test", "TestService"));
        TestEndpointInterface port = service.getPort();
        BindingProvider bindingProvider = (BindingProvider) port;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);
        Binding binding = bindingProvider.getBinding();
        @SuppressWarnings("unchecked")
        List<Handler> handlerChain = binding.getHandlerChain();
        TestHandler testHandler = new TestHandler();
        handlerChain.add(testHandler);
        binding.setHandlerChain(handlerChain);
        String value = "hello world";

        // operate
        try {
            port.echo(value);
            fail();
        } catch (Exception e) {
            Document faultDocument = testHandler.getFaultDocument();
            LOG.debug("fault document: " + DomTestUtils.domToString(faultDocument));
            Element nsElement = faultDocument.createElement("nsElement");
            nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:soap", "http://schemas.xmlsoap.org/soap/envelope/");
            nsElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:jaxws", "http://jax-ws.dev.java.net/");
            Node exceptionNode = XPathAPI.selectSingleNode(faultDocument, "soap:Envelope/soap:Body/soap:Fault/detail/jaxws:exception",
                    nsElement);
            assertNull(exceptionNode);
            Node faultCodeNode = XPathAPI.selectSingleNode(faultDocument, "soap:Envelope/soap:Body/soap:Fault/faultcode", nsElement);
            assertNotNull(faultCodeNode);
            assertEquals("wsse:InvalidSecurity", faultCodeNode.getTextContent());
        }
    }
}
