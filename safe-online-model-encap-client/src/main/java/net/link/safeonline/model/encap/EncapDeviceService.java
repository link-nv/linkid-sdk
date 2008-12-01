/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.encap;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceDisabledException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.DeviceRegistrationNotFoundException;
import net.link.safeonline.authentication.exception.MobileAuthenticationException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.data.AttributeDO;


@Local
public interface EncapDeviceService extends EncapService {

    public static final String JNDI_BINDING = EncapService.JNDI_PREFIX + "EncapDeviceServiceBean/local";


    /**
     * Verifies if this mobile is registered with OLAS.
     * 
     * @param mobile
     * @throws SubjectNotFoundException
     * @throws AttributeTypeNotFoundException
     * @throws AttributeNotFoundException
     * @throws DeviceDisabledException
     */
    void checkMobile(String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, AttributeNotFoundException, DeviceDisabledException;

    /**
     * Authenticate against the encap server and verifies with OLAS.
     * 
     * @param mobile
     * @param challengeId
     * @param mobileOTP
     * @return device subject ID
     * @throws MalformedURLException
     * @throws SubjectNotFoundException
     * @throws MobileAuthenticationException
     * @throws MobileException
     */
    String authenticate(String mobile, String challengeId, String mobileOTP)
            throws MalformedURLException, SubjectNotFoundException, MobileAuthenticationException, MobileException;

    /**
     * Authenticates against the encap server.
     * 
     * @param challengeId
     * @param mobileOTP
     * @return true or false
     * @throws MalformedURLException
     * @throws MobileException
     */
    boolean authenicateEncap(String challengeId, String mobileOTP)
            throws MalformedURLException, MobileException;

    /**
     * Activates the specified mobile at the encap server.
     * 
     * @param mobile
     * @param sessionId
     * @return activationCode code to be used by the user on his mobile
     * @throws MalformedURLException
     * @throws MobileException
     * @throws MobileRegistrationException
     */
    String register(String mobile, String sessionId)
            throws MalformedURLException, MobileException, MobileRegistrationException;

    /**
     * Commits the encap registration for OLAS, creates a device subject if necessary, creates a new device registration for this mobile and
     * attaches it to the device subject.
     * 
     * @param deviceUserId
     * @param mobile
     * @throws SubjectNotFoundException
     * @throws AttributeTypeNotFoundException
     */
    void commitRegistration(String userId, String mobile)
            throws SubjectNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException, AttributeNotFoundException;

    void removeEncapMobile(String mobile)
            throws MalformedURLException, MobileException;

    void remove(String userId, String mobile)
            throws MobileException, MalformedURLException, SubjectNotFoundException, AttributeTypeNotFoundException,
            PermissionDeniedException, AttributeNotFoundException;

    /**
     * Requests the encap server to send an OTP to the specified mobile.
     * 
     * @param mobile
     * @throws MalformedURLException
     * @throws MobileException
     */
    String requestOTP(String mobile)
            throws MalformedURLException, MobileException;

    /**
     * Returns list of mobiles registered with this user.
     * 
     * @param userId
     * @param locale
     * @throws SubjectNotFoundException
     * @throws DeviceNotFoundException
     */
    List<AttributeDO> getMobiles(String userId, Locale locale)
            throws SubjectNotFoundException, DeviceNotFoundException;

    /**
     * Disables the encap device registration.
     * 
     * @param userId
     * @param mobile
     * @throws SubjectNotFoundException
     * @throws DeviceNotFoundException
     * @throws DeviceRegistrationNotFoundException
     * @throws MobileException
     * @throws MalformedURLException
     */
    void disable(String userId, String mobile)
            throws SubjectNotFoundException, DeviceNotFoundException, DeviceRegistrationNotFoundException, MalformedURLException,
            MobileException;
}
