/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.notification.message;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;


/**
 * <h2>{@link NotificationMessage}<br>
 * <sub>Notification Message class.</sub></h2>
 * 
 * <p>
 * Notification Message class used by the notifications queue
 * </p>
 * 
 * <p>
 * <i>Oct 27, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            topic;

    private String            destination;

    private String            subject;

    private String            content;

    private long              consumerId;


    public NotificationMessage(String topic, String destination, String subject, String content, long consumerId) {

        this.topic = topic;
        this.destination = destination;
        this.subject = subject;
        this.content = content;
        this.consumerId = consumerId;
    }

    public NotificationMessage(Message message) throws JMSException {

        topic = message.getStringProperty("topic");
        destination = message.getStringProperty("destination");
        subject = message.getStringProperty("subject");
        content = message.getStringProperty("content");
        consumerId = message.getLongProperty("consumerId");
    }

    public Message getJMSMessage(Session session)
            throws JMSException {

        Message message = session.createMessage();
        message.setStringProperty("topic", topic);
        message.setStringProperty("destination", destination);
        message.setStringProperty("subject", subject);
        message.setStringProperty("content", content);
        message.setLongProperty("consumerId", consumerId);
        return message;
    }

    public String getTopic() {

        return topic;
    }

    public String getDestination() {

        return destination;
    }

    public String getSubject() {

        return subject;
    }

    public String getContent() {

        return content;
    }

    public long getConsumerId() {

        return consumerId;
    }
}
