/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.application;

import net.link.safeonline.dao.ApplicationDAO;


/**
 * Application Context. In a J2EE application a domain object context will most likely map to an EJB session bean
 * context. This means that this application context will use the J2EE security, transaction, persistence and naming
 * context of the calling session bean. The easiest way to construct the application context is to let the calling
 * session bean implement this interface.
 *
 * @author fcorneli
 *
 */
public interface ApplicationContext {

    ApplicationDAO getApplicationDAO();
}
