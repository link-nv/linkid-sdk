/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.ws.common.WSAuthenticationErrorCode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingworker.SwingWorker;


/**
 * SafeOnline services util class
 * 
 * <p>
 * Used to access to the attribute and data SafeOnline web services
 * </p>
 * 
 * @author wvdhaute
 * 
 */
public class AuthenticationUtils extends Observable {

    static final Log                   LOG                   = LogFactory.getLog(AuthenticationUtils.class);

    private static AuthenticationUtils servicesUtilsInstance = null;

    private AcceptanceConsoleManager   consoleManager        = AcceptanceConsoleManager.getInstance();


    private AuthenticationUtils() {

    }

    public static AuthenticationUtils getInstance() {

        if (null == servicesUtilsInstance) {
            servicesUtilsInstance = new AuthenticationUtils();
        }
        return servicesUtilsInstance;
    }

    public void authenticate(final String deviceName, final Object deviceCredentials) {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().authenticate(
                        AuthenticationUtils.this.consoleManager.getApplication(), deviceName, Locale.ENGLISH.getLanguage(),
                        deviceCredentials, null);
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                WSAuthenticationErrorCode errorCode = null;
                setChanged();
                try {
                    notifyObservers(get());
                } catch (InterruptedException e) {
                    errorCode = WSAuthenticationErrorCode.REQUEST_FAILED;
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        errorCode = ((WSAuthenticationException) e.getCause()).getErrorCode();
                        LOG.error("Authentication failed: error code = " + errorCode.getErrorCode());
                    } else {
                        errorCode = WSAuthenticationErrorCode.REQUEST_FAILED;
                        LOG.error("Authentication failed to execute ...", e);
                    }
                }
                if (null != errorCode) {
                    setChanged();
                    notifyObservers(errorCode);
                }
            }

        };
        worker.execute();
    }
}
