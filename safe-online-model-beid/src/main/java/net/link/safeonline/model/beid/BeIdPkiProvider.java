/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.beid;

import java.security.cert.X509Certificate;

import javax.ejb.Stateless;
import javax.security.auth.x500.X500Principal;

import net.link.safeonline.model.PkiProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;

@Stateless
@LocalBinding(jndiBinding = PkiProvider.PKI_PROVIDER_JNDI + "/beid")
public class BeIdPkiProvider implements PkiProvider {

	public static final String TRUST_DOMAIN_NAME = "beid";

	private static final Log LOG = LogFactory.getLog(BeIdPkiProvider.class);

	public boolean accept(X509Certificate certificate) {
		X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
		String subject = subjectPrincipal.toString();
		LOG.debug("subject: " + subject);
		if (subject.indexOf("SERIALNUMBER") == -1) {
			return false;
		}
		if (subject.indexOf("GIVENNAME") == -1) {
			return false;
		}
		if (subject.indexOf("SURNAME") == -1) {
			return false;
		}
		return true;
	}

	public String getTrustDomainName() {
		return TRUST_DOMAIN_NAME;
	}
}
