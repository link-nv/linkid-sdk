/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.notification;

import static net.link.safeonline.entity.notification.NotificationMessageEntity.QUERY_LIST_ALL;
import static net.link.safeonline.entity.notification.NotificationMessageEntity.QUERY_WHERE_CONTENT;
import static net.link.safeonline.entity.notification.NotificationMessageEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.notification.NotificationMessageEntity.QUERY_WHERE_SUBJECT_AND_CONTENT;
import static net.link.safeonline.entity.notification.NotificationMessageEntity.QUERY_WHERE_TOPIC;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;


@Entity
@Table(name = "notif_message")
@NamedQueries( {
        @NamedQuery(name = QUERY_LIST_ALL, query = "FROM NotificationMessageEntity msg"),
        @NamedQuery(name = QUERY_WHERE_TOPIC, query = "SELECT msg FROM NotificationMessageEntity AS msg "
                + "WHERE msg.topic = :topic AND msg.consumer = :consumer"),
        @NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT msg FROM NotificationMessageEntity AS msg "
                + "WHERE msg.topic = :topic AND msg.consumer = :consumer AND msg.subject = :subject"),
        @NamedQuery(name = QUERY_WHERE_CONTENT, query = "SELECT msg FROM NotificationMessageEntity AS msg "
                + "WHERE msg.topic = :topic AND msg.consumer = :consumer AND msg.content = :content"),
        @NamedQuery(name = QUERY_WHERE_SUBJECT_AND_CONTENT, query = "SELECT msg FROM NotificationMessageEntity AS msg "
                + "WHERE msg.topic = :topic AND msg.consumer = :consumer AND msg.subject = :subject AND msg.content = :content") })
public class NotificationMessageEntity implements Serializable {

    private static final long       serialVersionUID                = 1L;

    public static final String      QUERY_WHERE_TOPIC               = "not.msg.top";

    public static final String      QUERY_WHERE_SUBJECT             = "not.msg.sub";

    public static final String      QUERY_WHERE_SUBJECT_AND_CONTENT = "not.msg.sub.con";

    public static final String      QUERY_WHERE_CONTENT             = "not.msg.con";

    public static final String      QUERY_LIST_ALL                  = "not.msg.all";

    private long                    id;

    private String                  topic;

    private EndpointReferenceEntity consumer;

    private String                  subject;

    private String                  content;

    private long                    attempts;


    public NotificationMessageEntity() {

        // required

    }

    public NotificationMessageEntity(String topic, EndpointReferenceEntity consumer, String subject, String content) {

        this.topic = topic;
        this.consumer = consumer;
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

    @Transient
    public void addAttempt() {

        this.attempts++;

    }


    public interface QueryInterface {

        @QueryMethod(value = QUERY_WHERE_TOPIC, nullable = true)
        NotificationMessageEntity findNotificationMessage(@QueryParam("topic") String topic,
                @QueryParam("consumer") EndpointReferenceEntity consumer);

        @QueryMethod(value = QUERY_WHERE_SUBJECT, nullable = true)
        NotificationMessageEntity findNotificationMessageWhereSubject(@QueryParam("topic") String topic,
                @QueryParam("consumer") EndpointReferenceEntity consumer, @QueryParam("subject") String subject);

        @QueryMethod(value = QUERY_WHERE_CONTENT, nullable = true)
        NotificationMessageEntity findNotificationMessageWhereContent(@QueryParam("topic") String topic,
                @QueryParam("consumer") EndpointReferenceEntity consumer, @QueryParam("content") String content);

        @QueryMethod(value = QUERY_WHERE_SUBJECT_AND_CONTENT, nullable = true)
        NotificationMessageEntity findNotificationMessage(@QueryParam("topic") String topic,
                @QueryParam("consumer") EndpointReferenceEntity consumer, @QueryParam("subject") String subject,
                @QueryParam("content") String content);

        @QueryMethod(value = QUERY_LIST_ALL)
        List<NotificationMessageEntity> listNotificationMessages();

    }

}
