package net.link.safeonline.auth.webapp.template;

import javax.ejb.EJB;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.common.SafeOnlineCookies;
import net.link.safeonline.model.application.PublicApplication;
import net.link.safeonline.service.PublicApplicationService;
import net.link.safeonline.webapp.template.TemplatePage;
import net.link.safeonline.wicket.tools.WicketUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AuthenticationTemplatePage extends TemplatePage {

    private static final Log LOG = LogFactory.getLog(AuthenticationTemplatePage.class);

    @EJB(mappedName = PublicApplicationService.JNDI_BINDING)
    PublicApplicationService publicApplicationService;


    public AuthenticationTemplatePage() {

        super();
    }

    protected String findApplicationUrl() {

        Cookie[] cookies = WicketUtil.getServletRequest().getCookies();
        try {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SafeOnlineCookies.APPLICATION_COOKIE)) {
                    PublicApplication application = publicApplicationService.findPublicApplication(Long.parseLong(cookie.getValue()));
                    if (null != application) {
                        if (null != application.getUrl()) {
                            LOG.debug("found url: " + application.getUrl().toString());
                            return application.getUrl().toString() + "?authenticationTimeout=true";
                        }
                    }
                }
            }
            return null;
        } finally {
            LOG.debug("removing entry and timeout cookie");
            HttpServletResponse response = WicketUtil.getServletResponse();
            removeCookie(SafeOnlineCookies.APPLICATION_COOKIE, response);
            removeCookie(SafeOnlineCookies.ENTRY_COOKIE, response);
            removeCookie(SafeOnlineCookies.TIMEOUT_COOKIE, response);
        }
    }

    protected void removeCookie(String name, HttpServletResponse response) {

        LOG.debug("remove cookie: " + name);
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}
