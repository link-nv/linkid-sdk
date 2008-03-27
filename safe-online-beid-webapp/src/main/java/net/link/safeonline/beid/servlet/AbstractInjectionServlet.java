/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.RequestParameter;

/**
 * Abstract Injection Servlet. Injects request parameters into servlet fields.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractInjectionServlet extends HttpServlet {

	private static final Log LOG = LogFactory
			.getLog(AbstractInjectionServlet.class);

	private static final long serialVersionUID = 1L;

	@Override
	protected final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doInvocation(request, response);
	}

	@Override
	protected final void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doInvocation(request, response);
	}

	private void doInvocation(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		injectRequestParameters(request);
		injectSessionAttributes(request, session);
		InjectionResponseWrapper responseWrapper = new InjectionResponseWrapper(
				response);
		invoke(request, responseWrapper);
		outjectSessionAttributes(request, session);
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

	protected abstract void invoke(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException;

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

	private void injectSessionAttributes(HttpServletRequest request,
			HttpSession session) throws ServletException {
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

	private void outjectSessionAttributes(HttpServletRequest request,
			HttpSession session) throws ServletException {
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
