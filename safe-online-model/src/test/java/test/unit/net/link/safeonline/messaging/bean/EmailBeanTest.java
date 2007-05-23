package test.unit.net.link.safeonline.messaging.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Iterator;

import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.messaging.bean.EmailBean;
import net.link.safeonline.test.util.ConfigurationTestUtils;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import junit.framework.TestCase;

public class EmailBeanTest extends TestCase {

	private final static Log LOG = LogFactory.getLog(EmailBeanTest.class);

	private EmailBean testedInstance;

	public EmailBeanTest() throws Exception {
		this.testedInstance = new EmailBean();
		ConfigurationTestUtils.configure(this.testedInstance,
				"Mail server port", "2525");
	}

	public void testEmail() throws Exception {
		SimpleSmtpServer server = SimpleSmtpServer.start(2525);

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
