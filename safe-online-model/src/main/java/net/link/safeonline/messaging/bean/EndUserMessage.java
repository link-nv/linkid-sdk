package net.link.safeonline.messaging.bean;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

public class EndUserMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String destination;

	private String subject;

	private String message;

	public EndUserMessage(String destination, String subject, String message) {
		this.destination = destination;
		this.subject = subject;
		this.message = message;
	}

	public EndUserMessage(Message JMSMessage) throws JMSException {
		this.destination = JMSMessage.getStringProperty("destination");
		this.subject = JMSMessage.getStringProperty("subject");
		this.message = JMSMessage.getStringProperty("messagetext");
	}

	public TextMessage getJMSMessage(Session session) throws JMSException {
		TextMessage JMSMessage = session.createTextMessage();
		JMSMessage.setStringProperty("destination", this.destination);
		JMSMessage.setStringProperty("subject", this.subject);
		JMSMessage.setStringProperty("messagetext", this.message);
		return JMSMessage;
	}

	public String getDestination() {
		return this.destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
