package net.link.safeonline.webapp.template;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;


public abstract class TemplatePage extends WebPage {

    public static final String HEADER_ID  = "header_border";
    public static final String CONTENT_ID = "content_border";
    public static final String SIDEBAR_ID = "sidebar_border";

    private HeaderBorder       headerBorder;

    private ContentBorder      contentBorder;

    private SidebarBorder      sidebarBorder;


    public void addHeader(Page page) {

        if (null == headerBorder) {
            headerBorder = new HeaderBorder(HEADER_ID, page);
            super.add(headerBorder);
        }
    }

    public void addHeader(Page page, boolean logoutEnabled) {

        if (null == headerBorder) {
            headerBorder = new HeaderBorder(HEADER_ID, page, logoutEnabled);
            super.add(headerBorder);
        }

    }

    public HeaderBorder getHeader(Page page) {

        addHeader(page);
        return headerBorder;
    }

    public void addContent() {

        if (null == contentBorder) {
            contentBorder = new ContentBorder(CONTENT_ID);
            super.add(contentBorder);
        }
    }

    public ContentBorder getContent() {

        addContent();
        return contentBorder;
    }

    public void addSidebar() {

        if (null == sidebarBorder) {
            sidebarBorder = new SidebarBorder(SIDEBAR_ID);
            addContent();
            contentBorder.add(sidebarBorder);
        }
    }

    public SidebarBorder getSidebar() {

        addSidebar();
        return sidebarBorder;
    }
}
