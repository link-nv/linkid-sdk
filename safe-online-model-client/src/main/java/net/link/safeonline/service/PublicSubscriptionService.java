/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service;

import javax.ejb.Local;

import net.link.safeonline.SafeOnlineService;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;


/**
 * <h2>{@link PublicSubscriptionService}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 10, 2008</i>
 * </p>
 * 
 * @author wvdhaute
 */
@Local
public interface PublicSubscriptionService {

    public static final String JNDI_BINDING = SafeOnlineService.JNDI_PREFIX + "PublicSubscriptionServiceBean/local";


    boolean isSubscribed(SubjectEntity subject, ApplicationEntity application)
            throws SubscriptionNotFoundException;

}
