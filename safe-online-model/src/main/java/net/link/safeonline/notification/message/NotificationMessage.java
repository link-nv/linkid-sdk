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

    private String            destination;

    private String            subject;

    private String            content;


    public NotificationMessage(String destination, String subject, String content) {

        this.destination = destination;
        this.subject = subject;
        this.content = content;
    }

    public NotificationMessage(Message message) throws JMSException {

        this.auditContextId = message.getLongProperty("auditContextId");
    }

    public Message getJMSMessage(Session session) throws JMSException {

        Message message = session.createMessage();
        message.setLongProperty("auditContextId", this.auditContextId);

        return message;
    }

    public String getDestination() {

        return this.destination;
    }

    public String getSubject() {

        return this.subject;
    }

    public String getContent() {

        return this.content;
    }

}
