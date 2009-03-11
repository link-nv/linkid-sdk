package net.link.safeonline.demo.cinema.webapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.CinemaConstants;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.demo.cinema.service.UserService;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OlasApplicationPage;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;


public class LayoutPage extends OlasApplicationPage {

    static final Log          LOG              = LogFactory.getLog(LayoutPage.class);

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = TicketService.JNDI_BINDING)
    TicketService             ticketService;

    @EJB(mappedName = UserService.JNDI_BINDING)
    UserService               userService;

    private Ticket            ticketForm;

    private UserInfo          userForm;

    private FeedbackPanel     globalFeedback;


    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label("pageTitle", "Cinema Demo Application"));
        add(globalFeedback = new FeedbackPanel("globalFeedback"));

        add(userForm = new UserInfo("user"));
        add(ticketForm = new Ticket("ticket"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        // Check to see if our ticket data is complete - if so, create a ticket.
        if (!CinemaSession.isTicketSet()) {
            if (CinemaSession.isFilmAndTheaterSet() && CinemaSession.isTimeAndRoomSet() && CinemaSession.isSeatSet()
                    && CinemaSession.get().isUserSet()) {
                try {
                    CinemaSession.get().setTicket(
                            ticketService.createTicket(CinemaSession.get().getUser(), CinemaSession.get().getFilm(),
                                    CinemaSession.get().getTime(), CinemaSession.get().getOccupation()));
                }

                catch (IllegalStateException e) {
                    LOG.error("Removing seat selection.", e);
                    CinemaSession.get().toggleSeat(CinemaSession.get().getOccupation().getSeat());
                }
            }
        }

        if (CinemaSession.get().isUserSet()) {
            CinemaSession.get().setUser(userService.updateUser(CinemaSession.get().getUser(), WicketUtil.toServletRequest(getRequest())));
        }

        userForm.setVisible(CinemaSession.get().isUserSet());
        ticketForm.setVisible(CinemaSession.get().isUserSet()
                && (CinemaSession.isFilmSet() || CinemaSession.isTheaterSet() || CinemaSession.isRoomSet() || CinemaSession.isSeatSet()
                        || CinemaSession.isTimeSet() || CinemaSession.isTicketSet()));

        globalFeedback.setVisible(globalFeedback.anyErrorMessage());

        super.onBeforeRender();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOlasAuthenticated() {

        String olasId = WicketUtil.findOlasId(getRequest());
        CinemaUserEntity user = userService.getUser(olasId);

        CinemaSession.get().setUser(userService.updateUser(user, WicketUtil.toServletRequest(getRequest())));
    }


    class UserInfo extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        Model<String>             name;
        Model<String>             nrn;
        Model<Boolean>            junior;


        public UserInfo(String id) {

            super(id);

            add(new OlasLogoutLink("logout"));
            add(new Label("name", name = new Model<String>()));
            add(new Label("nrn", nrn = new Model<String>()));
            add(new Label("junior", junior = new Model<Boolean>(false)) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void onComponentTag(ComponentTag tag) {

                    super.onComponentTag(tag);

                    if (junior.getObject()) {
                        tag.put("class", "selected");
                    }
                }

                @Override
                protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

                    replaceComponentTagBody(markupStream, openTag, junior.getObject()? "Junior Discount": "No Discount");
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            if (CinemaSession.get().isUserSet()) {
                name.setObject(CinemaSession.get().getUser().getName());
                nrn.setObject(CinemaSession.get().getUser().getNrn());
                junior.setObject(CinemaSession.get().getUser().isJunior());
            }

            super.onBeforeRender();
        }
    }

    class Ticket extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private SelectedPrice     paymentForm;
        private SelectedRoom      roomForm;
        private SelectedTime      timeForm;
        private SelectedTheatre   theatreForm;
        private SelectedFilm      filmForm;


        public Ticket(String id) {

            super(id);

            add(filmForm = new SelectedFilm("film"));
            add(theatreForm = new SelectedTheatre("theatre"));
            add(timeForm = new SelectedTime("time"));
            add(roomForm = new SelectedRoom("room"));
            add(paymentForm = new SelectedPrice("payment"));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            filmForm.setVisible(CinemaSession.isFilmSet());
            theatreForm.setVisible(CinemaSession.isTheaterSet());
            timeForm.setVisible(CinemaSession.isTimeSet());
            roomForm.setVisible(CinemaSession.isRoomSet());
            paymentForm.setVisible(CinemaSession.isTicketSet());

            super.onBeforeRender();
        }
    }

    class SelectedFilm extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     name;


        public SelectedFilm(String id) {

            super(id);

            add(new Label("name", name = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetFilm();
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Put film name in label if film selected.
            if (CinemaSession.isFilmSet()) {
                name.setObject(CinemaSession.get().getFilm().getName());
            }

            super.onBeforeRender();
        }
    }

    class SelectedTheatre extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     name;


        public SelectedTheatre(String id) {

            super(id);

            add(new Label("name", name = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetTheatre();
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Put theatre name in label if theatre selected.
            if (CinemaSession.isTheaterSet()) {
                name.setObject(CinemaSession.get().getTheatre().getName());
            }

            super.onBeforeRender();
        }
    }

    class SelectedTime extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     time;


        public SelectedTime(String id) {

            super(id);

            add(new Label("time", time = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetTime();
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Put time in label (formatted) if time selected.
            if (CinemaSession.isTimeSet()) {
                time.setObject(WicketUtil.format(getLocale(), CinemaSession.get().getTime()));
            }

            super.onBeforeRender();
        }
    }

    class SelectedRoom extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private Model<String>     name;


        public SelectedRoom(String id) {

            super(id);

            add(new Label("name", name = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetRoom();
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Put name of the room in label if room selected.
            if (CinemaSession.isRoomSet()) {
                name.setObject(CinemaSession.get().getRoom().getName());
            }

            super.onBeforeRender();
        }
    }

    class SelectedPrice extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private IModel<String>    price;


        public SelectedPrice(String id) {

            super(id);

            add(new Label("price", price = new Model<String>()));
            add(new Link<String>("pay") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    if (!CinemaSession.isTicketSet())
                        return;

                    // Redirect the user to demo-payment
                    CinemaTicketEntity ticket = CinemaSession.get().getTicket();
                    double paymentPrice = ticket.getPrice();
                    String paymentMessage = String.format("Viewing of %s at %s in %s.", ticketService.getFilmName(ticket),
                            WicketUtil.format(getLocale(), new Date(ticket.getTime())), ticketService.getTheatreName(ticket));
                    String paymentTargetUrl = RequestUtils.toAbsolutePath(RequestCycle.get().urlFor(
                            new BookmarkablePageRequestTarget(TicketPage.class)).toString());

                    try {
                        String redirectUrl = String.format("%s?recipient=%s&amount=%s&message=%s&target=%s",

                        // Demo-Payment application.
                                RequestUtils.toAbsolutePath("../demo-payment/service"),

                                // Payment Recipient.
                                URLEncoder.encode(CinemaConstants.PAYMENT_RECIPIENT, "UTF-8"),

                                // Payment Amount.
                                URLEncoder.encode(Double.toString(paymentPrice), "UTF-8"),

                                // Payment Message.
                                URLEncoder.encode(paymentMessage, "UTF-8"),

                                // Return page after payment completion.
                                URLEncoder.encode(paymentTargetUrl, "UTF-8"));

                        // FIXME: Cheat by reserving ticket before payment has
                        // actually been completed.
                        ticketService.reserve(CinemaSession.get().getTicket());
                        CinemaSession.get().resetTicket();

                        // Redirect user to demo-payment.
                        throw new RedirectToUrlException(redirectUrl);
                    }

                    catch (UnsupportedEncodingException e) {
                        LOG.error("URL encoding error", e);
                    }
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Put price in label if ticket is complete.
            if (CinemaSession.isTicketSet()) {
                price.setObject(WicketUtil.format(CinemaSession.CURRENCY, ticketService.calculatePrice(CinemaSession.get().getTicket())));
            }

            super.onBeforeRender();
        }
    }
}
