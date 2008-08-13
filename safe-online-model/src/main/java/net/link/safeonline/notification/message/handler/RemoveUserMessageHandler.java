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
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.UserIdMappingService;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.DeviceSubjectDAO;
import net.link.safeonline.dao.SubjectDAO;
import net.link.safeonline.dao.SubjectIdentifierDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.device.DeviceSubjectEntity;
import net.link.safeonline.notification.message.MessageHandler;
import net.link.safeonline.service.DeviceMappingService;
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

    private DeviceMappingService deviceMappingService;

    private SubjectService       subjectService;

    private AttributeDAO         attributeDAO;

    private SubjectIdentifierDAO subjectIdentifierDAO;

    private SubjectDAO           subjectDAO;

    private DeviceSubjectDAO     deviceSubjectDAO;


    public void init() {

        this.userIdMappingService = EjbUtils.getEJB("SafeOnline/UserIdMappingServiceBean/local",
                UserIdMappingService.class);
        this.deviceMappingService = EjbUtils.getEJB("SafeOnline/DeviceMappingServiceBean/local",
                DeviceMappingService.class);
        this.subjectService = EjbUtils.getEJB("SafeOnline/SubjectServiceBean/local", SubjectService.class);
        this.attributeDAO = EjbUtils.getEJB("SafeOnline/AttributeDAOBean/local", AttributeDAO.class);
        this.subjectIdentifierDAO = EjbUtils.getEJB("SafeOnline/SubjectIdentifierDAOBean/local",
                SubjectIdentifierDAO.class);
        this.subjectDAO = EjbUtils.getEJB("SafeOnline/SubjectDAOBean/local", SubjectDAO.class);
        this.deviceSubjectDAO = EjbUtils.getEJB("SafeOnline/DeviceSubjectDAOBean/local", DeviceSubjectDAO.class);
    }

    public void handleMessage(String destination, List<String> message) {

        String id = message.get(0);
        LOG.debug("remove device user: " + id);
        DeviceSubjectEntity deviceSubject = this.subjectService.findDeviceSubject(id);
        if (null != deviceSubject) {
            for (SubjectEntity deviceRegistration : deviceSubject.getRegistrations()) {
                this.attributeDAO.removeAttributes(deviceRegistration);
                this.subjectIdentifierDAO.removeSubjectIdentifiers(deviceRegistration);
                this.subjectDAO.removeSubject(deviceRegistration);
            }
            this.deviceSubjectDAO.removeSubject(deviceSubject);
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

    public List<String> createDeviceMessage(List<String> message, DeviceEntity device) {

        List<String> returnMessage = new LinkedList<String>();
        String userId = message.get(0);
        DeviceMappingEntity deviceMapping;
        try {
            deviceMapping = this.deviceMappingService.getDeviceMapping(userId, device.getName());
        } catch (SubjectNotFoundException e) {
            return null;
        } catch (DeviceNotFoundException e) {
            return null;
        }
        returnMessage.add(deviceMapping.getId());
        return returnMessage;
    }

}
