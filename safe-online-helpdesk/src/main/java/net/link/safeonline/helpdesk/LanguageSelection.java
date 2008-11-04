package net.link.safeonline.helpdesk;

import javax.ejb.Local;

import net.link.safeonline.helpdesk.HelpdeskConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {

    public static final String JNDI_BINDING = HelpdeskConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
