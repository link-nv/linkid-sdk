package net.link.safeonline.webapp.template;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.webapp.common.HelpPage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;


public class SidebarBorder extends Border {

    private static final long  serialVersionUID = 1L;

    Log                        LOG              = LogFactory.getLog(getClass());

    public static final String SIDE_LINKS_ID    = "side_links";
    public static final String HELP_LINK_ID     = "help-link";
    public static final String LINKS_ID         = "links";
    public static final String LINK_ID          = "link";
    public static final String LINK_MESSAGE_ID  = "link_message";

    public static final String HELP_MESSAGE_ID  = "help_message";

    protected List<SideLink>   sideLinks;


    public SidebarBorder(String id, String helpMessage, final boolean showHelpdeskLink) {

        super(id);
        sideLinks = new LinkedList<SideLink>();

        add(new WebMarkupContainer(SIDE_LINKS_ID) {

            private static final long serialVersionUID = 1L;

            {
                add(new HelpLink(HELP_LINK_ID, showHelpdeskLink));
                add(new ListView<SideLink>(LINKS_ID, new AbstractReadOnlyModel<List<SideLink>>() {

                    private static final long serialVersionUID = 1L;


                    @Override
                    public List<SideLink> getObject() {

                        return sideLinks;
                    }
                }) {

                    private static final long serialVersionUID = 1L;


                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void onBeforeRender() {

                        setVisible(!sideLinks.isEmpty());

                        super.onBeforeRender();
                    }

                    @Override
                    protected void populateItem(ListItem<SideLink> item) {

                        SideLink link = item.getModelObject();
                        item.add(link.getLink());
                        if (null == link.getLink().get(LINK_MESSAGE_ID)) {
                            link.getLink().add(new Label(LINK_MESSAGE_ID, link.getLinkMessage()));
                        }
                    }
                });
            }


            @Override
            protected void onBeforeRender() {

                setVisible(showHelpdeskLink || !sideLinks.isEmpty());

                super.onBeforeRender();
            }
        });

        add(new Label(HELP_MESSAGE_ID, helpMessage));
    }

    public void setLinks(SideLink... links) {

        sideLinks.clear();
        sideLinks.addAll(Arrays.asList(links));
    }


    class HelpLink extends Link<String> {

        private static final long serialVersionUID = 1L;

        private boolean           show             = false;


        public HelpLink(String id, boolean show) {

            super(id);
            this.show = show;
        }

        @Override
        public void onClick() {

            throw new RestartResponseException(new HelpPage(getPage()));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isVisible() {

            return show;
        }

    }

}
