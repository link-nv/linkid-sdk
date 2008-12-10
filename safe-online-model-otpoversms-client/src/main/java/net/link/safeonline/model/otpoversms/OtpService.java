/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import java.net.ConnectException;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SafeOnlineResourceException;


@Local
public interface OtpService extends OtpOverSmsService {

    public static final String JNDI_BINDING = OtpOverSmsService.JNDI_PREFIX + "OtpServiceBean/local";


    void requestOtp(String mobile)
            throws ConnectException, SafeOnlineResourceException;

    boolean verifyOtp(String otp);

}
