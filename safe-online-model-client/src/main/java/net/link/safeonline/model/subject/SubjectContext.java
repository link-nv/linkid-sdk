/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.subject;

import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.model.SubjectManager;


/**
 * Subject Context. In a J2EE application the subject context will most likely map to an EJB session bean context. This means that this
 * subject context will use the J2EE security, transaction and naming context of the calling session bean. The easiest way to construct the
 * subject context is to let the calling session bean implement this interface.
 * 
 * @author fcorneli
 * 
 */
public interface SubjectContext {

    /**
     * Gives back a subject manager. In case of a J2EE application this subject manager will map to an EJB3 session bean subject manager.
     * 
     */
    SubjectManager getSubjectManager();

    SubscriptionDAO getSubscriptionDAO();
}
