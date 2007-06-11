package net.link.safeonline.messaging.bean;

import static net.link.safeonline.messaging.bean.EmailBean.queueName;

import java.util.Date;
import java.util.Properties;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.interceptor.Interceptors;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.model.ConfigurationInterceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = queueName) })
@Interceptors(ConfigurationInterceptor.class)
@Configurable(group = "E-mail configuration")
public class EmailBean implements MessageListener {

	private final static Log LOG = LogFactory.getLog(EmailBean.class);

	public final static String queueName = "queue/outgoing-email";

	@Configurable(name = "Outgoing mail server")
	private String emailServer = "127.0.0.1";

	@Configurable(name = "Mail server port")
	private String emailServerPort = "25";

	@Configurable(name = "E-mail sender")
	private String emailSender = "safeonline@lin-k.net";

	@Configurable(name = "Subject prefix")
	private String emailSubjectPrefix = "[Safe Online]";

	public void onMessage(Message msg) {
		try {

			EndUserMessage message = new EndUserMessage(msg);

			LOG.debug("Message received for: " + message.getDestination()
					+ " about: " + message.getSubject());

			Properties props = new Properties();
			props.put("mail.smtp.host", emailServer);
			props.put("mail.smtp.port", emailServerPort);
			Session session = Session.getInstance(props, null);

			MimeMessage mimemsg = new MimeMessage(session);
			mimemsg.setFrom(new InternetAddress(emailSender));
			InternetAddress[] address = { new InternetAddress(message
					.getDestination()) };
			mimemsg.setRecipients(javax.mail.Message.RecipientType.TO, address);
			mimemsg.setSubject(emailSubjectPrefix + " " + message.getSubject());
			mimemsg.setSentDate(new Date());

			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(message.getMessage());
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			mimemsg.setContent(mp);

			// send the message
			Transport.send(mimemsg);

		} catch (Exception e) {
			throw new EJBException(e);
		}
	}

}
