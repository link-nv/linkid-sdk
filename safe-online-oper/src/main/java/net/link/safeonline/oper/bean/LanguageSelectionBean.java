package net.link.safeonline.oper.bean;

import javax.ejb.Stateless;

import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;
import net.link.safeonline.oper.LanguageSelection;
import net.link.safeonline.oper.OperatorConstants;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("operLanguage")
@LocalBinding(jndiBinding = OperatorConstants.JNDI_PREFIX + "LanguageSelectionBean/local")
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

}
