/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.BasicSAMLMessageContext;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyRule;

public class AudienceRestrictionRule implements SecurityPolicyRule {

	private static final Log LOG = LogFactory
			.getLog(AudienceRestrictionRule.class);

	public void evaluate(MessageContext messageContext)
			throws SecurityPolicyException {
		if (false == messageContext instanceof BasicSAMLMessageContext<?, ?, ?>) {
			throw new SecurityPolicyException("invalid message context type");
		}
		SamlResponseMessageContext samlMessageContext = (SamlResponseMessageContext) messageContext;

		SAMLObject samlMessage = samlMessageContext.getInboundSAMLMessage();
		if (samlMessage == null) {
			throw new SecurityPolicyException(
					"Message context did not contain inbound SAML message");
		}

		if (false == samlMessage instanceof Response) {
			throw new SecurityPolicyException(
					"SAML message not an response message");
		}
		Response response = (Response) samlMessage;

		List<Assertion> assertions = response.getAssertions();
		if (assertions.isEmpty()) {
			throw new SecurityPolicyException(
					"no SAML assertions in response message");
		}
		Assertion assertion = assertions.get(0);

		Conditions conditions = assertion.getConditions();
		List<AudienceRestriction> audienceRestrictions = conditions
				.getAudienceRestrictions();
		if (audienceRestrictions.isEmpty()) {
			throw new SecurityPolicyException(
					"no Audience Restrictions found in response assertion");
		}

		AudienceRestriction audienceRestriction = audienceRestrictions.get(0);
		List<Audience> audiences = audienceRestriction.getAudiences();
		if (audiences.isEmpty()) {
			throw new SecurityPolicyException(
					"no Audiences found in AudienceRestriction");
		}
		Audience audience = audiences.get(0);

		String actualApplicationName = audience.getAudienceURI();
		LOG.debug("actual application name: " + actualApplicationName);
		String expectedApplicationName = samlMessageContext
				.getExpectedApplicationName();
		if (false == expectedApplicationName.equals(actualApplicationName)) {
			throw new SecurityPolicyException("application name not correct");
		}
	}
}
