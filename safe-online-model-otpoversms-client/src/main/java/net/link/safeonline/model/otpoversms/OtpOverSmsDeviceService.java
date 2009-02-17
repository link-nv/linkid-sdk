/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import java.net.ConnectException;

import javax.ejb.Local;
import javax.mail.AuthenticationFailedException;

import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;


@Local
public interface OtpOverSmsDeviceService extends OtpOverSmsService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpOverSmsDeviceServiceBean/local";


    String authenticate(String mobile, String pin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException;

    void register(String userId, String mobile, String pin, String otp)
            throws PermissionDeniedException, AuthenticationFailedException;

    boolean update(String userId, String mobile, String otp, String oldPin, String newPin)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException;

    void remove(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    boolean enable(String userId, String mobile, String otp, String pin)
            throws SubjectNotFoundException, AuthenticationFailedException, DeviceRegistrationNotFoundException;

    void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    void requestOtp(String mobile)
            throws ConnectException, SafeOnlineResourceException, SubjectNotFoundException, DeviceRegistrationNotFoundException,
            DeviceDisabledException;

    /**
     * @return <code>true</code> when the OTP has been dispatched to the user.
     */
    boolean isChallenged();
}
