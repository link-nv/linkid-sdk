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

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface OtpOverSmsDeviceService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "OtpOverSmsDeviceServiceBean/local";


    String authenticate(String mobile, String pin)
            throws DeviceNotFoundException, SubjectNotFoundException;

    void register(String userId, String mobile, String pin)
            throws SubjectNotFoundException, DeviceNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException;

    void update(String userId, String mobile, String oldPin, String newPin)
            throws PermissionDeniedException, DeviceNotFoundException, SubjectNotFoundException;

    void remove(String userId, String mobile, String pin)
            throws DeviceNotFoundException, PermissionDeniedException, SubjectNotFoundException, AttributeTypeNotFoundException;

    void disable(String userId, String mobile)
            throws DeviceNotFoundException, SubjectNotFoundException, DeviceRegistrationNotFoundException;

    void requestOtp(HttpSession httpSession, String mobile)
            throws ConnectException;

    boolean verifyOtp(HttpSession httpSession, String otp);

    void checkMobile(String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException;

}
