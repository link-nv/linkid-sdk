package net.link.safeonline.beid.bean;

import javax.ejb.Stateless;

import net.link.safeonline.beid.BeidConstants;
import net.link.safeonline.beid.LanguageSelection;
import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("beidLanguage")
@LocalBinding(jndiBinding = BeidConstants.JNDI_PREFIX
		+ "LanguageSelectionBean/local")
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements
		LanguageSelection {

}
