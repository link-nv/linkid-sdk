/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service.bean;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.service.PublicSubscriptionService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link PublicSubscriptionServiceBean}<br>
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
@Stateless
@LocalBinding(jndiBinding = PublicSubscriptionService.JNDI_BINDING)
public class PublicSubscriptionServiceBean implements PublicSubscriptionService {

    @EJB(mappedName = SubscriptionDAO.JNDI_BINDING)
    private SubscriptionDAO subscriptionDAO;


    /**
     * {@inheritDoc}
     */
    public boolean isSubscribed(SubjectEntity subject, ApplicationEntity application)
            throws SubscriptionNotFoundException {

        return null != subscriptionDAO.findSubscription(subject, application);
    }
}
