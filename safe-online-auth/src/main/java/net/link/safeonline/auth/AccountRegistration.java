/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth;

import java.io.IOException;
import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.DeviceNotFoundException;
import net.link.safeonline.authentication.exception.EmptyDevicePolicyException;
import net.link.safeonline.authentication.exception.ExistingUserException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;


@Local
public interface AccountRegistration {

    static final String REQUESTED_USERNAME_ATTRIBUTE = "requestedUsername";


    /*
     * Lifecycle.
     */
    void destroyCallback();

    /*
     * Accessors.
     */
    String getLogin();

    void setLogin(String login);

    String getDevice();

    void setDevice(String device);

    String getGivenCaptcha();

    void setGivenCaptcha(String givenCaptcha);

    String getCaptchaURL();

    String getUsername();

    /*
     * Factories
     */
    List<SelectItem> allDevicesFactory() throws ApplicationNotFoundException, EmptyDevicePolicyException;

    /*
     * Actions.
     */
    String loginNext() throws ExistingUserException, AttributeTypeNotFoundException, PermissionDeniedException;

    String deviceNext() throws DeviceNotFoundException, IOException;
}
