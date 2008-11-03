package net.link.safeonline.user.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.user.LanguageSelection;
import net.link.safeonline.user.UserConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("language")
@LocalBinding(jndiBinding = LanguageSelection.JNDI_BINDING)
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
