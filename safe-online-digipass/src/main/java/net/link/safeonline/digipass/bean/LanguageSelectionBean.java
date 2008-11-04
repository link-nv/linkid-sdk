package net.link.safeonline.digipass.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.digipass.LanguageSelection;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("digipassLanguage")
@LocalBinding(jndiBinding = LanguageSelection.JNDI_BINDING)
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
