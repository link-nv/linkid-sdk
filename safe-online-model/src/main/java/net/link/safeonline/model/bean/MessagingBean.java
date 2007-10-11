package net.link.safeonline.model.bean;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import net.link.safeonline.messaging.bean.EndUserMessage;
import net.link.safeonline.model.Messaging;

import static net.link.safeonline.messaging.bean.EmailBean.queueName;

@Stateless
public class MessagingBean implements Messaging {

	@Resource(mappedName = "ConnectionFactory")
	private ConnectionFactory factory;

	@Resource(mappedName = queueName)
	private Queue emailQueue;

	public void sendEmail(String to, String subject, String messageText) {

		EndUserMessage message = new EndUserMessage(to, subject, messageText);

		try {
			Connection connect = this.factory.createConnection();
			Session session = connect.createSession(true, 0);
			MessageProducer producer = session.createProducer(this.emailQueue);
			producer.send(message.getJMSMessage(session));
			session.close();
			connect.close();
		} catch (Exception e) {
			throw new EJBException();
		}

	}

}
