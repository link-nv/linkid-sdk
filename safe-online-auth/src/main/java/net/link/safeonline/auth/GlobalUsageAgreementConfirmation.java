package net.link.safeonline.auth;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.ApplicationIdentityNotFoundException;
import net.link.safeonline.authentication.exception.ApplicationNotFoundException;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;


@Local
public interface GlobalUsageAgreementConfirmation {

    /*
     * Actions.
     */
    String confirm() throws ApplicationNotFoundException, SubscriptionNotFoundException,
            ApplicationIdentityNotFoundException, PermissionDeniedException, AttributeTypeNotFoundException;

    /*
     * Accessors
     */
    String getUsageAgreement();

}
