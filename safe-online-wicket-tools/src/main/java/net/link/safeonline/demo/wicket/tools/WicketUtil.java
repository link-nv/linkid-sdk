/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.tools;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.spi.NamingManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.demo.wicket.javaee.DummyAnnotJavaEEInjector;
import net.link.safeonline.demo.wicket.service.OlasNamingStrategy;
import net.link.safeonline.sdk.auth.filter.LoginManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
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

    private static ConfigurableInjector injector;
    static final Log                    LOG = LogFactory.getLog(WicketUtil.class);


    /**
     * @return A string that is the formatted representation of the given date according to the user's locale in short
     *         form.
     */
    public static String format(Session session, Date date) {

        return format(session.getLocale(), date);
    }

    /**
     * @return A string that is the formatted representation of the given amount of currency according to the user's
     *         locale.
     */
    public static String format(Session session, Number number) {

        return format(session.getLocale(), number);
    }

    /**
     * @return A string that is the formatted representation of the given date according to the user's locale in short
     *         form.
     */
    public static String format(Locale locale, Date date) {

        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(date);
    }

    /**
     * @return A string that is the formatted representation of the given amount of currency according to the user's
     *         locale.
     */
    public static String format(Locale locale, Number number) {

        return NumberFormat.getCurrencyInstance(locale).format(number);
    }

    static ConfigurableInjector getInjector() {

        if (injector == null) {
            try {
                NamingManager.getInitialContext(null);
                injector = new AnnotJavaEEInjector(new OlasNamingStrategy());
            } catch (NoInitialContextException e) {
                injector = new DummyAnnotJavaEEInjector();
            } catch (NamingException e) {
                throw new EJBException(e);
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
     * @return <code>true</code> if the user is authenticated by the OLAS SDK framework.
     */
    public static boolean isAuthenticated(Request request) {

        return LoginManager.isAuthenticated(toServletRequest(request));
    }

    /**
     * @return The OLAS username that the current user has authenticated himself with.
     * 
     * @throws ServletException
     *             If the user has not yet authenticated.
     */
    public static String getUsername(Request request) throws ServletException {

        return LoginManager.getUsername(toServletRequest(request));
    }
}
