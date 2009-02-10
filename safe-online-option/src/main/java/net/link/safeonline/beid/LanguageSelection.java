package net.link.safeonline.beid;

import javax.ejb.Local;

import net.link.safeonline.ctrl.LanguageSelectionBase;
import net.link.safeonline.model.beid.BeIdService;


@Local
public interface LanguageSelection extends LanguageSelectionBase {

    public static final String JNDI_BINDING = BeIdService.JNDI_PREFIX + "LanguageSelectionBean/local";

}
