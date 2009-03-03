/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.tools;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.link.safeonline.common.OlasNamingStrategy;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.wicket.service.AnnotSDKInjector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.injection.ComponentInjector;
import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
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

    static final Log            LOG = LogFactory.getLog(WicketUtil.class);
    static ConfigurableInjector eeInjector;
    static AnnotSDKInjector     sdkInjector;


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

    /**
     * Add an injector to the given Wicket web application that will resolve fields with the {@link EJB} annotation.
     * 
     * @see OlasNamingStrategy
     */
    public static void addInjector(WebApplication application) {

        application.addComponentInstantiationListener(new ComponentInjector() {

            @Override
            public void onInstantiation(Component component) {

                inject(component);
            }
        });
    }

    /**
     * Perform Java EE injections on the given objects.
     */
    public static void inject(Object injectee) {

        if (eeInjector == null) {
            eeInjector = new AnnotJavaEEInjector(new OlasNamingStrategy());
        }
        if (sdkInjector == null) {
            sdkInjector = new AnnotSDKInjector();
        }

        eeInjector.inject(injectee);
        sdkInjector.inject(injectee);
    }

    /**
     * Get the {@link HttpServletRequest} contained in the given Wicket {@link Request}.
     */
    public static HttpServletRequest toServletRequest(Request request) {

        return ((WebRequest) request).getHttpServletRequest();
    }

    /**
     * Get the {@link HttpServletResponse} contained in the given Wicket {@link Response}.
     */
    public static HttpServletResponse toServletResponse(Response response) {

        return ((WebResponse) response).getHttpServletResponse();
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
    public static boolean isOlasAuthenticated(Request request) {

        return LoginManager.isAuthenticated(toServletRequest(request));
    }

    /**
     * @return The OLAS userId that the current user has authenticated himself with or <code>null</code> if the user isn't authenticated yet
     *         (through OLAS).
     */
    public static String findOlasId(Request request) {

        return LoginManager.findUserId(toServletRequest(request));
    }
}
