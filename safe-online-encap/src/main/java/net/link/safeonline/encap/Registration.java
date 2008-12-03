/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.encap;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.AttributeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.MobileException;
import net.link.safeonline.authentication.exception.MobileRegistrationException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.custom.converter.PhoneNumber;


@Local
public interface Registration {

    public static final String JNDI_BINDING = EncapConstants.JNDI_PREFIX + "RegistrationBean/local";


    /*
     * Accessors.
     */
    PhoneNumber getMobile();

    void setMobile(PhoneNumber mobile);

    String getMobileActivationCode();

    String getMobileClientLink();

    String getMobileOTP();

    void setMobileOTP(String mobileOTP);

    String getChallengeId();

    /*
     * Actions.
     */
    String mobileRegister()
            throws MobileException, MalformedURLException, MobileRegistrationException;

    String cancel()
            throws IOException;

    String mobileActivationOk();

    String mobileActivationRetry()
            throws MalformedURLException, MobileException, MobileRegistrationException;

    String requestOTP()
            throws MalformedURLException, MobileException;

    String authenticate()
            throws IOException, MobileException, SubjectNotFoundException, AttributeTypeNotFoundException, PermissionDeniedException,
            AttributeNotFoundException;

    /*
     * Lifecycle.
     */
    void destroyCallback();
}
