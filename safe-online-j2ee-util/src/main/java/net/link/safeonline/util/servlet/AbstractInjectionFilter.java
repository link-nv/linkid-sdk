/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.servlet;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.util.servlet.annotation.Context;
import net.link.safeonline.util.servlet.annotation.Init;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstract Injection Filter.
 * 
 * <ul>
 * <li>Injects EJBs.
 * <li>Injects filter init parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>Injects filter context parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * </ul>
 * 
 * @author wvdhaute
 * 
 */
public abstract class AbstractInjectionFilter implements Filter {

    private static final Log      LOG              = LogFactory.getLog(AbstractInjectionFilter.class);

    private static final long     serialVersionUID = 1L;

    protected Map<String, String> configParams;


    public void init(FilterConfig config)
            throws ServletException {

        initInitParameters(config);
        initContextParameters(config);
        injectEjbs();
    }

    @SuppressWarnings("unchecked")
    private void injectEjbs()
            throws ServletException {

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            EJB ejb = field.getAnnotation(EJB.class);
            if (null == ejb) {
                continue;
            }
            String mappedName = ejb.mappedName();
            if (null == mappedName)
                throw new ServletException("@EJB mappedName attribute required");
            LOG.debug("injecting: " + mappedName);
            Class type = field.getType();
            if (false == type.isInterface())
                throw new ServletException("field is not an interface type");
            Object ejbRef = EjbUtils.getEJB(mappedName, type);
            field.setAccessible(true);
            try {
                field.set(this, ejbRef);
            } catch (IllegalArgumentException e) {
                throw new ServletException("illegal argument: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new ServletException("illegal access: " + e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initInitParameters(FilterConfig config)
            throws ServletException {

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Init initAnnotation = field.getAnnotation(Init.class);
            if (null == initAnnotation) {
                continue;
            }
            String name = initAnnotation.name();
            if (null == name)
                throw new ServletException("@Init name attribute required");
            LOG.debug("init: " + name);
            String defaultValue = initAnnotation.defaultValue();
            boolean optional = initAnnotation.optional();
            boolean checkContext = initAnnotation.checkContext();

            String value = config.getInitParameter(name);
            if (null == value && checkContext) {
                value = config.getServletContext().getInitParameter(name);
            }
            if (null == value) {
                if (Init.NOT_SPECIFIED.equals(defaultValue) && !optional)
                    throw new UnavailableException("missing init parameter: " + name);
                if (Init.NOT_SPECIFIED.equals(defaultValue)) {
                    defaultValue = null;
                }
                value = defaultValue;
            }
            field.setAccessible(true);
            try {
                field.set(this, value);
            } catch (IllegalArgumentException e) {
                throw new ServletException("illegal argument: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new ServletException("illegal access: " + e.getMessage(), e);
            }
        }
        configParams = new HashMap<String, String>();
        Enumeration<String> initParamsEnum = config.getInitParameterNames();
        while (initParamsEnum.hasMoreElements()) {
            String paramName = initParamsEnum.nextElement();
            String paramValue = config.getInitParameter(paramName);
            LOG.debug("config param: " + paramName + "=" + paramValue);
            configParams.put(paramName, paramValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void initContextParameters(FilterConfig config)
            throws ServletException {

        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Context contextAnnotation = field.getAnnotation(Context.class);
            if (null == contextAnnotation) {
                continue;
            }
            String name = contextAnnotation.name();
            if (null == name)
                throw new ServletException("@Context name attribute required");
            LOG.debug("init: " + name);
            String defaultValue = contextAnnotation.defaultValue();
            boolean optional = contextAnnotation.optional();

            String value = config.getServletContext().getInitParameter(name);
            if (null == value) {
                if (Context.NOT_SPECIFIED.equals(defaultValue) && !optional)
                    throw new UnavailableException("missing init parameter: " + name);
                if (Context.NOT_SPECIFIED.equals(defaultValue)) {
                    defaultValue = null;
                }
                value = defaultValue;
            }
            field.setAccessible(true);
            try {
                field.set(this, value);
            } catch (IllegalArgumentException e) {
                throw new ServletException("illegal argument: " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new ServletException("illegal access: " + e.getMessage(), e);
            }
        }
        if (null == configParams) {
            configParams = new HashMap<String, String>();
        }
        Enumeration<String> initParamsEnum = config.getServletContext().getInitParameterNames();
        while (initParamsEnum.hasMoreElements()) {
            String paramName = initParamsEnum.nextElement();
            if (null == configParams.get(paramName)) {
                String paramValue = config.getServletContext().getInitParameter(paramName);
                LOG.debug("config param: " + paramName + "=" + paramValue);
                configParams.put(paramName, paramValue);
            }
        }
    }

    protected static void addCookie(String name, String value, String path, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        if (true == hasCookie(name, httpRequest))
            return;
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        LOG.debug("adding cookie: " + name + "=" + value + " path=" + cookie.getPath());
        httpResponse.addCookie(cookie);
    }

    protected static void setCookie(String name, String value, String path, HttpServletResponse httpResponse) {

        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        LOG.debug("setting cookie: " + name + "=" + value);
        httpResponse.addCookie(cookie);
    }

    protected static void removeCookie(String name, String path, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        if (false == hasCookie(name, httpRequest))
            return;
        LOG.debug("removing cookie: " + name);
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(path);
        cookie.setMaxAge(0);
        httpResponse.addCookie(cookie);

    }

    protected static boolean hasCookie(String name, HttpServletRequest httpRequest) {

        Cookie[] cookies = httpRequest.getCookies();
        if (null == cookies)
            return false;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()))
                return true;
        }
        return false;
    }

    protected static String findCookieValue(String name, HttpServletRequest httpRequest) {

        Cookie[] cookies = httpRequest.getCookies();
        if (null == cookies)
            return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName()))
                return cookie.getValue();
        }
        return null;
    }

}
