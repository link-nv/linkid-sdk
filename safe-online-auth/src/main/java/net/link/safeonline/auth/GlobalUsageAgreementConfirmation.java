package net.link.safeonline.auth;

import javax.ejb.Local;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;


@Local
public interface GlobalUsageAgreementConfirmation {

    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "GlobalUsageAgreementConfirmationBean/local";


    /*
     * Actions.
     */
    String confirm()
            throws ApplicationNotFoundException, SubscriptionNotFoundException, ApplicationIdentityNotFoundException,
            PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException;

    /*
     * Accessors
     */
    String getUsageAgreement();

    String getApplicationUrl();

}
