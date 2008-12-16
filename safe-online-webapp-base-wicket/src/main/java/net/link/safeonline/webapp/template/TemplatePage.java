package net.link.safeonline.webapp.template;

import net.link.safeonline.wicket.web.WicketPage;


public abstract class TemplatePage extends WicketPage {

    public static final String HEADER_ID  = "header_border";
    public static final String CONTENT_ID = "content_border";
    public static final String SIDEBAR_ID = "sidebar_border";

    private HeaderBorder       headerBorder;
    private ContentBorder      contentBorder;
    private SidebarBorder      sidebarBorder;


    public HeaderBorder getHeader() {

        return getHeader(true);
    }

    public HeaderBorder getHeader(boolean logoutEnabled) {

        if (null == this.headerBorder) {
            this.headerBorder = new HeaderBorder(HEADER_ID, this, logoutEnabled);
            add(this.headerBorder);
        }

        return this.headerBorder;
    }

    public ContentBorder getContent() {

        if (null == this.contentBorder) {
            this.contentBorder = new ContentBorder(CONTENT_ID);
            add(this.contentBorder);
        }

        return this.contentBorder;
    }

    public SidebarBorder getSidebar() {

        if (null == this.sidebarBorder) {
            this.sidebarBorder = new SidebarBorder(SIDEBAR_ID);
            getContent().add(this.sidebarBorder);
        }

        return this.sidebarBorder;
    }
}
