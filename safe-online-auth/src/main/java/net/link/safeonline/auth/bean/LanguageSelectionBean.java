package net.link.safeonline.auth.bean;

import javax.ejb.Stateless;

import net.link.safeonline.auth.AuthenticationConstants;
import net.link.safeonline.auth.LanguageSelection;
import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("authLanguage")
@LocalBinding(jndiBinding = AuthenticationConstants.JNDI_PREFIX + "LanguageSelectionBean/local")
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
