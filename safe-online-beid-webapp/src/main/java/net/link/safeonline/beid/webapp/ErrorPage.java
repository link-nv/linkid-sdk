/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.beid.webapp;

import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.BAD_JAVA_VERSION;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.BAD_PLATFORM;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.NO_JAVA;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.NO_MIDDLEWARE;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.NO_READER;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.ErrorType.PROTOCOL_VIOLATION;
import static net.link.safeonline.beid.webapp.BeIdMountPoints.MountPoint.TYPE_PARAMETER;

import javax.ejb.EJB;

import net.link.safeonline.authentication.service.SamlAuthorityService;
import net.link.safeonline.device.sdk.AuthenticationContext;
import net.link.safeonline.device.sdk.ProtocolContext;
import net.link.safeonline.model.beid.BeIdConstants;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;


/**
 * <h2>{@link ErrorPage}<br>
 * <sub>Failure during the BeID detection pipeline.</sub></h2>
 * 
 * <p>
 * This page caches any fatal errors during the BeID applet support pipeline and tries to give as useful an error message to the user as
 * possible.
 * </p>
 * 
 * <p>
 * <i>Jan 6, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class ErrorPage extends TemplatePage {

    @EJB(mappedName = SamlAuthorityService.JNDI_BINDING)
    transient SamlAuthorityService samlAuthorityService;

    AuthenticationContext          authenticationContext;
    ProtocolContext                protocolContext;


    public ErrorPage(PageParameters parameters) {

        authenticationContext = AuthenticationContext.getAuthenticationContext(WicketUtil.toServletRequest(getRequest()).getSession());
        protocolContext = ProtocolContext.getProtocolContext(WicketUtil.getHttpSession(getRequest()));

        // Header & Sidebar.
        getHeader();

        // Our content.
        String type = parameters.getString(TYPE_PARAMETER);
        String errorTitle = localize("%l", "protocolError");
        String errorMessage = localize("%l", "errorMessage");
        if (BAD_PLATFORM.getTypeValue().equalsIgnoreCase(type)) {
            errorTitle = localize("%l", "platformUnsupported");
            errorMessage = localize("%l", "platformUnsupportedInfo");
        } else if (NO_READER.getTypeValue().equalsIgnoreCase(type)) {
            errorTitle = localize("%l", "missingReader");
            errorMessage = localize("%l <a href='%s'>%s</a>", "missingReaderInfo", "http://www.belgeid.be/", "http://www.belgeid.be/");
        } else if (NO_MIDDLEWARE.getTypeValue().equalsIgnoreCase(type)) {
            errorTitle = localize("%l", "missingMiddleware");
            errorMessage = localize("%l <a href='%s'>%s</a>", "missingMiddlewareInfo", "http://www.belgium.be/zip/eid_datacapture_nl.html",
                    "http://www.belgium.be/zip/eid_datacapture_nl.html");
        } else if (BAD_JAVA_VERSION.getTypeValue().equalsIgnoreCase(type)) {
            errorTitle = localize("%l", "javaVersionError");
            errorMessage = localize("%l <a href='%s'>%s</a>", "javaVersionErrorInfo", "http://www.java.com/getjava/",
                    "http://www.java.com/getjava/");
        } else if (NO_JAVA.getTypeValue().equalsIgnoreCase(type)) {
            errorTitle = localize("%l", "javaDisabled");
            errorMessage = localize("%l", "javaDisabledInfo");
        } else if (PROTOCOL_VIOLATION.getTypeValue().equalsIgnoreCase(type)) {
            errorTitle = localize("%l", "protocolError");
            errorMessage = localize("%l", "protocolErrorInfo");
        }

        getContent().add(new Label("title", errorTitle));
        getContent().add(new Label("message", errorMessage));
        getContent().add(new Link<String>("retry") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                retry();
            }
        });
        getContent().add(new Link<String>("cancel") {

            private static final long serialVersionUID = 1L;


            @Override
            public void onClick() {

                cancel();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPageTitle() {

        return localize("error");
    }

    void retry() {

        throw new RedirectToUrlException("beid.html");
    }

    void cancel() {

        if (authenticationContext != null && authenticationContext.getApplication() != null
                && authenticationContext.getApplication().length() > 0) {
            authenticationContext.setUsedDevice(BeIdConstants.BEID_DEVICE_ID);
            authenticationContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

            throw new RedirectToUrlException("_authentication_exit");
        }

        else if (protocolContext != null && protocolContext.getSubject() != null && protocolContext.getSubject().length() > 0) {
            protocolContext.setSuccess(false);
            protocolContext.setValidity(samlAuthorityService.getAuthnAssertionValidity());

            throw new RedirectToUrlException("_device_exit");
        }

        // No valid authentication context or protocol context?
        retry();
    }
}
