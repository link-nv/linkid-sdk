/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import java.net.ConnectException;

import javax.ejb.Local;
import javax.servlet.http.HttpSession;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface OtpOverSmsDeviceService extends OtpOverSmsService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpOverSmsDeviceServiceBean/local";


    String authenticate(String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException;

    void register(String userId, String mobile, String pin)
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException,
            AttributeNotFoundException;

    boolean update(String userId, String mobile, String oldPin, String newPin)
            throws DeviceNotFoundException, SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException,
            DeviceDisabledException;

    boolean remove(String userId, String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException,
            PermissionDeniedException, DeviceDisabledException;

    boolean disable(String userId, String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException, AttributeTypeNotFoundException;

    void requestOtp(HttpSession httpSession, String mobile)
            throws ConnectException;

    boolean verifyOtp(HttpSession httpSession, String mobile, String otp)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException;

    boolean verifyOtp(HttpSession httpSession, String otp);

    void checkMobile(String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException;

}
