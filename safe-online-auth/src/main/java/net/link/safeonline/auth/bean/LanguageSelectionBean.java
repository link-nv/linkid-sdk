package net.link.safeonline.auth.bean;

import javax.ejb.Stateless;

import net.link.safeonline.auth.LanguageSelection;
import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.ctrl.bean.LanguageSelectionBaseBean;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.seam.annotations.Name;


@Stateless
@Name("authLanguage")
@LocalBinding(jndiBinding = LanguageSelection.JNDI_BINDING)
public class LanguageSelectionBean extends LanguageSelectionBaseBean implements LanguageSelection {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getLanguageCookieName() {

        return SafeOnlineCookies.AUTH_LANGUAGE_COOKIE;
    }
}
