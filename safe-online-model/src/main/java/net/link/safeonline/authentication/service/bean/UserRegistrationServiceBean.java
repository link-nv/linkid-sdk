/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.authentication.service.bean;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.DevicePolicyService;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.UserRegistrationService;
import net.link.safeonline.authentication.service.UserRegistrationServiceRemote;
import net.link.safeonline.entity.DeviceEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.model.UserRegistrationManager;
import net.link.safeonline.service.SubjectService;


/**
 * Implementation of user registration service interface. This component does not live within the SafeOnline core
 * security domain. This because a user that is about to register himself is not yet logged on into the system.
 * 
 * @author fcorneli
 * 
 */
@Stateless
public class UserRegistrationServiceBean implements UserRegistrationService, UserRegistrationServiceRemote {

    @EJB
    private SubjectService          subjectService;

    @EJB
    private UserRegistrationManager userRegistrationManager;

    @EJB
    private DevicePolicyService     devicePolicyService;

    @EJB
    private ProxyAttributeService   proxyAttributeService;


    public SubjectEntity registerUser(String login) throws ExistingUserException, AttributeTypeNotFoundException,
            PermissionDeniedException, AttributeUnavailableException {

        SubjectEntity subject = this.subjectService.findSubjectFromUserName(login);
        if (null == subject)
            return this.userRegistrationManager.registerUser(login);

        // For each device configured in OLAS: try to find the device attribute, if so, user was already registered with
        // a valid device so throw an exception
        List<DeviceEntity> devices = this.devicePolicyService.getDevices();
        for (DeviceEntity device : devices) {
            Object attribute;
            try {
                attribute = this.proxyAttributeService.findAttributeValue(subject.getUserId(), device
                        .getAttributeType().getName());
            } catch (SubjectNotFoundException e) {
                continue;
            }
            if (null != attribute) {
                throw new ExistingUserException();
            }
        }
        return subject;
    }
}
