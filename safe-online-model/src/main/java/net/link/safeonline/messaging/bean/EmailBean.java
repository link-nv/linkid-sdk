package net.link.safeonline.messaging.bean;

import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailServer;
import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailServerPort;
import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailSender;
import static net.link.safeonline.messaging.bean.EmailConfigurationProviderBean.emailSubjectPrefix;

import static net.link.safeonline.messaging.bean.EmailBean.queueName;

import java.util.Date;
import java.util.Properties;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.link.safeonline.model.ConfigurationManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = queueName) })
public class EmailBean implements MessageListener {

	private final static Log LOG = LogFactory.getLog(EmailBean.class);

	public final static String queueName = "queue/outgoing-email";

	@EJB
	private ConfigurationManager configurationManager;

	public void onMessage(Message msg) {
		try {

			String server = this.configurationManager.findConfigItem(
					emailServer).getValue();
			String port = this.configurationManager.findConfigItem(
					emailServerPort).getValue();
			String sender = this.configurationManager.findConfigItem(
					emailSender).getValue();
			String prefix = this.configurationManager.findConfigItem(
					emailSubjectPrefix).getValue();

			EndUserMessage message = new EndUserMessage(msg);

			LOG.debug("Message received for: " + message.getDestination()
					+ " about: " + message.getSubject());

			Properties props = new Properties();
			props.put("mail.smtp.host", server);
			props.put("mail.smtp.port", port);
			Session session = Session.getInstance(props, null);

			MimeMessage mimemsg = new MimeMessage(session);
			mimemsg.setFrom(new InternetAddress(sender));
			InternetAddress[] address = { new InternetAddress(message
					.getDestination()) };
			mimemsg.setRecipients(javax.mail.Message.RecipientType.TO, address);
			mimemsg.setSubject(prefix + " " + message.getSubject());
			mimemsg.setSentDate(new Date());

			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message.getMessage());
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mimemsg.setContent(mp);

			// send the message
			Transport.send(mimemsg);

		} catch (Exception e) {
			throw new EJBException();
		}
	}

}
