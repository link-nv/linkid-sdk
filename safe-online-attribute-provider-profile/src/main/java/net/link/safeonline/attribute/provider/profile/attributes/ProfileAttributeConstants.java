package net.link.safeonline.attribute.provider.profile.attributes;

/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 04/10/11
 * Time: 12:00
 * To change this template use File | Settings | File Templates.
 */
public class ProfileAttributeConstants {

    public static final String EMAIL_CONFIRMATION_TIMEOUT_CONFIG = "Time to confirm email (min)";
    public static final String EMAIL_CONFIRMATION_TIMEOUT_CONFIG_GROUP = "Profile Attributes";

    public static final String ATTRIBUTE_CONFIRMATION_LANDING_PATH = "confirm-attribute";
    public static final String ATTRIBUTE_CONFIRMATION_PARAMETER_KEY = "key";

    public static final String JNDI_CONTEXT = "ProfileAttributeProvider";
    public static final String JNDI_PREFIX = JNDI_CONTEXT + "/";
    public static final String ENTITY_MANAGER = "EmailConfirmationManager";
}
