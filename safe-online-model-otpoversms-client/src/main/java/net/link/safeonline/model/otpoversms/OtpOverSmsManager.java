/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface OtpOverSmsManager extends OtpOverSmsService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpOverSmsManagerBean/local";


    void registerMobile(SubjectEntity subject, String mobile, String pin)
            throws PermissionDeniedException;

    void updatePin(SubjectEntity subject, String mobile, String oldPin, String newPin)
            throws DeviceRegistrationNotFoundException;

    boolean validatePin(SubjectEntity subject, String mobile, String pin)
            throws DeviceRegistrationNotFoundException;

    void removeMobile(SubjectEntity subject, String mobile)
            throws DeviceRegistrationNotFoundException;
}
