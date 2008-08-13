package net.link.safeonline.digipass.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.digipass.DigipassConstants;
import net.link.safeonline.digipass.LanguageSelection;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("digipassLanguage")
@LocalBinding(jndiBinding = DigipassConstants.JNDI_PREFIX + "LanguageSelectionBean/local")
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
