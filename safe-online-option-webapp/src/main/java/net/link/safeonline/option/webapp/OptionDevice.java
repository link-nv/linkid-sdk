/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.option.webapp;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.authentication.exception.DeviceAuthenticationException;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;


/**
 * <h2>{@link OptionDevice}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Feb 9, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class OptionDevice {

    private static final Log    LOG              = LogFactory.getLog(OptionDevice.class);

    public static final String  PIN_COOKIE_NAME  = "OPTION.pin";
    private static final String IMEI_COOKIE_NAME = "OPTION.imei";


    /**
     * Check whether the user's registered device is registered with the given PIN code, and if so, returns its IMEI number.
     * 
     * @return The IMEI of the dummy Option device registered by the web user, provided the given PIN is correct.
     * 
     * @throws DeviceAuthenticationException
     *             If the given PIN is incorrect for the user's registered device.
     */
    public static String validate(String pin)
            throws DeviceAuthenticationException {

        WebRequest webRequest = ((WebRequestCycle) RequestCycle.get()).getWebRequest();

        Cookie pinCookie = webRequest.getCookie(PIN_COOKIE_NAME);
        if (pin == null || pinCookie == null || !pin.equals(pinCookie.getValue())) {
            // Invalid PIN.
            LOG.debug("Validation failed of PIN: " + pin);
            LOG.debug("PIN Cookie (" + PIN_COOKIE_NAME + ") was: " + (pinCookie == null? "[no cookie]": pinCookie.getValue()));

            throw new DeviceAuthenticationException();
        }

        Cookie imeiCookie = webRequest.getCookie(IMEI_COOKIE_NAME);
        return imeiCookie != null? imeiCookie.getValue(): null;
    }

    /**
     * Register a new device for the current user with the given PIN code.
     * 
     * @return The IMEI of the device the user just registered with the given PIN code.
     */
    public static String register(String pin) {

        // Generate a random IMEI number (15 random digits).
        String imei = String.format("%015d", new Double(Math.pow(10, 16) * Math.random()).longValue());

        return _update(pin, imei);
    }

    /**
     * Update the device registration of the current user with the given PIN and IMEI number.
     * 
     * @return The IMEI number of the updated device.
     */
    public static String _update(String pin, String imei) {

        WebResponse webResponse = ((WebRequestCycle) RequestCycle.get()).getWebResponse();

        Cookie pinCookie = new Cookie(PIN_COOKIE_NAME, pin);
        pinCookie.setMaxAge(Integer.MAX_VALUE);
        webResponse.addCookie(pinCookie);

        Cookie imeiCookie = new Cookie(IMEI_COOKIE_NAME, imei);
        imeiCookie.setMaxAge(Integer.MAX_VALUE);
        webResponse.addCookie(imeiCookie);

        // TODO: Manually add cookies to the MockHttpServletRequest until https://issues.apache.org/jira/browse/WICKET-1886 is fixed.
        HttpServletRequest servletRequest = WicketUtil.toServletRequest();
        if (servletRequest instanceof org.apache.wicket.protocol.http.MockHttpServletRequest) {
            ((MockHttpServletRequest) servletRequest).addCookie(pinCookie);
            ((MockHttpServletRequest) servletRequest).addCookie(imeiCookie);
        }

        return imei;
    }

    /**
     * Update the PIN code of the current user's registered device.\
     * 
     * @return The IMEI number of the updated device.
     * 
     * @throws DeviceAuthenticationException
     *             The old PIN code isn't valid or no device is currently registered.
     */
    public static String update(String oldPin, String newPin)
            throws DeviceAuthenticationException {

        String imei = validate(oldPin);

        return _update(newPin, imei);
    }

    /**
     * Unregister the current user's device with the given IMEI.
     * 
     * @param imei
     *            The IMEI of the device the user should have registered.
     * @return <code>false</code> The current user has no device with the given IMEI registered.
     */
    public static boolean unregister(String imei, HttpServletRequest request, HttpServletResponse response) {

        if (imei == null || !imei.equals(OptionDevice.find(request)))
            return false;

        Cookie imeiCookie = new Cookie(IMEI_COOKIE_NAME, null);
        imeiCookie.setMaxAge(0);
        response.addCookie(imeiCookie);

        Cookie pinCookie = new Cookie(PIN_COOKIE_NAME, null);
        pinCookie.setMaxAge(0);
        response.addCookie(pinCookie);

        return true;
    }

    /**
     * @return The IMEI of the current user's registered device or <code>null</code> if he has none.
     */
    public static String find(HttpServletRequest request) {

        for (Cookie c : request.getCookies())
            if (c.getName().equals(IMEI_COOKIE_NAME))
                return c.getValue();

        return null;
    }
}
