package net.link.safeonline.option;

import javax.ejb.Local;

import net.link.safeonline.option.OptionConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = OptionConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
