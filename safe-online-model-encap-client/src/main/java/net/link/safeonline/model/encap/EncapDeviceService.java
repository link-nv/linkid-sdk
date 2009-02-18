/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.encap;

import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceRegistrationException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;


@Local
public interface EncapDeviceService extends EncapService {

    public static final String JNDI_BINDING = EncapService.JNDI_PREFIX + "EncapDeviceServiceBean/local";


    /**
     * Authenticate against the encap server and verifies with OLAS.
     * 
     * @param mobile
     * @param mobileOTP
     * @return device subject ID
     * @throws DeviceRegistrationNotFoundException
     * @throws DeviceDisabledException
     * @throws SubjectNotFoundException
     * @throws MobileException
     * @throws DeviceAuthenticationException
     */
    String authenticate(String mobileOTP)
            throws SubjectNotFoundException, DeviceDisabledException, DeviceRegistrationNotFoundException, MobileException,
            DeviceAuthenticationException;

    /**
     * Activates the specified mobile at the encap server.
     * 
     * @param mobile
     * @param sessionId
     * @return activationCode code to be used by the user on his mobile
     * @throws MobileException
     * @throws DeviceRegistrationException
     */
    String register(String mobile)
            throws MobileException, DeviceRegistrationException;

    /**
     * Commits the encap registration for OLAS, creates a device subject if necessary, creates a new device registration for this mobile and
     * attaches it to the device subject.
     * 
     * @param nodeName
     * @param userId
     * @param mobile
     * @param otp
     * @throws MobileException
     * @throws DeviceAuthenticationException
     * @throws NodeNotFoundException
     */
    void commitRegistration(String nodeName, String userId, mobile, String otp)
            throws MobileException, DeviceAuthenticationException, NodeNotFoundException;

    void remove(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, MobileException;

    /**
     * Requests the encap server to send an OTP to the specified mobile.
     * 
     * @param mobile
     */
    void requestOTP(String mobile)
            throws MobileException;

    /**
     * Returns list of mobiles registered with this user.
     * 
     * @param userId
     * @param locale
     * @throws SubjectNotFoundException
     */
    List<AttributeDO> getMobiles(String userId, Locale locale)
            throws SubjectNotFoundException;

    /**
     * Disables the encap device registration.
     * 
     * @param userId
     * @param mobile
     * @throws DeviceRegistrationNotFoundException
     * @throws SubjectNotFoundException
     */
    void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException;

    /**
     * Enables the encap device registration.
     * 
     * @param userId
     * @param mobile
     * @param otp
     * @throws DeviceRegistrationNotFoundException
     * @throws SubjectNotFoundException
     * @throws DeviceAuthenticationException
     * @throws MobileException
     */
    void enable(String userId, String otp)
            throws SubjectNotFoundException, DeviceRegistrationNotFoundException, MobileException, DeviceAuthenticationException;

    /**
     * @return <code>true</code> when an OTP has been dispatched to the user and we're waiting to verify it.
     */
    boolean isChallenged();
}
