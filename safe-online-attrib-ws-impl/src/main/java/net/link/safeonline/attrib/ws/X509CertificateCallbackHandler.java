/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.attrib.ws;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.jboss.security.auth.callback.ObjectCallback;

public class X509CertificateCallbackHandler implements CallbackHandler {

	private final X509Certificate certificate;

	public X509CertificateCallbackHandler(X509Certificate certificate) {
		this.certificate = certificate;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			if (callback instanceof ObjectCallback) {
				ObjectCallback objectCallback = (ObjectCallback) callback;
				objectCallback.setCredential(this.certificate);
			}
		}
	}
}
