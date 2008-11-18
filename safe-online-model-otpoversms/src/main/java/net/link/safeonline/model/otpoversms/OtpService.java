/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.otpoversms;

import java.net.ConnectException;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;


@Local
public interface OtpService extends SafeOnlineService {

    public static final String JNDI_BINDING       = SafeOnlineService.JNDI_PREFIX + "OtpServiceBean/local";

    public static final String DUMMY_OTP_LOCATION = "http://localhost:8080/safe-online-sms-ws/dummy";


    void requestOtp(String mobile)
            throws ConnectException;

    boolean verifyOtp(String otp);

}
