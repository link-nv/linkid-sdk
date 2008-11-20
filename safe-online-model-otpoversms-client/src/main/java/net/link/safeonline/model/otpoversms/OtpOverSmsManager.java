/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface OtpOverSmsManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "OtpOverSmsManagerBean/local";


    void registerMobile(SubjectEntity subject, String mobile, String pin)
            throws PermissionDeniedException;

    void changePin(SubjectEntity subject, String mobile, String oldPin, String newPin)
            throws PermissionDeniedException, DeviceNotFoundException;

    boolean validatePin(SubjectEntity subject, String mobile, String pin)
            throws DeviceNotFoundException;

    void removeMobile(SubjectEntity subject, String mobile)
            throws DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException, AttributeNotFoundException;
}
