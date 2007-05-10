package test.unit.net.link.safeonline.messaging.bean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Iterator;

import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.link.safeonline.entity.ConfigItemEntity;
import net.link.safeonline.messaging.bean.EmailBean;
import net.link.safeonline.model.ConfigurationManager;
import net.link.safeonline.test.util.EJBTestUtils;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import junit.framework.TestCase;

import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailServer;
import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailServerPort;
import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailSender;
import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailSubjectPrefix;

public class EmailBeanTest extends TestCase {

	private final static Log LOG = LogFactory.getLog(EmailBeanTest.class);

	private EmailBean testedInstance;

	private ConfigurationManager configurationManager;

	public EmailBeanTest() throws Exception {
		this.testedInstance = new EmailBean();

		this.configurationManager = createMock(ConfigurationManager.class);

		EJBTestUtils.inject(this.testedInstance, this.configurationManager);
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

		expect(this.configurationManager.findConfigItem(emailServer))
				.andReturn(new ConfigItemEntity(emailServer, "127.0.0.1", null));
		expect(this.configurationManager.findConfigItem(emailServerPort))
				.andReturn(new ConfigItemEntity(emailServerPort, "2525", null));
		expect(this.configurationManager.findConfigItem(emailSender))
				.andReturn(
						new ConfigItemEntity(emailSender, "test@test.test",
								null));
		expect(this.configurationManager.findConfigItem(emailSubjectPrefix))
				.andReturn(
						new ConfigItemEntity(emailSubjectPrefix,
								"[Safe Online]", null));
		replay(this.configurationManager);

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
