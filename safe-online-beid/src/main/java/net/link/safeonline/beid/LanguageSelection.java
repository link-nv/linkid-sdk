package net.link.safeonline.beid;

import javax.ejb.Local;

import net.link.safeonline.beid.BeidConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = BeidConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
