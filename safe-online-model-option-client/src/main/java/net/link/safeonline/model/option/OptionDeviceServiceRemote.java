/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.model.option;

import javax.ejb.Remote;


/**
 * <h2>{@link OptionDeviceServiceRemote}<br>
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
@Remote
public interface OptionDeviceServiceRemote extends OptionDeviceService {

    public static final String JNDI_BINDING = OptionService.JNDI_PREFIX + "OptionDeviceServiceBean/remote";

}
