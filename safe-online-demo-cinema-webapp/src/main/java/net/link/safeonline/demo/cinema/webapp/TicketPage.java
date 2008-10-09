package net.link.safeonline.demo.cinema.webapp;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.service.UserService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * <h2>{@link TicketPage}<br>
 * <sub>Wicket backend for ticket overview page.</sub></h2>
 *
 * <p>
 * On this page the user can see all the tickets he purchased in the past and start the process of purchasing a new one.
 * </p>
 *
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class TicketPage extends LayoutPage {

    private static final long serialVersionUID = 1L;

    @EJB
    transient UserService     userService;


    /**
     * If film and theatre are selected; continue to the time and room selection page.
     *
     * If not, assign components to the HTML wicket elements so the user can select a film and theatre.
     */
    public TicketPage() {

        if (!CinemaSession.isUserSet()) {
            setResponsePage(LoginPage.class);
            return;
        }

        add(new Label("headerTitle", "Ticket History"));

        add(new TicketForm("tickets"));
    }


    /**
     * <h2>{@link TicketForm}<br>
     * <sub>Ticket History Form.</sub></h2>
     *
     * <p>
     * This form shows some information on purchased tickets.
     *
     * A link lets the user purchase a new one.
     * </p>
     *
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     *
     * @author mbillemo
     */
    class TicketForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public TicketForm(String id) {

            super(id);

            final List<CinemaTicketEntity> data = TicketPage.this.ticketService.getTickets(CinemaSession.get().getUser());

            add(new ListView<CinemaTicketEntity>("list", data) {

                private static final long serialVersionUID = 1L;

                {
                    setVisible(!data.isEmpty());
                }


                @Override
                protected void populateItem(ListItem<CinemaTicketEntity> item) {

                    final CinemaTicketEntity ticket = item.getModelObject();

                    /* Ticket Details. */
                    item.add(new Label("time", WicketUtil.format(Session.get(), new Date(ticket.getTime()))));
                    item.add(new Label("film", TicketPage.this.ticketService.getFilmName(ticket)));
                    item.add(new Label("theatre", TicketPage.this.ticketService.getTheatreName(ticket)));
                    item.add(new Label("room", TicketPage.this.ticketService.getRoomName(ticket)));
                    item.add(new Label("price", WicketUtil.format(CURRENCY, ticket.getPrice())));
                }
            });

            add(new Link<FilmTheatreSelectionPage>("new") {

                private static final long serialVersionUID = 1L;


                @Override
                public void onClick() {

                    setResponsePage(FilmTheatreSelectionPage.class);
                }
            });
        }

        @Override
        protected void onSubmit() {

        }
    }
}
