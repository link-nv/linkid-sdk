package net.link.safeonline.webapp.listener;

import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LanguagePhaseListener implements PhaseListener {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory
			.getLog(LanguagePhaseListener.class);

	public void afterPhase(PhaseEvent phaseEvent) {
		PhaseId phaseId = phaseEvent.getPhaseId();
		if (phaseId == PhaseId.RESTORE_VIEW) {
			FacesContext facesContext = phaseEvent.getFacesContext();
			Cookie[] cookies = ((HttpServletRequest) facesContext
					.getExternalContext().getRequest()).getCookies();
			for (Cookie cookie : cookies) {
				if ("OLAS.language".equals(cookie.getName())) {
					String language = cookie.getValue();
					LOG.debug("use language stored in cookie: " + language);
					facesContext.getViewRoot().setLocale(new Locale(language));
				}
			}
		}
	}

	public void beforePhase(PhaseEvent phaseEvent) {
		return;
	}

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
