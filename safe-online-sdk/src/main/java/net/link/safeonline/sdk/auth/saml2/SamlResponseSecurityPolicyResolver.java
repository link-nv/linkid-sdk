/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.util.List;

import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicy;
import org.opensaml.ws.security.SecurityPolicyResolver;
import org.opensaml.ws.security.SecurityPolicyRule;
import org.opensaml.ws.security.provider.BasicSecurityPolicy;
import org.opensaml.ws.security.provider.HTTPRule;
import org.opensaml.ws.security.provider.MandatoryIssuerRule;
import org.opensaml.xml.security.SecurityException;


public class SamlResponseSecurityPolicyResolver implements SecurityPolicyResolver {

    public Iterable<SecurityPolicy> resolve(MessageContext messageContext) throws SecurityException {

        return null;
    }

    public SecurityPolicy resolveSingle(MessageContext messageContext) throws SecurityException {

        SecurityPolicy securityPolicy = new BasicSecurityPolicy();
        List<SecurityPolicyRule> securityPolicyRules = securityPolicy.getPolicyRules();
        securityPolicyRules.add(new HTTPRule(null, "POST", false));
        securityPolicyRules.add(new MandatoryIssuerRule());
        return securityPolicy;
    }
}
