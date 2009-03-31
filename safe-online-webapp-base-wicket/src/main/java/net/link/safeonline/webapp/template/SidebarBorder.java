package net.link.safeonline.webapp.template;

import java.util.LinkedList;
import java.util.List;

import net.link.safeonline.webapp.common.HelpPage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


public class SidebarBorder extends Border {

    private static final long  serialVersionUID = 1L;

    Log                        LOG              = LogFactory.getLog(getClass());

    public static final String SIDE_LINKS_ID    = "side_links";
    public static final String HELP_LINK_ID     = "help-link";
    public static final String LINKS_ID         = "links";
    public static final String LINK_ID          = "link";
    public static final String LINK_MESSAGE_ID  = "link_message";

    public static final String HELP_MESSAGE_ID  = "help_message";


    public SidebarBorder(String id, String helpMessage, final boolean showHelpdeskLink, final SideLink... links) {

        super(id);

        add(new WebMarkupContainer(SIDE_LINKS_ID) {

            private static final long serialVersionUID = 1L;

            {

                final List<SideLink> linkList = new LinkedList<SideLink>();
                CollectionUtils.addAll(linkList, links);

                add(new HelpLink(HELP_LINK_ID, showHelpdeskLink));
                add(new ListView<SideLink>(LINKS_ID, linkList) {

                    private static final long serialVersionUID = 1L;


                    @Override
                    protected void populateItem(ListItem<SideLink> item) {

                        SideLink link = item.getModelObject();
                        item.add(link.getLink());
                        if (null == link.getLink().get(LINK_MESSAGE_ID)) {
                            link.getLink().add(new Label(LINK_MESSAGE_ID, link.getLinkMessage()));
                        }
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isVisible() {

                        return !linkList.isEmpty();
                    }

                });

            }


            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isVisible() {

                return showHelpdeskLink || links.length > 0;
            }

        });

        add(new Label(HELP_MESSAGE_ID, helpMessage));
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
