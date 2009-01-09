/*
 * SafeOnline project.
 *
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.service.bean;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.dao.ApplicationDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;

import org.jboss.annotation.ejb.LocalBinding;


/**
 * <h2>{@link PublicApplicationServiceBean} - Service for {@link PublicApplication}.</h2>
 * 
 * <p>
 * Provides access to attributes of the given application that are publicly available.
 * </p>
 * <p>
 * <i>Dec 18, 2007</i>
 * </p>
 * 
 * @author mbillemo
 */
@Stateless
@LocalBinding(jndiBinding = PublicApplicationService.JNDI_BINDING)
public class PublicApplicationServiceBean implements PublicApplicationService {

    @EJB(mappedName = ApplicationDAO.JNDI_BINDING)
    private ApplicationDAO applicationDAO;


    /**
     * {@inheritDoc}
     */
    @PermitAll
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public PublicApplication findPublicApplication(String applicationName) {

        ApplicationEntity application = applicationDAO.findApplication(applicationName);
        if (application == null)
            return null;

        return new PublicApplication(application);
    }
}
