package net.link.safeonline.demo.cinema.webapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.CinemaConstants;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.demo.wicket.tools.OlasLogoutLink;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.sdk.auth.filter.LoginManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;


public class LayoutPage extends WebPage {

    private static final long serialVersionUID = 1L;

    Log                       LOG              = LogFactory.getLog(getClass());

    @EJB(mappedName = TicketService.JNDI_BINDING)
    transient TicketService   ticketService;


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


    class UserInfo extends WebMarkupContainer {

        private static final long serialVersionUID = 1L;
        Model<String>             name;
        Model<String>             nrn;
        Model<Boolean>            junior;


        public UserInfo(String id) {

            super(id);

            add(new OlasLogoutLink("logout"));
            add(new Label("name", this.name = new Model<String>()));
            add(new Label("nrn", this.nrn = new Model<String>()));
            add(new Label("junior", this.junior = new Model<Boolean>(false)) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void onComponentTag(ComponentTag tag) {

                    super.onComponentTag(tag);

                    if (UserInfo.this.junior.getObject()) {
                        tag.put("class", "selected");
                    }
                }

                @Override
                protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

                    replaceComponentTagBody(markupStream, openTag, UserInfo.this.junior.getObject()? "Junior Discount": "No Discount");
                }
            });

            if (CinemaSession.isUserSet()) {
                this.name.setObject(CinemaSession.get().getUser().getName());
                this.nrn.setObject(CinemaSession.get().getUser().getNrn());
                this.junior.setObject(CinemaSession.get().getUser().isJunior());
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

            add(new Label("name", this.name = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetFilm();
                    setResponsePage(FilmTheatreSelectionPage.class);
                }
            });

            // Put film name in label or hide if no film selected.
            if (CinemaSession.isFilmSet()) {
                this.name.setObject(CinemaSession.get().getFilm().getName());
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

            add(new Label("name", this.name = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetTheatre();
                    setResponsePage(FilmTheatreSelectionPage.class);
                }
            });

            // Put theatre name in label or hide if no theatre selected.
            if (CinemaSession.isTheaterSet()) {
                this.name.setObject(CinemaSession.get().getTheatre().getName());
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

            add(new Label("time", this.time = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetTime();
                    setResponsePage(TimeRoomSelectionPage.class);
                }
            });

            // Put time in label (formatted) or hide if no time selected.
            if (CinemaSession.isTimeSet()) {
                this.time.setObject(WicketUtil.format(getLocale(), CinemaSession.get().getTime()));
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

            add(new Label("name", this.name = new Model<String>()));
            add(new Link<String>("delete") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    CinemaSession.get().resetRoom();
                    setResponsePage(TimeRoomSelectionPage.class);
                }
            });

            // Put name of the room in label or hide if no room selected.
            if (CinemaSession.isRoomSet()) {
                this.name.setObject(CinemaSession.get().getRoom().getName());
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

            add(new Label("price", this.price = new Model<String>()));

            // Check to see if our ticket data is complete - if so, create a ticket.
            if (!CinemaSession.isTicketSet()) {
                if (CinemaSession.isFilmAndTheaterSet() && CinemaSession.isTimeAndRoomSet() && CinemaSession.isSeatSet()
                        && CinemaSession.isUserSet()) {
                    try {
                        CinemaSession.get().setTicket(
                                LayoutPage.this.ticketService.createTicket(CinemaSession.get().getUser(), CinemaSession.get().getFilm(),
                                        CinemaSession.get().getTime(), CinemaSession.get().getOccupation()));
                    }

                    catch (IllegalStateException e) {
                        LayoutPage.this.LOG.error("Removing seat selection.", e);
                        CinemaSession.get().toggleSeat(CinemaSession.get().getOccupation().getSeat());
                    }
                }
            }

            // Put price in label or hide if ticket is not complete.
            if (CinemaSession.isTicketSet()) {
                this.price.setObject(WicketUtil.format(CinemaSession.CURRENCY,
                        LayoutPage.this.ticketService.calculatePrice(CinemaSession.get().getTicket())));
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
                    String paymentUsername = CinemaSession.get().getUser().getName();
                    CinemaTicketEntity ticket = CinemaSession.get().getTicket();
                    double paymentPrice = ticket.getPrice();
                    String paymentMessage = String.format("Viewing of %s at %s in %s.", LayoutPage.this.ticketService.getFilmName(ticket),
                            WicketUtil.format(getLocale(), new Date(ticket.getTime())),
                            LayoutPage.this.ticketService.getTheatreName(ticket));
                    String paymentTargetUrl = RequestUtils.toAbsolutePath(RequestCycle.get().urlFor(
                            new BookmarkablePageRequestTarget(TicketPage.class)).toString());

                    try {
                        String redirectUrl = String.format("%s?user=%s&recipient=%s&amount=%s&message=%s&target=%s",

                        // Demo-Payment application.
                                RequestUtils.toAbsolutePath("../demo-payment/entry.seam"),

                                // Paying user's OLAS name.
                                URLEncoder.encode(paymentUsername, "UTF-8"),

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
                        LayoutPage.this.ticketService.reserve(CinemaSession.get().getTicket());
                        CinemaSession.get().resetTicket();

                        // Redirect user to demo-payment.
                        throw new RedirectToUrlException(redirectUrl);
                    }

                    catch (UnsupportedEncodingException e) {
                        LayoutPage.this.LOG.error("URL encoding error", e);
                    }
                }
            });
        }
    }
}
