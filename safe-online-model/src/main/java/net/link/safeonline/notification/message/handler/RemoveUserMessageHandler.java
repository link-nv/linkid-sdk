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
import net.link.safeonline.authentication.service.NodeAccountService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.notification.message.NotificationMessage;
import net.link.safeonline.notification.message.MessageHandler;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Message handler for topic: {@link SafeOnlineConstants#TOPIC_REMOVE_USER}.
 * 
 * @author wvdhaute
 * 
 */
public class RemoveUserMessageHandler implements MessageHandler {

    private static final Log     LOG = LogFactory.getLog(RemoveUserMessageHandler.class);

    private UserIdMappingService userIdMappingService;

    private NodeMappingService   nodeMappingService;

    private SubjectService       subjectService;

    private NodeAccountService   nodeAccountService;


    public void init() {

        this.userIdMappingService = EjbUtils.getEJB("SafeOnline/UserIdMappingServiceBean/local",
                UserIdMappingService.class);
        this.nodeMappingService = EjbUtils.getEJB("SafeOnline/NodeMappingServiceBean/local", NodeMappingService.class);
        this.nodeAccountService = EjbUtils.getEJB("SafeOnline/NodeAccountServiceBean/local", NodeAccountService.class);
        this.subjectService = EjbUtils.getEJB("SafeOnline/SubjectServiceBean/local", SubjectService.class);
    }

    public void handleMessage(String destination, String subject, String content) {

        LOG.debug("handle remove message for user: " + subject);
        SubjectEntity subjectEntity = this.subjectService.findSubject(subject);
        if (null != subject) {
            LOG.debug("remove user: " + subject);
            this.nodeAccountService.removeAccount(subjectEntity);
        }
    }

    public NotificationMessage createMessage(String topic, String subject, String content, EndpointReferenceEntity consumer) {

        if (null != consumer.getApplication()) {
            try {
                String applicationUserId = this.userIdMappingService.getApplicationUserId(consumer.getApplication()
                        .getName(), subject);
                return new NotificationMessage(consumer.getApplication().getName(), applicationUserId, content);
            } catch (SubscriptionNotFoundException e) {
                return null;
            } catch (ApplicationNotFoundException e) {
                return null;
            }
        }
        if (null != consumer.getNode()) {
            try {
                NodeMappingEntity nodeMapping = this.nodeMappingService.getNodeMapping(subject, consumer.getNode()
                        .getName());
                return new NotificationMessage(consumer.getNode().getName(), nodeMapping.getId(), content);
            } catch (SubjectNotFoundException e) {
                return null;
            } catch (NodeNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
