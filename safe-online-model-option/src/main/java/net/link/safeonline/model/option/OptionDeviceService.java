/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.model.option.exception.OptionAuthenticationException;
import net.link.safeonline.model.option.exception.OptionRegistrationException;


/**
 * <h2>{@link OptionDeviceService}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 8, 2008</i>
 * </p>
 * 
 * @author dhouthoo
 */
@Local
public interface OptionDeviceService {

    String authenticate(String imei, String pin) throws SubjectNotFoundException, OptionAuthenticationException,
            OptionRegistrationException;

    void register(String userId, String imei, String pin) throws OptionAuthenticationException,
            OptionRegistrationException;

    void remove(String userId, String imei, String pin) throws OptionAuthenticationException,
            OptionRegistrationException, SubjectNotFoundException;

}
