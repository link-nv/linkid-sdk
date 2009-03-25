/*
 * SafeOnline project.
 * 
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.authentication.service;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.servlet.http.Cookie;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;


/**
 * <h2>{@link SingleSignOnService}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * Single Sign On service interface. This service is used by the {@link AuthenticationService} and {@link LogoutService} for Single Sign On
 * / Single Logout. The bean behind this interface is stateful. So a certain method invocation pattern is needed.
 * 
 * XXX: complete me
 * </p>
 * 
 * <p>
 * <i>Mar 25, 2009</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface SingleSignOnService extends SafeOnlineService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "SingleSignOnServiceBean/local";


    /**
     * Aborts the single sign on procedure and removes the stateful instance.
     */
    void abort();

    /**
     * Initialize the single sign on procedure, given a list of application pool names, application authenticating against and device
     * restriction.
     * 
     * @param audiences
     *            the list of application pool names.
     * @param devices
     * @param application
     */
    void initialize(boolean forceAuthn, List<String> audiences, ApplicationEntity application, Set<DeviceEntity> devices);

    /**
     * Attempts to login, given a set of Single Sign On Cookies.
     * 
     * @return list of valid single sign on assertions or <code>null</code> if not successful. The list of invalid cookies can be retrieved
     *         with {@link #getInvalidCookies()}.
     */
    List<AuthenticationAssertion> login(List<Cookie> ssoCookies);

    /**
     * Returns list of invalid ( expired, ... ) SSO cookies.
     */
    List<Cookie> getInvalidCookies();
}
