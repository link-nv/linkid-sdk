/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.audit;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;


public class AuditMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long              auditContextId;


    public Long getAuditContextId() {

        return this.auditContextId;
    }

    public void setAuditContextId(Long auditContextId) {

        this.auditContextId = auditContextId;
    }

    public AuditMessage(Long auditContextId) {

        this.auditContextId = auditContextId;
    }

    public AuditMessage(Message message) throws JMSException {

        this.auditContextId = message.getLongProperty("auditContextId");
    }

    public Message getJMSMessage(Session session) throws JMSException {

        Message message = session.createMessage();
        message.setLongProperty("auditContextId", this.auditContextId);
        return message;
    }

}
