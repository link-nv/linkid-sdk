package net.link.safeonline.webapp.template;

import net.link.safeonline.wicket.web.WicketPage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class TemplatePage extends WicketPage {

    public static final String HEADER_ID  = "header_border";
    public static final String CONTENT_ID = "content_border";
    public static final String SIDEBAR_ID = "sidebar_border";

    protected final Log        LOG        = LogFactory.getLog(getClass());

    private HeaderBorder       headerBorder;
    private ContentBorder      contentBorder;
    private SidebarBorder      sidebarBorder;


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

        if (null == contentBorder) {
            contentBorder = new ContentBorder(CONTENT_ID);
            add(contentBorder);
        }

        return contentBorder;
    }

    /**
     * Help link is <b>ENABLED</b> by default using this method.
     * 
     * @see #getHeader(boolean)
     */
    public SidebarBorder getSidebar() {

        return getSidebar(true);
    }

    public SidebarBorder getSidebar(boolean showHelp) {

        if (null == sidebarBorder) {
            sidebarBorder = new SidebarBorder(SIDEBAR_ID, showHelp);
            getContent().add(sidebarBorder);
        }

        return sidebarBorder;
    }
}
