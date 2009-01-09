package net.link.safeonline.messaging.bean;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;


public class EndUserMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            destination;

    private String            subject;

    private String            message;


    public EndUserMessage(String destination, String subject, String message) {

        this.destination = destination;
        this.subject = subject;
        this.message = message;
    }

    public EndUserMessage(Message JMSMessage) throws JMSException {

        destination = JMSMessage.getStringProperty("destination");
        subject = JMSMessage.getStringProperty("subject");
        message = JMSMessage.getStringProperty("messagetext");
    }

    public TextMessage getJMSMessage(Session session)
            throws JMSException {

        TextMessage JMSMessage = session.createTextMessage();
        JMSMessage.setStringProperty("destination", destination);
        JMSMessage.setStringProperty("subject", subject);
        JMSMessage.setStringProperty("messagetext", message);
        return JMSMessage;
    }

    public String getDestination() {

        return destination;
    }

    public void setDestination(String destination) {

        this.destination = destination;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

}
