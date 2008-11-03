package net.link.safeonline.encap;

import javax.ejb.Local;

import net.link.safeonline.encap.EncapConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = EncapConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
