package net.link.safeonline.encap.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.encap.LanguageSelection;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("encapLanguage")
@LocalBinding(jndiBinding = LanguageSelection.JNDI_BINDING)
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
