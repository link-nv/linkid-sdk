/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.tools;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import net.link.safeonline.sdk.auth.filter.LoginManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import org.wicketstuff.javaee.naming.IJndiNamingStrategy;
import org.wicketstuff.javaee.naming.StandardJndiNamingStrategy;


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

    static final Log LOG = LogFactory.getLog(WicketUtil.class);


    /**
     * @return A string that is the formatted representation of the given date according to the user's locale in short
     *         form.
     */
    public static String formatDate(Session session, Date date) {

        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, session.getLocale()).format(date);
    }

    /**
     * @return A string that is the formatted representation of the given amount of currency according to the user's
     *         locale.
     */
    public static String formatCurrency(Session session, Number number) {

        return NumberFormat.getCurrencyInstance(session.getLocale()).format(number);
    }

    /**
     * Add an injector to the given Wicket web application that will resolve fields with the {@link EJB} annotation.
     * 
     * This injector assumes the field is of a type that is a bean interface with a publicly accessible BINDING constant
     * field which points to the JNDI location of the bean that needs to be injected into the field.
     */
    public static void addInjector(WebApplication application) {

        application.addComponentInstantiationListener(new JavaEEComponentInjector(application,
                new IJndiNamingStrategy() {

                    private static final long                serialVersionUID = 1L;
                    private final StandardJndiNamingStrategy defaultStrategy  = new StandardJndiNamingStrategy();


                    @SuppressWarnings("unchecked")
                    public String calculateName(String ejbName, Class ejbType) {

                        try {
                            Field bindingField = ejbType.getDeclaredField("BINDING");
                            Object binding = bindingField.get(null);

                            LOG.debug("Resolved '" + ejbName + "' type '" + ejbType.getCanonicalName() + "' to: "
                                    + binding);

                            if (binding != null)
                                return binding.toString();
                        } catch (SecurityException e) {
                            LOG.warn("No access to fields when trying to resolve '" + ejbName + "' type '"
                                    + ejbType.getCanonicalName() + "'", e);
                        } catch (NoSuchFieldException e) {
                            LOG.warn("No field called 'BINDING' when trying to resolve '" + ejbName + "' type '"
                                    + ejbType.getCanonicalName() + "'", e);
                        } catch (IllegalArgumentException e) {
                            LOG.warn("No valid instance of EJB when trying to resolve '" + ejbName + "' type '"
                                    + ejbType.getCanonicalName() + "'", e);
                        } catch (IllegalAccessException e) {
                            LOG.warn("No access to 'BINDING' when trying to resolve '" + ejbName + "' type '"
                                    + ejbType.getCanonicalName() + "'", e);
                        }

                        return this.defaultStrategy.calculateName(ejbName, ejbType);
                    }
                }));
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
