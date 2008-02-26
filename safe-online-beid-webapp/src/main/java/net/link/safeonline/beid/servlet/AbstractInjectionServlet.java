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
import javax.servlet.http.HttpSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.RequestParameter;

/**
 * Abstract Injection Servlet. Injects request parameters into servlet fields.
 * 
 * @author fcorneli
 * 
 */
public abstract class AbstractInjectionServlet extends HttpServlet {

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
		injectRequestParameters(request);
		invoke(request, response);
		outjectSessionAttributes(request);
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

	private void outjectSessionAttributes(HttpServletRequest request)
			throws ServletException {
		HttpSession session = request.getSession();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Out outAnnotation = field.getAnnotation(Out.class);
			if (null == outAnnotation) {
				continue;
			}
			String outName = outAnnotation.value();
			if (false == ScopeType.SESSION.equals(outAnnotation.scope())) {
				throw new ServletException("only supportign SESSION scope");
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
			session.setAttribute(outName, value);
		}
	}
}
