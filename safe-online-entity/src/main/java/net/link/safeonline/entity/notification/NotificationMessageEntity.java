/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name = "notif_message")
public class NotificationMessageEntity implements Serializable {

    private static final long       serialVersionUID = 1L;

    private long                    id;

    private String                  topic;

    private EndpointReferenceEntity consumer;

    private String                  destination;

    private String                  subject;

    private String                  content;

    private long                    attempts;


    public NotificationMessageEntity() {

        // required

    }

    public NotificationMessageEntity(String topic, EndpointReferenceEntity consumer, String destination,
            String subject, String content) {

        this.topic = topic;
        this.consumer = consumer;
        this.destination = destination;
        this.subject = subject;
        this.content = content;
        this.attempts = 1;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "consumer")
    public EndpointReferenceEntity getConsumer() {

        return this.consumer;
    }

    public void setConsumer(EndpointReferenceEntity consumer) {

        this.consumer = consumer;
    }

    public String getTopic() {

        return this.topic;
    }

    public void setTopic(String topic) {

        this.topic = topic;
    }

    public String getDestination() {

        return this.destination;
    }

    public void setDestination(String destination) {

        this.destination = destination;
    }

    public String getSubject() {

        return this.subject;

    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    public String getContent() {

        return this.content;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public long getAttempts() {

        return this.attempts;
    }

    public void setAttempts(long attempts) {

        this.attempts = attempts;
    }


    public interface QueryInterface {

    }

}
