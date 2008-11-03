package net.link.safeonline.user;

import javax.ejb.Local;

import net.link.safeonline.user.UserConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = UserConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
