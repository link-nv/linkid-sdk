package net.link.safeonline.webapp.template;

import javax.servlet.http.HttpSession;

import net.link.safeonline.common.SafeOnlineAppConstants;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.WicketPage;

import org.apache.wicket.behavior.HeaderContributor;


public abstract class TemplatePage extends WicketPage {

    public static final String HEADER_ID  = "header_border";
    public static final String CONTENT_ID = "content_border";
    public static final String SIDEBAR_ID = "sidebar_border";

    protected HeaderBorder     headerBorder;
    protected ContentBorder    contentBorder;
    protected SidebarBorder    sidebarBorder;


    public TemplatePage() {

        // If minimal session attribute is set, add minimal.css style sheet.
        HttpSession httpSession = WicketUtil.getHttpSession(getRequest());
        Object minimalAttribute = httpSession.getAttribute(SafeOnlineAppConstants.MINIMAL_ATTRIBUTE);
        boolean isMinimal = Boolean.parseBoolean(String.valueOf(minimalAttribute));
        if (isMinimal) {
            add(HeaderContributor.forCss(TemplatePage.class, "minimal.css"));
        }

        // Add the <h1>page title</h1> component.
        contentBorder = new ContentBorder(CONTENT_ID, getPageTitle());
        add(contentBorder);
    }

    /**
     * Call this method after you have programmatically changed the state of the page such that the title needs to be updated.
     */
    protected void updatePageTitle() {

        contentBorder.get("pageTitle").setDefaultModelObject(getPageTitle());
    }

    /**
     * @return The main title of the page.
     */
    protected abstract String getPageTitle();

    /**
     * Logout link is <b>DISABLED</b> by default using this method.
     * 
     * @see #getHeader(boolean)
     */
    public HeaderBorder getHeader() {

        return getHeader(false);
    }

    public HeaderBorder getHeader(boolean logoutEnabled) {

        if (null == headerBorder) {
            headerBorder = new HeaderBorder(HEADER_ID, this, logoutEnabled);
            add(headerBorder);
        }

        return headerBorder;
    }

    public ContentBorder getContent() {

        return contentBorder;
    }

    /**
     * Help link is <b>ENABLED</b> by default using this method.
     * 
     * @see #getHeader(boolean)
     */
    public SidebarBorder getSidebar(String helpMessage) {

        return getSidebar(helpMessage, true);
    }

    public SidebarBorder getSidebar(String helpMessage, boolean showHelpdeskLink) {

        if (null == sidebarBorder) {
            sidebarBorder = new SidebarBorder(SIDEBAR_ID, helpMessage, showHelpdeskLink);
            contentBorder.add(sidebarBorder);
        }

        return sidebarBorder;
    }
}
