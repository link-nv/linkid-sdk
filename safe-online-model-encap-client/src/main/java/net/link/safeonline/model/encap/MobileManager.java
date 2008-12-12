/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.encap;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.MobileException;


@Local
public interface MobileManager extends EncapService {

    public static final String JNDI_BINDING = EncapService.JNDI_PREFIX + "MobileManagerBean/local";


    String requestOTP(String mobile)
            throws MobileException;

    boolean verifyOTP(String challengeId, String OTPValue)
            throws MobileException;

    String activate(String mobile, String sessionInfo)
            throws MobileException;

    void remove(String mobile)
            throws MobileException;

    void lock(String mobile)
            throws MobileException;

    void unLock(String mobile)
            throws MobileException;

    String getClientDownloadLink();
}
