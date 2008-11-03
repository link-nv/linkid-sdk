package net.link.safeonline.auth;

import javax.ejb.Local;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = AuthenticationConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
