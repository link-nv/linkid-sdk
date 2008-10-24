package net.link.safeonline.option.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.option.LanguageSelection;
import net.link.safeonline.option.OptionConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("optionLanguage")
@LocalBinding(jndiBinding = OptionConstants.JNDI_PREFIX + "LanguageSelectionBean/local")
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
