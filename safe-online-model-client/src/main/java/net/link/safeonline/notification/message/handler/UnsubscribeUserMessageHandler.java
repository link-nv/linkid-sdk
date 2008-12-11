/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message.handler;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.notification.message.MessageHandler;
import net.link.safeonline.notification.message.NotificationMessage;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.PublicSubscriptionService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;


/**
 * Message handler for topic: {@link SafeOnlineConstants#TOPIC_UNSUBSCRIBE_USER}.
 * 
 * @author wvdhaute
 * 
 */
public class UnsubscribeUserMessageHandler implements MessageHandler {

    /**
     * {@inheritDoc}
     */
    public void handleMessage(String destination, String subject, String content) {

        // do nothing
        return;
    }

    /**
     * {@inheritDoc}
     */
    public NotificationMessage createMessage(String topic, String subject, String content, EndpointReferenceEntity consumer) {

        UserIdMappingService userIdMappingService = EjbUtils.getEJB(UserIdMappingService.JNDI_BINDING, UserIdMappingService.class);
        NodeMappingService nodeMappingService = EjbUtils.getEJB(NodeMappingService.JNDI_BINDING, NodeMappingService.class);
        SubjectService subjectService = EjbUtils.getEJB(SubjectService.JNDI_BINDING, SubjectService.class);
        PublicSubscriptionService publicSubscriptionService = EjbUtils.getEJB(PublicSubscriptionService.JNDI_BINDING,
                PublicSubscriptionService.class);

        if (null != consumer.getApplication()) {
            try {
                SubjectEntity subjectEntity = subjectService.getSubject(subject);
                if (publicSubscriptionService.isSubscribed(subjectEntity, consumer.getApplication())) {
                    String applicationUserId = userIdMappingService.getApplicationUserId(consumer.getApplication().getName(), subject);
                    return new NotificationMessage(topic, consumer.getApplication().getName(), applicationUserId, content, consumer.getId());
                }
                return null;
            } catch (SubscriptionNotFoundException e) {
                return null;
            } catch (ApplicationNotFoundException e) {
                return null;
            } catch (SubjectNotFoundException e) {
                return null;
            }
        }
        if (null != consumer.getNode()) {
            try {
                NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject, consumer.getNode().getName());
                return new NotificationMessage(topic, consumer.getNode().getName(), nodeMapping.getId(), content, consumer.getId());
            } catch (SubjectNotFoundException e) {
                return null;
            } catch (NodeNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
