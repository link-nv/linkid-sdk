/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.servlet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import net.link.safeonline.annotation.Context;
import net.link.safeonline.annotation.Init;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.web.RequestParameter;

/**
 * Abstract Injection Servlet.
 * 
 * <ul>
 * <li>Injects request parameters into servlet fields.
 * <li>Injects and outjects session parameters.
 * <li>Injects EJBs.
 * <li>Injects servlet init parameters. If no defaultValue is specified, an
 * {@link UnavailableException} will be thrown.
 * <li>Injects servlet context parameters. If no defaultValue is specified, an
 * {@link UnavailableException} will be thrown.
 * </ul>
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractInjectionServlet extends HttpServlet {

	private static final Log LOG = LogFactory
			.getLog(AbstractInjectionServlet.class);

	private static final long serialVersionUID = 1L;

	protected Map<String, String> configParams;

	@Override
	public void init(ServletConfig config) throws ServletException {
		initInitParameters(config);
		initContextParameters(config);
		injectEjbs();
	}

	@Override
	protected final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGetInvocation(request, response);
	}

	@Override
	protected final void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPostInvocation(request, response);
	}

	private void doGetInvocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		injectRequestParameters(request);
		injectSessionAttributes(session);
		InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper(
				response);
		invokeGet(request, responseWrapper);
		outjectSessionAttributes(session);
		responseWrapper.commit();
	}

	private void doPostInvocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		injectRequestParameters(request);
		injectSessionAttributes(session);
		InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper(
				response);
		invokePost(request, responseWrapper);
		outjectSessionAttributes(session);
		responseWrapper.commit();
	}

	/**
	 * Injection response wrapper. We use a response wrapper since we want to be
	 * able to postpone some actions.
	 * 
	 * @author fcorneli
	 * 
	 */
	public static class InjectionResponseWrapper extends
			HttpServletResponseWrapper {

		private String redirectLocation;

		public InjectionResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void sendRedirect(String location) throws IOException {
			if (null != this.redirectLocation) {
				throw new IllegalStateException("cannot send redirect twice");
			}
			this.redirectLocation = location;
		}

		public void commit() throws IOException {
			if (null != this.redirectLocation) {
				super.sendRedirect(this.redirectLocation);
			}
		}
	}

	protected void invokeGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		super.doGet(request, response);
	}

	protected void invokePost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		super.doPost(request, response);
	}

	private void injectRequestParameters(HttpServletRequest request)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			RequestParameter requestParameterAnnotation = field
					.getAnnotation(RequestParameter.class);
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
				throw new ServletException("illegal argument: "
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new ServletException("illegal access: " + e.getMessage(),
						e);
			}
		}
	}

	private void injectSessionAttributes(HttpSession session)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			In inAnnotation = field.getAnnotation(In.class);
			if (null == inAnnotation) {
				continue;
			}
			String inName = inAnnotation.value();
			if (false == ScopeType.SESSION.equals(inAnnotation.scope())) {
				throw new ServletException("only supporting SESSION scope");
			}
			Object value = session.getAttribute(inName);
			if (inAnnotation.required()) {
				if (null == value) {
					throw new ServletException(
							"missing required session attribute: " + inName);
				}
			}
			field.setAccessible(true);
			try {
				field.set(this, value);
			} catch (IllegalArgumentException e) {
				throw new ServletException("illegal argument: "
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new ServletException("illegal access: " + e.getMessage(),
						e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void injectEjbs() throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			EJB ejb = field.getAnnotation(EJB.class);
			if (null == ejb) {
				continue;
			}
			String mappedName = ejb.mappedName();
			if (null == mappedName) {
				throw new ServletException("@EJB mappedName attribute required");
			}
			LOG.debug("injecting: " + mappedName);
			Class type = field.getType();
			if (false == type.isInterface()) {
				throw new ServletException("field is not an interface type");
			}
			Object ejbRef = EjbUtils.getEJB(mappedName, type);
			field.setAccessible(true);
			try {
				field.set(this, ejbRef);
			} catch (IllegalArgumentException e) {
				throw new EJBException("illegal argument: " + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new EJBException("illegal access: " + e.getMessage(), e);
			}
		}
	}

	private void initInitParameters(ServletConfig config)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Init initAnnotation = field.getAnnotation(Init.class);
			if (null == initAnnotation) {
				continue;
			}
			String name = initAnnotation.name();
			if (null == name) {
				throw new ServletException("@Init name attribute required");
			}
			LOG.debug("init: " + name);
			String defaultValue = initAnnotation.defaultValue();

			String value = config.getInitParameter(name);
			if (null == value) {
				if (Init.NOT_SPECIFIED == defaultValue) {
					throw new UnavailableException("missing init parameter: "
							+ name);
				}
				value = defaultValue;
			}
			field.setAccessible(true);
			try {
				field.set(this, value);
			} catch (IllegalArgumentException e) {
				throw new EJBException("illegal argument: " + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new EJBException("illegal access: " + e.getMessage(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initContextParameters(ServletConfig config)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Context contextAnnotation = field.getAnnotation(Context.class);
			if (null == contextAnnotation) {
				continue;
			}
			String name = contextAnnotation.name();
			if (null == name) {
				throw new ServletException("@Context name attribute required");
			}
			LOG.debug("init: " + name);
			String defaultValue = contextAnnotation.defaultValue();

			String value = config.getServletContext().getInitParameter(name);
			if (null == value) {
				if (Context.NOT_SPECIFIED == defaultValue) {
					throw new UnavailableException("missing init parameter: "
							+ name);
				}
				value = defaultValue;
			}
			field.setAccessible(true);
			try {
				field.set(this, value);
			} catch (IllegalArgumentException e) {
				throw new EJBException("illegal argument: " + e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new EJBException("illegal access: " + e.getMessage(), e);
			}
		}
		this.configParams = new HashMap<String, String>();
		Enumeration<String> initParamsEnum = config.getServletContext()
				.getInitParameterNames();
		while (initParamsEnum.hasMoreElements()) {
			String paramName = initParamsEnum.nextElement();
			String paramValue = config.getServletContext().getInitParameter(
					paramName);
			this.configParams.put(paramName, paramValue);
		}
	}

	private void outjectSessionAttributes(HttpSession session)
			throws ServletException {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Out outAnnotation = field.getAnnotation(Out.class);
			if (null == outAnnotation) {
				continue;
			}
			String outName = outAnnotation.value();
			if (false == ScopeType.SESSION.equals(outAnnotation.scope())) {
				throw new ServletException("only supporting SESSION scope");
			}
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(this);
			} catch (IllegalArgumentException e) {
				throw new ServletException("illegal argument: "
						+ e.getMessage(), e);
			} catch (IllegalAccessException e) {
				throw new ServletException("illegal access: " + e.getMessage(),
						e);
			}
			LOG.debug("outjecting to session attribute: " + outName);
			session.setAttribute(outName, value);
		}
	}
}
