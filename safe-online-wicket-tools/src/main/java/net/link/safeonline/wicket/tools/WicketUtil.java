/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.tools;

import java.security.PrivateKey;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClient;
import net.link.safeonline.sdk.ws.idmapping.NameIdentifierMappingClientImpl;
import net.link.safeonline.wicket.javaee.DummyAnnotJavaEEInjector;
import net.link.safeonline.wicket.service.OlasNamingStrategy;
import net.link.safeonline.wicket.tools.olas.DummyAttributeClient;
import net.link.safeonline.wicket.tools.olas.DummyNameIdentifierMappingClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.wicketstuff.javaee.injection.AnnotJavaEEInjector;


/**
 * <h2>{@link WicketUtil}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Sep 17, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class WicketUtil {

    static final Log            LOG         = LogFactory.getLog(WicketUtil.class);
    static ConfigurableInjector injector;

    private static final String WS_LOCATION = "WsLocation";
    private static boolean      isUnitTest;


    /**
     * @return A formatter according to the given locale in short form.
     */
    public static DateFormat getDateFormat(Locale locale) {

        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    }

    /**
     * @return A string that is the formatted representation of the given date according to the given locale in short form.
     */
    public static String format(Locale locale, Date date) {

        return getDateFormat(locale).format(date);
    }

    /**
     * @return A formatter according to the given locale's currency.
     */
    public static NumberFormat getCurrencyFormat(Locale locale) {

        return NumberFormat.getCurrencyInstance(locale);
    }

    /**
     * @return A string that is the formatted representation of the given amount of currency according to the given locale.
     */
    public static String format(Locale locale, Number number) {

        return getCurrencyFormat(locale).format(number);
    }

    static ConfigurableInjector getInjector() {

        if (injector == null) {
            if (!isUnitTest) {
                injector = new AnnotJavaEEInjector(new OlasNamingStrategy());
            } else {
                // Inside Unit Test
                injector = new DummyAnnotJavaEEInjector();
            }
        }

        return injector;
    }

    /**
     * Add an injector to the given Wicket web application that will resolve fields with the {@link EJB} annotation.
     * 
     * @see OlasNamingStrategy
     */
    public static void addInjector(WebApplication application) {

        application.addComponentInstantiationListener(new ComponentInjector() {

            {
                InjectorHolder.setInjector(getInjector());
            }
        });
    }

    /**
     * Perform Java EE injections on the given objects.
     */
    public static void inject(Object injectee) {

        getInjector().inject(injectee);
    }

    /**
     * Get the {@link HttpServletRequest} contained in the given Wicket {@link Request}.
     */
    public static HttpServletRequest toServletRequest(Request request) {

        return ((WebRequest) request).getHttpServletRequest();
    }

    /**
     * Get the {@link HttpSession} contained in the given Wicket {@link Request}.
     */
    public static HttpSession getHttpSession(Request request) {

        return toServletRequest(request).getSession();
    }

    /**
     * @return <code>true</code> if the user is authenticated by the OLAS SDK framework.
     */
    public static boolean isAuthenticated(Request request) {

        return LoginManager.isAuthenticated(toServletRequest(request));
    }

    /**
     * @return The OLAS userId that the current user has authenticated himself with.
     * 
     * @throws ServletException
     *             If the user has not yet authenticated.
     */
    public static String getUserId(Request request)
            throws ServletException {

        return LoginManager.getUserId(toServletRequest(request));
    }

    /**
     * Telling {@link WicketUtil} that we're unit testing will make it generate dummy services to emulate OLAS services that are not
     * available in the unit testing framework.
     */
    public static void setUnitTesting(boolean unitTesting) {

        isUnitTest = unitTesting;
    }

    /**
     * Retrieve a proxy to the OLAS attribute web service.
     * 
     * @param loginRequest
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used. {@link DummyAttributeClient} is used instead,
     *            provided you called {@link #setUnitTesting(boolean)}.
     */
    public static AttributeClient getOLASAttributeService(HttpServletRequest loginRequest, PrivateKeyEntry privateKeyEntry) {

        if (!isUnitTest) {
            // Find the location of the OLAS web services to use.
            String wsLocation = loginRequest.getSession().getServletContext().getInitParameter(WS_LOCATION);

            // Find the key and certificate of the bank application.
            X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
            PrivateKey privateKey = privateKeyEntry.getPrivateKey();

            // Create the attribute service client.
            return new AttributeClientImpl(wsLocation, certificate, privateKey);
        }

        return new DummyAttributeClient();
    }

    /**
     * Retrieve a proxy to the OLAS id mapping web service.
     * 
     * @param request
     *            The request that contains a session with a servlet context that has the WsLocation init parameter set.<br>
     *            Note: This can be <code>null</code> for unit tests - it is not used. {@link DummyAttributeClient} is used instead,
     *            provided you called {@link #setUnitTesting(boolean)}.
     */
    public static NameIdentifierMappingClient getOLASIdMappingService(HttpServletRequest request, PrivateKeyEntry privateKeyEntry) {

        return getOLASIdMappingService(request, privateKeyEntry.getPrivateKey(), (X509Certificate) privateKeyEntry.getCertificate());

    }

    public static NameIdentifierMappingClient getOLASIdMappingService(HttpServletRequest request, PrivateKey privateKey,
                                                                      X509Certificate certificate) {

        if (!isUnitTest) {
            // Find the location of the OLAS web services to use.
            String wsLocation = request.getSession().getServletContext().getInitParameter(WS_LOCATION);

            // Create the id mapping service client.
            return new NameIdentifierMappingClientImpl(wsLocation, certificate, privateKey);
        }

        return new DummyNameIdentifierMappingClient();
    }
}
