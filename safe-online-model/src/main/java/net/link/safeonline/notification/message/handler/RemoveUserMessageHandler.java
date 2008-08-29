/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.notification.message.handler;

import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.NodeAccountService;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
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

    public void handleMessage(String destination, List<String> message) {

        String userId = message.get(0);
        LOG.debug("handle remove message for user: " + userId);
        SubjectEntity subject = this.subjectService.findSubject(userId);
        if (null != subject) {
            LOG.debug("remove user: " + userId);
            this.nodeAccountService.removeAccount(subject);
        }
    }

    public List<String> createApplicationMessage(List<String> message, ApplicationEntity application) {

        List<String> returnMessage = new LinkedList<String>();
        String userId = message.get(0);
        String applicationUserId;
        try {
            applicationUserId = this.userIdMappingService.getApplicationUserId(application.getName(), userId);
        } catch (SubscriptionNotFoundException e) {
            return null;
        } catch (ApplicationNotFoundException e) {
            return null;
        }
        returnMessage.add(applicationUserId);
        return returnMessage;
    }

    public List<String> createNodeMessage(List<String> message, NodeEntity node) {

        List<String> returnMessage = new LinkedList<String>();
        String userId = message.get(0);
        NodeMappingEntity nodeMapping;
        try {
            nodeMapping = this.nodeMappingService.getNodeMapping(userId, node.getName());
        } catch (SubjectNotFoundException e) {
            return null;
        } catch (NodeNotFoundException e) {
            return null;
        }
        returnMessage.add(nodeMapping.getId());
        return returnMessage;
    }

}
