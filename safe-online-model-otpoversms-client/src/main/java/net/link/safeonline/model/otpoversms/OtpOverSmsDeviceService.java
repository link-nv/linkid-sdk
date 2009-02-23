/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.osgi.sms.exception.SmsServiceException;


@Local
public interface OtpOverSmsDeviceService extends OtpOverSmsService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpOverSmsDeviceServiceBean/local";


    String authenticate(String pin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException;

    void register(String nodeName, String userId, String pin, String otp)
            throws DeviceAuthenticationException;

    void update(String userId, String oldPin, String newPin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceDisabledException, DeviceAuthenticationException;

    void remove(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    void enable(String userId, String pin, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, DeviceAuthenticationException;

    void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    void requestOtp(String mobile)
            throws SmsServiceException, SafeOnlineResourceException, SubjectNotFoundException, DeviceRegistrationNotFoundException;

    /**
     * @return <code>true</code> when the OTP has been dispatched to the user.
     */
    boolean isChallenged();
}
