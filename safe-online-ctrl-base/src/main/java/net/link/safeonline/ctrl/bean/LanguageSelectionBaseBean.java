package net.link.safeonline.ctrl.bean;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.ctrl.LanguageSelectionBase;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.log.Log;

public class LanguageSelectionBaseBean implements LanguageSelectionBase {

	public static final String LAST_PAGE = "lang.selection.lastpage";

	@In
	Context sessionContext;

	@In
	FacesContext facesContext;

	@In(create = true)
	LocaleSelector localeSelector;

	@Logger
	private Log log;

	public String entry() {
		this.sessionContext.set(LAST_PAGE, this.facesContext.getViewRoot()
				.getViewId());
		return "/language.xhtml";
	}

	public String goBack() {
		String result = (String) this.sessionContext.get(LAST_PAGE);
		this.sessionContext.remove(LAST_PAGE);
		return result;
	}

	public void selectLanguage(ActionEvent event) {
		this.log.debug("selected language: " + event.getComponent().getId());
		this.localeSelector.selectLanguage(event.getComponent().getId());

		HttpServletResponse response = (HttpServletResponse) this.facesContext
				.getExternalContext().getResponse();

		Cookie languageCookie = new Cookie("OLAS.language", event
				.getComponent().getId());
		languageCookie.setPath("/");
		languageCookie.setMaxAge(60 * 60 * 24 * 30 * 6);
		response.addCookie(languageCookie);
	}
}
