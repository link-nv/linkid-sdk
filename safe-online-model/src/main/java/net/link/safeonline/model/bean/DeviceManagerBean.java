/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.bean;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.SafeOnlineDeviceRoles;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.model.DeviceManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.security.SecurityDomain;


@Stateless
@SecurityDomain(SafeOnlineConstants.SAFE_ONLINE_DEVICE_SECURITY_DOMAIN)
public class DeviceManagerBean implements DeviceManager {

    private static final Log LOG = LogFactory.getLog(DeviceManagerBean.class);

    @Resource
    private SessionContext   context;

    @EJB
    private DeviceDAO        deviceDAO;


    @RolesAllowed(SafeOnlineDeviceRoles.DEVICE_ROLE)
    public DeviceEntity getCallerDevice() {

        Principal callerPrincipal = this.context.getCallerPrincipal();
        String deviceName = callerPrincipal.getName();
        LOG.debug("get caller device: " + deviceName);
        DeviceEntity callerDevice;
        try {
            callerDevice = this.deviceDAO.getDevice(deviceName);
        } catch (DeviceNotFoundException e) {
            throw new EJBException("device not found: " + e.getMessage(), e);
        }
        return callerDevice;
    }
}
