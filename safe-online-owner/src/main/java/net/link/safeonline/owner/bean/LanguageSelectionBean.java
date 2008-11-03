package net.link.safeonline.owner.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.owner.LanguageSelection;
import net.link.safeonline.owner.OwnerConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("ownerLanguage")
@LocalBinding(jndiBinding = LanguageSelection.JNDI_BINDING)
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
