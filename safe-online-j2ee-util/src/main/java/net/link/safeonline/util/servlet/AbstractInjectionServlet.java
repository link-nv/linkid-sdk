/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import net.link.safeonline.util.ee.EjbUtils;
import net.link.safeonline.util.servlet.annotation.Context;
import net.link.safeonline.util.servlet.annotation.In;
import net.link.safeonline.util.servlet.annotation.Init;
import net.link.safeonline.util.servlet.annotation.Out;
import net.link.safeonline.util.servlet.annotation.RequestParameter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Abstract Injection Servlet.
 * 
 * <ul>
 * <li>Injects request parameters into servlet fields.
 * <li>Injects and outjects session parameters.
 * <li>Injects EJBs.
 * <li>Injects servlet init parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>Injects servlet context parameters. If no defaultValue is specified, an {@link UnavailableException} will be thrown.
 * <li>By default checks if the servlet is accessed with a secure connection. If context parameter <code>Protocol</code> is
 * <code>http</code> or <code>securityCheck</code> is set to <code>false</code> this check will be ommitted.
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractInjectionServlet extends HttpServlet {

    private static final Log      LOG              = LogFactory.getLog(AbstractInjectionServlet.class);

    private static final long     serialVersionUID = 1L;

    protected Map<String, String> configParams;


    @Override
    public void init(ServletConfig config)
            throws ServletException {

        super.init(config);

        initInitParameters(config);
        initContextParameters(config);
        injectEjbs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SafeOnlineConfig safeOnlineConfig = SafeOnlineConfig.load(request);

        /*
         * When we're behind a proxy or load balancer, the servlet request URI here points to this machine rather than the server that the
         * request was actually sent to. This causes validation issues in OpenSAML and problems when redirecting to relative URIs.
         * 
         * To solve this problem, we wrap the servlet request and response such that the request URI in the HttpServletRequest is the
         * request URI of the client's request (the request to the proxy/load balancer), and such that sendRedirects with relative URIs are
         * translated to absolute URIs using the client's request URI base.
         */
        String endpoint = safeOnlineConfig.endpointFor(request);
        if (endpoint != null) {
            HttpServletRequestEndpointWrapper wrappedRequest = new HttpServletRequestEndpointWrapper(request, endpoint);
            HttpServletResponseEndpointWrapper wrappedResponse = new HttpServletResponseEndpointWrapper(response, endpoint);

            LOG.debug("Wrapped request and response using endpoint: " + endpoint);
            super.service(wrappedRequest, wrappedResponse);
        }

        else {
            LOG.debug("No endpoint defined.  Not wrapping request and response.");
            super.service(request, response);
        }
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doGetInvocation(request, response);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        doPostInvocation(request, response);
    }

    private void doGetInvocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        injectRequestParameters(request);
        injectSessionAttributes(session);
        InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper(response);
        invokeGet(request, responseWrapper);
        outjectSessionAttributes(session);
        responseWrapper.commit();
    }

    private void doPostInvocation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        injectRequestParameters(request);
        injectSessionAttributes(session);
        InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper(response);
        invokePost(request, responseWrapper);
        outjectSessionAttributes(session);
        responseWrapper.commit();
    }

    /**
     * Invalidate the old session, start a new one, and repeat the injection process.
     * 
     * @return The new session (Also available via {@link HttpServletRequest#getSession(boolean)}, of course).
     */
    protected HttpSession restartSession(HttpServletRequest request)
            throws ServletException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        session = request.getSession(true);

        injectRequestParameters(request);
        injectSessionAttributes(session);

        return session;
    }


    /**
     * Injection response wrapper. We use a response wrapper since we want to be able to postpone some actions.
     * 
     * @author fcorneli
     * 
     */
    public static class InjectionResponseWrapper extends HttpServletResponseWrapper {

        private String redirectLocation;


        public InjectionResponseWrapper(HttpServletResponse response) {

            super(response);
        }

        @Override
        public void sendRedirect(String location)
                throws IOException {

            if (null != redirectLocation)
                throw new IllegalStateException("cannot send redirect twice");

            redirectLocation = location;
        }

        public void commit()
                throws IOException {

            if (null != redirectLocation) {
                super.sendRedirect(redirectLocation);
            }
        }
    }


    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        super.doGet(request, response);
    }

    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        super.doPost(request, response);
    }

    private void injectRequestParameters(HttpServletRequest request)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                RequestParameter requestParameterAnnotation = field.getAnnotation(RequestParameter.class);
                if (null == requestParameterAnnotation) {
                    continue;
                }
                String requestParameterName = requestParameterAnnotation.value();
                String value = request.getParameter(requestParameterName);
                if (null == value) {
                    continue;
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
        }
    }

    private void injectSessionAttributes(HttpSession session)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                In inAnnotation = field.getAnnotation(In.class);
                if (null == inAnnotation) {
                    continue;
                }
                String inName = inAnnotation.value();
                Object value = session.getAttribute(inName);
                if (inAnnotation.required()) {
                    if (null == value)
                        throw new ServletException("missing required session attribute: " + inName);
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
        }
    }

    @SuppressWarnings("unchecked")
    private void injectEjbs()
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                EJB ejb = field.getAnnotation(EJB.class);
                if (null == ejb) {
                    continue;
                }
                String mappedName = ejb.mappedName();
                if (null == mappedName)
                    throw new ServletException(String.format("field %s.%s's @EJB requires mappedName attribute", getClass(), field));
                LOG.debug("injecting: " + mappedName);

                Class fieldType = field.getType();
                if (false == fieldType.isInterface())
                    throw new ServletException(String.format("field %s.%s's type should be an interface", getClass(), field));
                try {
                    Object ejbRef = EjbUtils.getEJB(mappedName, fieldType);
                    field.setAccessible(true);
                    try {
                        field.set(this, ejbRef);
                    } catch (IllegalArgumentException e) {
                        throw new ServletException(String.format("while injecting into %s:", getClass()), e);
                    } catch (IllegalAccessException e) {
                        throw new ServletException(String.format("while injecting into %s:", getClass()), e);
                    }
                } catch (RuntimeException e) {
                    throw new ServletException(String.format("couldn't resolve EJB named: %s (while injecting into %s)", mappedName,
                            getClass()), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void initInitParameters(ServletConfig config)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
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
    }

    @SuppressWarnings("unchecked")
    private void initContextParameters(ServletConfig config)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
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
    }

    private void outjectSessionAttributes(HttpSession session)
            throws ServletException {

        for (Class<?> type = getClass(); type != Object.class; type = type.getSuperclass()) {
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                Out outAnnotation = field.getAnnotation(Out.class);
                if (null == outAnnotation) {
                    continue;
                }
                String outName = outAnnotation.value();
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(this);
                    if (value == null && outAnnotation.required())
                        throw new ServletException("missing required session attribute: " + outName);
                } catch (IllegalArgumentException e) {
                    throw new ServletException("illegal argument: " + e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new ServletException("illegal access: " + e.getMessage(), e);
                }
                LOG.debug("outjecting to session attribute: " + outName);
                session.setAttribute(outName, value);
            }
        }
    }

    /**
     * Redirects to the specified error page. The errorMessages entries contain as key the name of the error message attribute that will be
     * pushed on the session. The attribute value will be looked up if a resource bundle is specified, else directly pushed onto the
     * session.
     * 
     * @param request
     * @param response
     * @param errorPage
     * @param resourceBundleName
     * @param errorMessages
     * @throws IOException
     */
    public void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String errorPage, String resourceBundleName,
                                    ErrorMessage... errorMessages)
            throws IOException {

        HttpSession session = request.getSession();
        ResourceBundle resourceBundle = null;
        if (null != resourceBundleName) {
            Locale locale = request.getLocale();
            try {
                resourceBundle = ResourceBundle.getBundle(resourceBundleName, locale, Thread.currentThread().getContextClassLoader());
            } catch (MissingResourceException e) {
                resourceBundle = null;
            }
        }
        for (ErrorMessage errorMessage : errorMessages) {
            if (null != resourceBundle) {
                try {
                    errorMessage.setMessage(resourceBundle.getString(errorMessage.getMessage()));
                } catch (MissingResourceException e) {
                    // not found
                }
            }
        }
        if (null == errorPage) {
            /*
             * If no error page specified, spit out a basic HTML page containing the error message.
             */
            writeBasicErrorPage(response, errorMessages);
        } else {
            for (ErrorMessage errorMessage : errorMessages) {
                session.setAttribute(errorMessage.getName(), errorMessage.getMessage());
            }
            response.sendRedirect(errorPage);
        }
    }

    private void writeBasicErrorPage(HttpServletResponse response, ErrorMessage... errorMessages)
            throws IOException {

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        out.println("<html>");
        {
            out.println("<head><title>Error</title></head>");
            out.println("<body>");
            {
                out.println("<h1>Error(s)</h1>");
                out.println("<p>");
                {
                    for (ErrorMessage errorMessage : errorMessages) {
                        out.println(errorMessage.getMessage() + "</br>");
                    }
                }
                out.println("</p>");
            }
            out.println("</body>");
        }
        out.println("</html>");
        out.close();
    }
}
