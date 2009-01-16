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
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.OlasApplicationPage;
import net.link.safeonline.wicket.web.OlasLogoutLink;

import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;


public class LayoutPage extends OlasApplicationPage {

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = TicketService.JNDI_BINDING)
    transient TicketService   ticketService;

    @EJB(mappedName = UserService.JNDI_BINDING)
    transient UserService     userService;


    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label("pageTitle", "Cinema Demo Application"));

        add(new UserInfo("user"));
        add(new Ticket("ticket"));
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

            if (CinemaSession.get().isUserSet()) {
                name.setObject(CinemaSession.get().getUser().getName());
                nrn.setObject(CinemaSession.get().getUser().getNrn());
                junior.setObject(CinemaSession.get().getUser().isJunior());
            } else {
                setVisible(false);
            }
        }
    }

    class Ticket extends WebMarkupContainer {

        public Ticket(String id) {

            super(id);

            setVisible(LoginManager.isAuthenticated(((WebRequest) getRequest()).getHttpServletRequest())
                    && (CinemaSession.isFilmSet() || CinemaSession.isTheaterSet() || CinemaSession.isRoomSet() || CinemaSession.isSeatSet()
                            || CinemaSession.isTimeSet() || CinemaSession.isTicketSet()));

            add(new SelectedFilm("film"));
            add(new SelectedTheatre("theatre"));
            add(new SelectedTime("time"));
            add(new SelectedRoom("room"));
            add(new SelectedPrice("payment"));
        }


        private static final long serialVersionUID = 1L;

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
                    throw new RestartResponseException(FilmTheatreSelectionPage.class);
                }
            });

            // Put film name in label or hide if no film selected.
            if (CinemaSession.isFilmSet()) {
                name.setObject(CinemaSession.get().getFilm().getName());
            } else {
                setVisible(false);
            }
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
                    throw new RestartResponseException(FilmTheatreSelectionPage.class);
                }
            });

            // Put theatre name in label or hide if no theatre selected.
            if (CinemaSession.isTheaterSet()) {
                name.setObject(CinemaSession.get().getTheatre().getName());
            } else {
                setVisible(false);
            }
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
                    throw new RestartResponseException(TimeRoomSelectionPage.class);
                }
            });

            // Put time in label (formatted) or hide if no time selected.
            if (CinemaSession.isTimeSet()) {
                time.setObject(WicketUtil.format(getLocale(), CinemaSession.get().getTime()));
            } else {
                setVisible(false);
            }
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
                    throw new RestartResponseException(TimeRoomSelectionPage.class);
                }
            });

            // Put name of the room in label or hide if no room selected.
            if (CinemaSession.isRoomSet()) {
                name.setObject(CinemaSession.get().getRoom().getName());
            } else {
                setVisible(false);
            }
        }
    }

    class SelectedPrice extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        private IModel<String>    price;


        public SelectedPrice(String id) {

            super(id);

            add(new Label("price", price = new Model<String>()));

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

            // Put price in label or hide if ticket is not complete.
            if (CinemaSession.isTicketSet()) {
                price.setObject(WicketUtil.format(CinemaSession.CURRENCY, ticketService.calculatePrice(CinemaSession.get().getTicket())));
            } else {
                setVisible(false);
            }

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
    }
}
