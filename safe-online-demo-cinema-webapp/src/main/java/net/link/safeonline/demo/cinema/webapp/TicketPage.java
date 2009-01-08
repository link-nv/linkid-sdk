package net.link.safeonline.demo.cinema.webapp;

import java.util.Date;
import java.util.List;

import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.PageLink;
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
@RequireLogin(loginPage = LoginPage.class)
public class TicketPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * If film and theatre are selected; continue to the time and room selection page.
     * 
     * If not, assign components to the HTML wicket elements so the user can select a film and theatre.
     */
    public TicketPage() {

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
                    item.add(new Label("time", WicketUtil.format(getLocale(), new Date(ticket.getTime()))));
                    item.add(new Label("film", TicketPage.this.ticketService.getFilmName(ticket)));
                    item.add(new Label("theatre", TicketPage.this.ticketService.getTheatreName(ticket)));
                    item.add(new Label("room", TicketPage.this.ticketService.getRoomName(ticket)));
                    item.add(new Label("price", WicketUtil.format(CinemaSession.CURRENCY, ticket.getPrice())));
                }
            });

            add(new PageLink("new", FilmTheatreSelectionPage.class));
        }

        @Override
        protected void onSubmit() {

        }
    }
}
