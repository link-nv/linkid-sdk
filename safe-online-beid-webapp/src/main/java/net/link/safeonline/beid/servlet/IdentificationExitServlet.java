/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;

/**
 * This servlet redirects back to the calling web application.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationExitServlet extends AbstractInjectionServlet {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(IdentificationExitServlet.class);

	@In(value = "target", scope = ScopeType.SESSION)
	private String target;

	@In(value = IdentificationDataServlet.NAME_SESSION_ATTRIBUTE, scope = ScopeType.SESSION)
	private String name;

	@Override
	protected void invoke(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		LOG.debug("target: " + this.target);
		LOG.debug("name: " + this.name);

		PrintWriter writer = response.getWriter();
		writer.println("<html>");
		{
			writer.println("<body onload=\"document.myform.submit();\">");
			{
				writer.println("<form name=\"myform\" action=\""
						+ URLEncoder.encode(this.target, "UTF-8")
						+ "\" method=\"POST\">");
				{
					writer
							.println("<input type=\"hidden\" name=\"name\" value=\""
									+ this.name + "\"/>");
				}
				writer.println("</form>");
			}
			writer.println("</body>");
		}
		writer.println("</html>");
	}
}
