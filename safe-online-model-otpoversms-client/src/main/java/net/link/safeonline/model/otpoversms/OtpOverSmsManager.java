/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.entity.SubjectEntity;


@Local
public interface OtpOverSmsManager extends OtpOverSmsService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpOverSmsManagerBean/local";


    void registerMobile(SubjectEntity subject, String mobile, String pin)
            throws PermissionDeniedException;

    boolean changePin(SubjectEntity subject, String mobile, String oldPin, String newPin)
            throws DeviceNotFoundException;

    boolean validatePin(SubjectEntity subject, String mobile, String pin)
            throws DeviceNotFoundException;

    void removeMobile(SubjectEntity subject, String mobile)
            throws DeviceNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException;
}
