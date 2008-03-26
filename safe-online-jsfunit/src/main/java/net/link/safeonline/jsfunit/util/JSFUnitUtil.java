/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.jsfunit.util;

import java.io.IOException;
import java.net.MalformedURLException;

import net.link.safeonline.jsfunit.servlet.EntryServlet;
import net.link.safeonline.sdk.auth.filter.AuthenticationFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.jsfunit.framework.WebConversationFactory;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class JSFUnitUtil {

	private static final Log LOG = LogFactory.getLog(JSFUnitUtil.class);

	private JSFUnitUtil() {
		// empty
	}

	/**
	 * Login for JSFUnit using the JSFUnit {@link EntryServlet}. The
	 * {@link AuthenticationFilter} should be set on this page to activate the
	 * jsfunit login.
	 * 
	 * @param wc
	 * @param page
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void login(WebConversation wc, String page)
			throws MalformedURLException, IOException, SAXException {
		String loginPath = WebConversationFactory.getWARURL() + "/" + page;
		WebRequest req = new GetMethodWebRequest(loginPath);
		WebResponse resp = wc.getResponse(req);

		// Explicitly post the form containing the SAML request as HttpUnit
		// offers only limited javascript support.
		LOG.debug("post SAML request form");
		WebForm form = resp.getForms()[0];
		resp = form.submit();

		// Now post the form containing the SAML response.
		LOG.debug("post SAML response form");
		form = resp.getForms()[0];
		resp = form.submit();
	}

}
