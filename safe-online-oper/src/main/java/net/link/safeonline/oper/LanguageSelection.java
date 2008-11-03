package net.link.safeonline.oper;

import javax.ejb.Local;

import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.ctrl.LanguageSelectionBase;


@Local
public interface LanguageSelection extends LanguageSelectionBase {
    public static final String JNDI_BINDING = OperatorConstants.JNDI_PREFIX + "LanguageSelectionBean/local";

}
