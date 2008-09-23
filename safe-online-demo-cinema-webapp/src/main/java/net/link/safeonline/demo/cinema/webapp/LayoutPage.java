package net.link.safeonline.demo.cinema.webapp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.demo.cinema.CinemaConstants;
import net.link.safeonline.demo.cinema.entity.TicketEntity;
import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.seam.SafeOnlineLoginUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;


public class LayoutPage extends WebPage<Object> {

    private static final long serialVersionUID = 1L;
    Log                       LOG              = LogFactory.getLog(getClass());

    @EJB
    transient TicketService   ticketService;


    /**
     * Add components to the layout that are present on every page.
     * 
     * This includes the title and the global ticket.
     */
    public LayoutPage() {

        add(new Label<String>("pageTitle", "Cinema Demo Application"));

        add(new UserInfo("user"));
        add(new Ticket("ticket"));
    }


    class UserInfo extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;


        public UserInfo(String id) {

            super(id);

            add(new Link<String>("logout") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    getRequestCycle().setRequestTarget(new IRequestTarget() {

                        public void detach(RequestCycle requestCycle) {

                        }

                        public void respond(RequestCycle requestCycle) {

                            HttpServletRequest request = ((WebRequest) requestCycle.getRequest())
                                    .getHttpServletRequest();
                            HttpServletResponse response = ((WebResponse) requestCycle.getResponse())
                                    .getHttpServletResponse();
                            String target = request.getServletPath();
                            String userId = CinemaSession.get().getUser().getId();

                            // setRedirect(true);
                            // setResponsePage(LoginPage.class);

                            SafeOnlineLoginUtils.logout(userId, target, request, response);
                        }
                    });
                }
            });
            Label<String> name = new Label<String>("name");
            add(name);
            Label<String> nrn = new Label<String>("nrn");
            add(nrn);
            Label<Boolean> junior = new Label<Boolean>("junior", new Model<Boolean>(false)) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void onComponentTag(ComponentTag tag) {

                    super.onComponentTag(tag);

                    if (getModelObject()) {
                        tag.put("class", "selected");
                    }
                }

                @Override
                protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {

                    replaceComponentTagBody(markupStream, openTag, getModelObject()? "Junior Discount": "No Discount");
                }
            };
            add(junior);

            if (CinemaSession.isUserSet()) {
                name.setModel(new Model<String>(CinemaSession.get().getUser().getName()));
                nrn.setModel(new Model<String>(CinemaSession.get().getUser().getNrn()));
                junior.setModel(new Model<Boolean>(CinemaSession.get().getUser().isJunior()));
            } else {
                setVisible(false);
            }
        }
    }

    class Ticket extends WebMarkupContainer<String> {

        public Ticket(String id) {

            super(id);

            setVisible(LoginManager.isAuthenticated(((WebRequest) getRequest()).getHttpServletRequest())
                    && (CinemaSession.isFilmSet() || CinemaSession.isTheaterSet() || CinemaSession.isRoomSet()
                            || CinemaSession.isSeatSet() || CinemaSession.isTimeSet() || CinemaSession.isTicketSet()));

            add(new SelectedFilm("film"));
            add(new SelectedTheatre("theatre"));
            add(new SelectedTime("time"));
            add(new SelectedRoom("room"));
            add(new SelectedTicket("ticket"));
        }


        private static final long serialVersionUID = 1L;

    }

    class SelectedFilm extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;


        public SelectedFilm(String id) {

            super(id);

            Label<String> name = new Label<String>("name");
            add(name);
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
                name.setModel(new Model<String>(CinemaSession.get().getFilm().getName()));
            } else {
                setVisible(false);
            }
        }
    }

    class SelectedTheatre extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;


        public SelectedTheatre(String id) {

            super(id);

            Label<String> name = new Label<String>("name");
            add(name);
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
                name.setModel(new Model<String>(CinemaSession.get().getTheatre().getName()));
            } else {
                setVisible(false);
            }
        }
    }

    class SelectedTime extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;


        public SelectedTime(String id) {

            super(id);

            Label<String> time = new Label<String>("time");
            add(time);
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
                time.setModel(new Model<String>(CinemaSession.format(CinemaSession.get().getTime())));
            } else {
                setVisible(false);
            }
        }
    }

    class SelectedRoom extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;


        public SelectedRoom(String id) {

            super(id);

            Label<String> name = new Label<String>("name");
            add(name);
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
                name.setModel(new Model<String>(CinemaSession.get().getRoom().getName()));
            } else {
                setVisible(false);
            }
        }
    }

    class SelectedTicket extends WebMarkupContainer<String> {

        private static final long serialVersionUID = 1L;


        public SelectedTicket(String id) {

            super(id);

            Label<String> price = new Label<String>("price");
            add(price);
            add(new PayForm("pay"));

            // Put name of the room in label or hide if no room selected.
            if (CinemaSession.isTicketSet()) {
                price.setModel(new Model<String>(CinemaSession.format(LayoutPage.this.ticketService
                        .calculatePrice(CinemaSession.get().getTicket()))));
            } else {
                setVisible(false);
            }
        }


        class PayForm extends Form<String> {

            private static final long serialVersionUID = 1L;


            public PayForm(String id) {

                super(id);
            }

            @Override
            protected void onSubmit() {

                if (!CinemaSession.isTicketSet())
                    return;

                // Redirect the user to demo-payment
                HttpServletRequest request = ((WebRequest) getRequest()).getHttpServletRequest();
                HttpServletResponse response = ((WebResponse) getResponse()).getHttpServletResponse();

                String target = response.encodeRedirectURL(request.getRequestURL().toString());

                try {
                    String username = CinemaSession.get().getUser().getName();
                    TicketEntity ticket = CinemaSession.get().getTicket();
                    double price = ticket.getPrice();
                    String message = String.format("Viewing of %s at %s in %s.", LayoutPage.this.ticketService
                            .getFilmName(ticket), CinemaSession.format(new Date(ticket.getTime())),
                            LayoutPage.this.ticketService.getTheatreName(ticket));

                    String redirectUrl = String.format("%s/%s?user=%s&recipient=%s&amount=%s&message=%s&target=%s",

                    // Demo-Payment Host. (same as ours)
                            target.replaceFirst("[^/]*/$", ""),

                            // Demo-Payment application.
                            "demo-payment/entry.seam",

                            // Paying user's OLAS name.
                            URLEncoder.encode(username, "UTF-8"),

                            // Payment Recipient.
                            URLEncoder.encode(CinemaConstants.PAYMENT_RECIPIENT, "UTF-8"),

                            // Payment Amount.
                            URLEncoder.encode(Double.toString(price), "UTF-8"),

                            // Payment Message.
                            URLEncoder.encode(message, "UTF-8"),

                            // Return page after payment completion.
                            URLEncoder.encode(target, "UTF-8"));

                    // FIXME: Cheat by reserving ticket before payment has
                    // actually been completed.
                    LayoutPage.this.ticketService.reserve(CinemaSession.get().getTicket());
                    CinemaSession.get().resetTicket();

                    // Redirect user to demo-payment.
                    getRequestCycle().setRequestTarget(new RedirectRequestTarget(redirectUrl));
                }

                catch (UnsupportedEncodingException e) {
                    LayoutPage.this.LOG.error("URL encoding error", e);
                }
            }
        }
    }
}
