package net.link.safeonline.helpdesk.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.helpdesk.LanguageSelection;
import net.link.safeonline.helpdesk.HelpdeskConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("helpdeskLanguage")
@LocalBinding(jndiBinding = HelpdeskConstants.JNDI_PREFIX
		+ "LanguageSelectionBean/local")
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements
		LanguageSelection {

}
