/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.siemens.acceptance.ws.auth.console;

import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.ExecutionException;

import net.lin_k.safe_online.auth.DeviceAuthenticationInformationType;
import net.link.safeonline.auth.ws.Confirmation;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.auth.Attribute;
import net.link.safeonline.sdk.ws.exception.WSAuthenticationException;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
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
                        deviceCredentials, AuthenticationUtils.this.consoleManager.getPublicKey());
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        } else if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient()
                                                                                  .getDeviceAuthenticationInformation()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient()
                                                                                   .getDeviceAuthenticationInformation());
                        } else {
                            // no additional device information given but the specific device needs further authentication steps
                            notifyObservers(new DeviceAuthenticationInformationType());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void requestGlobalUsageAgreement() {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().getGlobalUsageAgreement();
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void confirmGlobalUsageAgreement(final Confirmation confirmation) {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().confirmGlobalUsageAgreement(confirmation);
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void requestUsageAgreement() {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().getUsageAgreement();
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void confirmUsageAgreement(final Confirmation confirmation) {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().confirmUsageAgreement(confirmation);
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void getIdentity() {

        SwingWorker<List<Attribute>, Object> worker = new SwingWorker<List<Attribute>, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected List<Attribute> doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().getIdentity();
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void confirmIdentity(final Confirmation confirmation) {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().confirmIdentity(confirmation);
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void getMissingAttributes() {

        SwingWorker<List<Attribute>, Object> worker = new SwingWorker<List<Attribute>, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected List<Attribute> doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().getMissingAttributes();
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

    public void saveMissingAttributes(final List<Attribute> missingAttributes) {

        SwingWorker<String, Object> worker = new SwingWorker<String, Object>() {

            @SuppressWarnings("synthetic-access")
            @Override
            protected String doInBackground()
                    throws Exception {

                return AuthenticationUtils.this.consoleManager.getAuthenticationClient().saveMissingAttributes(missingAttributes);
            }

            @SuppressWarnings("synthetic-access")
            @Override
            protected void done() {

                AuthenticationError error = null;
                setChanged();
                try {
                    if (null == get()) {
                        if (null != AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep()) {
                            notifyObservers(AuthenticationUtils.this.consoleManager.getAuthenticationClient().getAuthenticationStep());
                        }
                    } else {
                        notifyObservers(get());
                    }
                } catch (InterruptedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication interrupted ...", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof WSAuthenticationException) {
                        WSAuthenticationException authenticationException = (WSAuthenticationException) e.getCause();
                        error = new AuthenticationError(authenticationException.getErrorCode(), authenticationException.getMessage());
                        LOG.error("Authentication failed: error code = " + error.getCode().getErrorCode());
                    } else {
                        error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                        LOG.error("Authentication failed to execute ...", e);
                    }
                } catch (RequestDeniedException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                } catch (WSClientTransportException e) {
                    error = new AuthenticationError(WSAuthenticationErrorCode.REQUEST_FAILED, null);
                    LOG.error("Authentication failed to execute ...", e);
                }
                if (null != error) {
                    setChanged();
                    notifyObservers(error);
                }
            }

        };
        worker.execute();
    }

}
