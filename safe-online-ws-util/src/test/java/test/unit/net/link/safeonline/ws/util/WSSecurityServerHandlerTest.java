/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.ws.util;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.security.cert.X509Certificate;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.TestSOAPMessageContext;
import net.link.safeonline.ws.util.WSSecurityServerHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WSSecurityServerHandlerTest {

	private static final Log LOG = LogFactory
			.getLog(WSSecurityServerHandlerTest.class);

	private WSSecurityServerHandler testedInstance;

	private JndiTestUtils jndiTestUtils;

	private ConfigurationManager mockConfigurationManager;

	private Object[] mockObjects;

	@Before
	public void setUp() throws Exception {
		this.mockConfigurationManager = createMock(ConfigurationManager.class);
		this.jndiTestUtils = new JndiTestUtils();
		this.jndiTestUtils.setUp();
		this.jndiTestUtils.bindComponent(
				"SafeOnline/ConfigurationManagerBean/local",
				this.mockConfigurationManager);
		this.testedInstance = new WSSecurityServerHandler();
		this.testedInstance.postConstructCallback();

		this.mockObjects = new Object[] { this.mockConfigurationManager };
	}

	@After
	public void tearDown() throws Exception {
		this.jndiTestUtils.tearDown();
	}

	@Test
	public void testHandleMessageAddsCertificateToContext() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
				.getResourceAsStream("/test-ws-security-message.xml");
		assertNotNull(testSoapMessageInputStream);

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// stubs
		expect(
				this.mockConfigurationManager
						.getMaximumWsSecurityTimestampOffset()).andStubReturn(
				Long.MAX_VALUE);

		// prepare
		replay(this.mockObjects);

		// operate
		this.testedInstance.handleMessage(soapMessageContext);

		// verify
		verify(this.mockObjects);
		X509Certificate resultCertificate = WSSecurityServerHandler
				.getCertificate(soapMessageContext);
		assertNotNull(resultCertificate);
		assertTrue(WSSecurityServerHandler.isSignedElement("id-21414356",
				soapMessageContext));
		assertFalse(WSSecurityServerHandler.isSignedElement("id-foobar",
				soapMessageContext));
	}

	@Test
	public void testHandleMessageInvalidSoapBody() throws Exception {
		// setup
		MessageFactory messageFactory = MessageFactory
				.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
		InputStream testSoapMessageInputStream = WSSecurityServerHandlerTest.class
				.getResourceAsStream("/test-ws-security-invalid-message.xml");
		assertNotNull(testSoapMessageInputStream);

		SOAPMessage message = messageFactory.createMessage(null,
				testSoapMessageInputStream);

		SOAPMessageContext soapMessageContext = new TestSOAPMessageContext(
				message, false);

		// prepare
		replay(this.mockObjects);

		// operate & verify
		try {
			this.testedInstance.handleMessage(soapMessageContext);
			fail();
		} catch (RuntimeException e) {
			// expected
			verify(this.mockObjects);
		}
	}

	@Test
	public void maxMillis() throws Exception {
		DateTime dateTime = new DateTime("2007-02-26T15:06:11.824Z");
		LOG.debug("date time: " + dateTime);
		Instant instant = dateTime.toInstant();
		LOG.debug("instant: " + instant);
		DateTime now = new DateTime();
		LOG.debug("now: " + now);
		Instant nowInstant = now.toInstant();
		LOG.debug("now instant: " + nowInstant);
		long diff = Math.abs(nowInstant.getMillis() - instant.getMillis());
		LOG.debug("diff: " + diff);
		LOG.debug("Max: " + Long.MAX_VALUE);
		LOG.debug("diff > MAX?: " + (diff > Long.MAX_VALUE));
		Duration duration = new Duration(instant, nowInstant);
		LOG.debug("duration: " + duration);
	}
}
