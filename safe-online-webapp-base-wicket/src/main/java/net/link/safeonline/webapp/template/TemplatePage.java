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


    public HeaderBorder getHeader() {

        return getHeader(true);
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

    public SidebarBorder getSidebar() {

        if (null == sidebarBorder) {
            sidebarBorder = new SidebarBorder(SIDEBAR_ID);
            getContent().add(sidebarBorder);
        }

        return sidebarBorder;
    }
}
