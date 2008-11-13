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

        if (null == this.headerBorder) {
            this.headerBorder = new HeaderBorder(HEADER_ID, page);
            super.add(this.headerBorder);
        }
    }

    public HeaderBorder getHeader(Page page) {

        addHeader(page);
        return this.headerBorder;
    }

    public void addContent() {

        if (null == this.contentBorder) {
            this.contentBorder = new ContentBorder(CONTENT_ID);
            super.add(this.contentBorder);
        }
    }

    public ContentBorder getContent() {

        addContent();
        return this.contentBorder;
    }

    public void addSidebar() {

        if (null == this.sidebarBorder) {
            this.sidebarBorder = new SidebarBorder(SIDEBAR_ID);
            addContent();
            this.contentBorder.add(this.sidebarBorder);
        }
    }

    public SidebarBorder getSidebar() {

        addSidebar();
        return this.sidebarBorder;
    }
}
