/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.saml2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.core.StatusResponseType;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.SecurityPolicyRule;

/**
 * Security policy rule that checks whether the response is indeed a response to
 * a previous request.
 * 
 * <p>
 * Remark from Chad La Joie: The security policy rules are meant to be stateless
 * so that they can be used over many messages. Storing the InResponseTo in the
 * rule represents state and wouldn't allow you to use the rule across messages
 * (as the message ID you're responding to would change).
 * </p>
 * 
 * @author fcorneli
 * 
 */
public class InResponseToRule implements SecurityPolicyRule {

	private static Log LOG = LogFactory.getLog(InResponseToRule.class);

	public void evaluate(MessageContext messageContext)
			throws SecurityPolicyException {
		if (!(messageContext instanceof SamlResponseMessageContext)) {
			LOG
					.debug("Invalid message context type, this policy rule only support SamlResponseMessageContext");
			return;
		}
		SamlResponseMessageContext samlMsgCtx = (SamlResponseMessageContext) messageContext;

		SAMLObject samlMsg = samlMsgCtx.getInboundSAMLMessage();
		if (samlMsg == null) {
			LOG.error("Message context did not contain inbound SAML message");
			throw new SecurityPolicyException(
					"Message context did not contain inbound SAML message");
		}
		if (samlMsg instanceof StatusResponseType) {
			StatusResponseType statusResponse = (StatusResponseType) samlMsg;
			String actualInResponseTo = statusResponse.getInResponseTo();
			if (!samlMsgCtx.getExpectedInResponseTo()
					.equals(actualInResponseTo)) {
				throw new SecurityPolicyException(
						"Response not in response to "
								+ samlMsgCtx.getExpectedInResponseTo()
								+ " but to " + actualInResponseTo);
			}
		}
	}
}
