package net.link.safeonline.attribute.provider.profile.bean;

import java.util.Date;
import net.link.safeonline.attribute.provider.profile.attributes.ProfileAttributeConstants;
import net.link.safeonline.attribute.provider.profile.entity.EmailConfirmationEntity;


/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 20/10/11
 * Time: 13:55
 * To change this template use File | Settings | File Templates.
 */
public interface EmailConfirmationManager {

    public static final String JNDI_BINDING = ProfileAttributeConstants.JNDI_PREFIX + "EmailConfirmationManager/local";

    public EmailConfirmationEntity createNewEmailConfirmation(String userId, String email)
            throws ConfirmationInProgressException;

    public String getUserId(String confirmationId);
    
    public String getConfirmationIdForUser(String userId, boolean filterExpired);

    public String getConfirmationIdForEmail(String email, boolean filterExpired);

    public void removeAllExpired();

    public void remove(String confirmationId);

}
