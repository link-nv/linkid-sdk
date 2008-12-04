package net.link.safeonline.option;

import javax.ejb.Local;

import net.link.safeonline.ctrl.LanguageSelectionBase;
import net.link.safeonline.model.option.OptionService;


@Local
public interface LanguageSelection extends LanguageSelectionBase {

    public static final String JNDI_BINDING = OptionService.JNDI_PREFIX + "LanguageSelectionBean/local";

}
