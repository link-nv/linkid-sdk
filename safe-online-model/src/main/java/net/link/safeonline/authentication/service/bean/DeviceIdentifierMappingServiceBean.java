/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineDeviceRoles;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DeviceIdentifierMappingService;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.DeviceMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.DeviceManager;
import net.link.safeonline.service.DeviceMappingService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;

@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_DEVICE_SECURITY_DOMAIN)
public class DeviceIdentifierMappingServiceBean implements
        DeviceIdentifierMappingService {

    private static final Log     LOG = LogFactory
                                             .getLog(DeviceIdentifierMappingServiceBean.class);

    @EJB
    private DeviceManager        deviceManager;

    @EJB
    private DeviceMappingService deviceMappingService;

    @EJB
    private SubjectService       subjectService;


    @RolesAllowed(SafeOnlineDeviceRoles.DEVICE_ROLE)
    public String getDeviceMappingId(String username)
            throws DeviceNotFoundException, SubjectNotFoundException {

        LOG.debug("get device mapping id: " + username);
        DeviceEntity device = this.deviceManager.getCallerDevice();
        SubjectEntity subject = this.subjectService
                .getSubjectFromUserName(username);

        DeviceMappingEntity deviceMapping = this.deviceMappingService
                .getDeviceMapping(subject.getUserId(), device.getName());
        return deviceMapping.getId();
    }
}
