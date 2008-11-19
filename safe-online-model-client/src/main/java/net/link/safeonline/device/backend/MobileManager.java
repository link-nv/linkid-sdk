/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.device.backend;

import java.net.MalformedURLException;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.MobileException;


@Local
public interface MobileManager extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "MobileManagerBean/local";


    String requestOTP(String mobile)
            throws MalformedURLException, MobileException;

    boolean verifyOTP(String challengeId, String OTPValue)
            throws MalformedURLException, MobileException;

    String activate(String mobile, String sessionInfo)
            throws MalformedURLException, MobileException;

    void remove(String mobile)
            throws MalformedURLException, MobileException;

    void lock(String mobile)
            throws MalformedURLException, MobileException;

    void unLock(String mobile)
            throws MalformedURLException, MobileException;

    String getClientDownloadLink();
}
