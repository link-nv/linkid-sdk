/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.device.sdk.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.device.sdk.exception.DeviceFinalizationException;
import net.link.safeonline.device.sdk.exception.DeviceInitializationException;
import net.link.safeonline.device.sdk.saml2.DeviceOperationType;
import net.link.safeonline.device.sdk.saml2.Saml2Handler;
import net.link.safeonline.sdk.KeyStoreUtils;
import net.link.safeonline.sdk.auth.saml2.HttpServletRequestEndpointWrapper;
import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.ErrorMessage;
import net.link.safeonline.util.servlet.annotation.Context;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Landing servlet on the remote device issuer side where OLAS posts its SAML authentication request to for device registration, updating,
 * removal.
 * 
 * @author wvdhaute
 * 
 */
public class LandingServlet extends AbstractInjectionServlet {

    private static final Log  LOG              = LogFactory.getLog(LandingServlet.class);

    private static final long serialVersionUID = 1L;

    @Context(name = "KeyStoreResource", optional = true)
    private String            p12KeyStoreResourceName;

    @Context(name = "KeyStoreFile", optional = true)
    private String            p12KeyStoreFileName;

    @Context(name = "KeyStorePassword", optional = true)
    private String            keyStorePassword;

    @Context(name = "KeyStoreType", defaultValue = "pkcs12")
    private String            keyStoreType;

    @Init(name = "ServletEndpointUrl", optional = true)
    private String            servletEndpointUrl;

    @Init(name = "RegistrationUrl", optional = true)
    private String            registrationUrl;

    @Init(name = "RemovalUrl", optional = true)
    private String            removalUrl;

    @Init(name = "UpdateUrl", optional = true)
    private String            updateUrl;

    @Init(name = "DisableUrl", optional = true)
    private String            disableUrl;

    @Init(name = "ErrorPage", optional = true)
    private String            errorPage;

    private KeyPair           applicationKeyPair;

    private X509Certificate   applicationCertificate;


    @Override
    public void init(ServletConfig config)
            throws ServletException {

        super.init(config);
        InputStream keyStoreInputStream = null;
        if (null != this.p12KeyStoreResourceName) {
            Thread currentThread = Thread.currentThread();
            ClassLoader classLoader = currentThread.getContextClassLoader();
            keyStoreInputStream = classLoader.getResourceAsStream(this.p12KeyStoreResourceName);
            if (null == keyStoreInputStream)
                throw new UnavailableException("PKCS12 keystore resource not found: " + this.p12KeyStoreResourceName);
        } else if (null != this.p12KeyStoreFileName) {
            try {
                keyStoreInputStream = new FileInputStream(this.p12KeyStoreFileName);
            } catch (FileNotFoundException e) {
                throw new UnavailableException("PKCS12 keystore resource not found: " + this.p12KeyStoreFileName);
            }
        }
        if (null != keyStoreInputStream) {
            PrivateKeyEntry privateKeyEntry = KeyStoreUtils.loadPrivateKeyEntry(this.keyStoreType, keyStoreInputStream,
                    this.keyStorePassword, this.keyStorePassword);
            this.applicationKeyPair = new KeyPair(privateKeyEntry.getCertificate().getPublicKey(), privateKeyEntry.getPrivateKey());
            this.applicationCertificate = (X509Certificate) privateKeyEntry.getCertificate();
        }
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOG.debug("doPost");

        /**
         * Wrap the request to use the servlet endpoint url if defined. To prevent failure when behind a reverse proxy or loadbalancer when
         * opensaml is checking the destination field.
         */
        HttpServletRequestEndpointWrapper requestWrapper;
        if (null != this.servletEndpointUrl) {
            requestWrapper = new HttpServletRequestEndpointWrapper(request, this.servletEndpointUrl);
        } else {
            requestWrapper = new HttpServletRequestEndpointWrapper(request, request.getRequestURL().toString());
        }

        DeviceOperationType deviceOperation;
        try {
            Saml2Handler handler = Saml2Handler.getSaml2Handler(requestWrapper);
            handler.init(this.configParams, this.applicationCertificate, this.applicationKeyPair);
            deviceOperation = handler.initDeviceOperation(requestWrapper);
            if (deviceOperation.equals(DeviceOperationType.REGISTER) || deviceOperation.equals(DeviceOperationType.NEW_ACCOUNT_REGISTER)) {
                if (null == this.registrationUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(this.registrationUrl);
            } else if (deviceOperation.equals(DeviceOperationType.REMOVE)) {
                if (null == this.removalUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(this.removalUrl);
            } else if (deviceOperation.equals(DeviceOperationType.UPDATE)) {
                if (null == this.updateUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(this.updateUrl);
            } else if (deviceOperation.equals(DeviceOperationType.DISABLE)) {
                if (null == this.disableUrl) {
                    handler.abortDeviceOperation(requestWrapper, response);
                }
                response.sendRedirect(this.disableUrl);
            } else {
                handler.abortDeviceOperation(requestWrapper, response);
            }
        } catch (DeviceInitializationException e) {
            LOG.debug("device initialization exception: " + e.getMessage());
            redirectToErrorPage(requestWrapper, response, this.errorPage, null, new ErrorMessage(e.getMessage()));

            return;
        } catch (DeviceFinalizationException e) {
            LOG.debug("device finalization exception: " + e.getMessage());
            redirectToErrorPage(requestWrapper, response, this.errorPage, null, new ErrorMessage(e.getMessage()));

            return;
        }
    }
}
