/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.messaging.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import javax.jms.Message;

import net.link.safeonline.messaging.bean.EmailBean;
import net.link.safeonline.test.util.ConfigurationTestUtils;
import net.link.safeonline.test.util.WebServiceTestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class EmailBeanTest {

	private final static Log LOG = LogFactory.getLog(EmailBeanTest.class);

	private EmailBean testedInstance;

	@Before
	public void setUp() throws Exception {
		this.testedInstance = new EmailBean();

	}

	@Test
	public void testEmail() throws Exception {
		LOG.debug("starting test SMTP server...");
		int freePort = WebServiceTestUtils.getFreePort();
		LOG.debug("using free port: " + freePort);
		SimpleSmtpServer server = SimpleSmtpServer.start(freePort);
		LOG.debug("test SMTP server running");

		ConfigurationTestUtils.configure(this.testedInstance,
				"Mail server port", Integer.toString(freePort));

		Message message = createMock(Message.class);
		expect(message.getStringProperty("destination")).andReturn(
				"test@test.test");
		expect(message.getStringProperty("subject")).andReturn("testsubject");
		expect(message.getStringProperty("messagetext")).andReturn(
				"testmessage");
		replay(message);

		// operate
		this.testedInstance.onMessage(message);

		server.stop();

		// verify
		verify(message);
		assertTrue(server.getReceivedEmailSize() == 1);
		Iterator emailIter = server.getReceivedEmail();
		SmtpMessage email = (SmtpMessage) emailIter.next();
		assertTrue(email.getHeaderValue("Subject").equals(
				"[Safe Online] testsubject"));
		LOG.debug(email.getBody());
		assertTrue(email.getBody().contains("testmessage"));
	}

}
