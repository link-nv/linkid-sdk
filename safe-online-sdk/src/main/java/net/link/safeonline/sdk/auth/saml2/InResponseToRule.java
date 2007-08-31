package net.link.safeonline.sdk.auth.saml2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.binding.SAMLMessageContext;
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

	private final String expectedInResponseTo;

	public InResponseToRule(String expectedInResponseTo) {
		this.expectedInResponseTo = expectedInResponseTo;
	}

	public void evaluate(MessageContext messageContext)
			throws SecurityPolicyException {
		if (!(messageContext instanceof SAMLMessageContext)) {
			LOG
					.debug("Invalid message context type, this policy rule only support SAMLMessageContext");
			return;
		}
		SAMLMessageContext<?, ?, ?> samlMsgCtx = (SAMLMessageContext<?, ?, ?>) messageContext;

		SAMLObject samlMsg = samlMsgCtx.getInboundSAMLMessage();
		if (samlMsg == null) {
			LOG.error("Message context did not contain inbound SAML message");
			throw new SecurityPolicyException(
					"Message context did not contain inbound SAML message");
		}
		if (samlMsg instanceof StatusResponseType) {
			StatusResponseType statusResponse = (StatusResponseType) samlMsg;
			String actualInResponseTo = statusResponse.getInResponseTo();
			if (!expectedInResponseTo.equals(actualInResponseTo)) {
				throw new SecurityPolicyException(
						"Response not in response to " + expectedInResponseTo
								+ " but to " + actualInResponseTo);
			}
		}
	}
}
