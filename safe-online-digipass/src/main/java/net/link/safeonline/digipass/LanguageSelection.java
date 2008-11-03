package net.link.safeonline.digipass;

import javax.ejb.Local;

import net.link.safeonline.digipass.DigipassConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = DigipassConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
