/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.protocol;

import javax.ejb.NoSuchEJBException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.service.LogoutService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This HTTP session listener manages the life-cycle of the logout service instance used by the authentication web application.
 * 
 * @author wvdhaute
 * 
 */
public class LogoutServiceManager implements HttpSessionListener {

    public static final String LOGOUT_SERVICE_ATTRIBUTE = "logoutService";

    private static final Log   LOG                      = LogFactory.getLog(LogoutServiceManager.class);


    public void sessionCreated(HttpSessionEvent event) {

    }

    public static void bindLogoutService(HttpSession session) {

        LogoutService logoutService = EjbUtils.getEJB(LogoutService.JNDI_BINDING, LogoutService.class);
        session.setAttribute(LOGOUT_SERVICE_ATTRIBUTE, logoutService);
    }

    public void sessionDestroyed(HttpSessionEvent event) {

        HttpSession session = event.getSession();

        LogoutService logoutService = (LogoutService) session.getAttribute(LOGOUT_SERVICE_ATTRIBUTE);
        if (null == logoutService)
            /*
             * This is the normal thing to happen. This means that the logout service was already properly terminated.
             */
            return;

        /*
         * Make sure we do a proper cleanup of the logout service instance. This can happen in the event of an unusual, well, event.
         */
        try {
            LOG.debug("aborting logout service instance");
            logoutService.abort();
            /*
             * By doing so we actually inform the EJB container that he can release some resources held by the logout service bean instance.
             */
        } catch (NoSuchEJBException e) {
            /*
             * This means that the logout service instances did throw a system exception, which has put it in the non-existing state, or
             * that someone already invoked a remove method on the bean and forgot to remove the bean reference from the HTTP session.
             */
            LOG.warn("no such EJB exception received");
        }
    }

    /**
     * Gives back the logout service instance associated with the given HTTP session. Later on we could limit the usage of this method to
     * certain states on the logout service. It is clear that this method should not be used to finalize the logout service via
     * {@link LogoutService#finalizeLogout(boolean)} or {@link LogoutService#abort()}. These operations should be performed via this logout
     * service manager class.
     * 
     * @param session
     */
    public static LogoutService getLogoutService(HttpSession session) {

        LogoutService logoutService = (LogoutService) session.getAttribute(LOGOUT_SERVICE_ATTRIBUTE);
        if (null == logoutService)
            throw new IllegalStateException("logout service instance not present");
        return logoutService;
    }

    /**
     * Finalizes the logout process.
     * 
     * This method will return an encoded SAML logout response token which should be communicated to the application the user is logging out
     * for.
     * 
     * @param partialLogout
     * @param session
     * @throws NodeNotFoundException
     */
    public static String finalizeLogout(boolean partialLogout, HttpSession session)
            throws NodeNotFoundException {

        LogoutService logoutService = (LogoutService) session.getAttribute(LOGOUT_SERVICE_ATTRIBUTE);
        if (null == logoutService)
            throw new IllegalStateException("logout service instance not present");
        try {
            return logoutService.finalizeLogout(partialLogout);
        } finally {
            /*
             * No matter what happens, we don't want the sessionDestroyed method to call abort on our finished logout service instance.
             */
            session.removeAttribute(LOGOUT_SERVICE_ATTRIBUTE);
        }
    }

    /**
     * Aborts the logout process.
     * 
     * @param session
     */
    public static void abort(HttpSession session) {

        LogoutService logoutService = (LogoutService) session.getAttribute(LOGOUT_SERVICE_ATTRIBUTE);
        if (null == logoutService)
            throw new IllegalStateException("logout service instance not present");
        try {
            logoutService.abort();
        } finally {
            session.removeAttribute(LOGOUT_SERVICE_ATTRIBUTE);
        }
    }
}
