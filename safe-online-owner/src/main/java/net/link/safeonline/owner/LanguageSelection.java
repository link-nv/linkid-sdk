package net.link.safeonline.owner;

import javax.ejb.Local;

import net.link.safeonline.owner.OwnerConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = OwnerConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
