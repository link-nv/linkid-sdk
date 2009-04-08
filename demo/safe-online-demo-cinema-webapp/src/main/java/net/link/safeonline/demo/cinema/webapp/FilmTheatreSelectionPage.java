package net.link.safeonline.demo.cinema.webapp;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;
import net.link.safeonline.demo.cinema.service.FilmService;
import net.link.safeonline.demo.cinema.service.TheatreService;
import net.link.safeonline.wicket.tools.WicketUtil;
import net.link.safeonline.wicket.web.RequireLogin;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * <h2>{@link FilmTheatreSelectionPage}<br>
 * <sub>Wicket backend for theatre and film selection page.</sub></h2>
 * 
 * <p>
 * On this page the user selects a film and the theatre in which he'd like to view his film. Upon selecting either it is added to the
 * session and the other's view is limited to only those entities that still apply.
 * </p>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@RequireLogin(loginPage = LoginPage.class)
public class FilmTheatreSelectionPage extends LayoutPage {

    private static final long serialVersionUID = 1L;

    @EJB(mappedName = FilmService.JNDI_BINDING)
    FilmService               filmService;

    @EJB(mappedName = TheatreService.JNDI_BINDING)
    TheatreService            theatreService;

    private FilmsForm         filmsForm;

    private TheatersForm      theatresForm;


    /**
     * If film and theatre are selected; continue to the time and room selection page.
     * 
     * If not, assign components to the HTML wicket elements so the user can select a film and theatre.
     */
    public FilmTheatreSelectionPage() {

        add(new Label("headerTitle", "Film And Theatre Selection"));

        add(filmsForm = new FilmsForm("films"));
        add(theatresForm = new TheatersForm("theatres"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBeforeRender() {

        // If theatre and film selected, send user to the time and room selection page.
        if (CinemaSession.isFilmAndTheaterSet())
            throw new RestartResponseException(TimeRoomSelectionPage.class);

        filmsForm.setVisible(!CinemaSession.isFilmSet());
        theatresForm.setVisible(!CinemaSession.isTheaterSet());

        super.onBeforeRender();
    }


    /**
     * <h2>{@link FilmsForm}<br>
     * <sub>Film Selection Form.</sub></h2>
     * 
     * <p>
     * This form shows some information on films.
     * 
     * When no theatre is selected, it lists all films, otherwise it limits the film selection to those available in the selected theatre.
     * 
     * The user can then select a film to purchase a ticket for.
     * </p>
     * 
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class FilmsForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public FilmsForm(String id) {

            super(id);

            // Either get all films or just those that play in selected theatre.
            List<CinemaFilmEntity> data;
            if (CinemaSession.isTheaterSet()) {
                data = filmService.getFilmsThatPlayIn(CinemaSession.get().getTheatre());
            } else {
                data = filmService.getAllFilms();
            }

            add(new ListView<CinemaFilmEntity>("list", data) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<CinemaFilmEntity> item) {

                    final CinemaFilmEntity film = item.getModelObject();

                    /* Film Details. */
                    item.add(new Label("price", WicketUtil.format(CinemaSession.CURRENCY, film.getPrice())));
                    item.add(new Label("description", film.getDescription()));

                    /* Film Selection. */
                    item.add(new Link<String>("select") {

                        private static final long serialVersionUID = 1L;

                        {
                            add(new Label("name", film.getName()));
                        }


                        @Override
                        public void onClick() {

                            CinemaSession.get().setFilm(film);
                        }
                    });
                }
            });
        }

        @Override
        protected void onSubmit() {

        }
    }

    /**
     * <h2>{@link TheatersForm}<br>
     * <sub>Theatre Selection Form.</sub></h2>
     * 
     * <p>
     * This form shows some information on theatres.
     * 
     * When no film is selected, it lists all theatres, otherwise it limits the theatre selection to those that play the selected film.
     * 
     * The user can then select a theatre to purchase a ticket for.
     * <p>
     * 
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class TheatersForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public TheatersForm(String id) {

            super(id);

            // Either get all theatres or just those that play selected film.
            List<CinemaTheatreEntity> data;
            if (CinemaSession.isFilmSet()) {
                data = theatreService.getTheatresThatPlay(CinemaSession.get().getFilm());
            } else {
                data = theatreService.getAllTheatres();
            }

            add(new ListView<CinemaTheatreEntity>("list", data) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<CinemaTheatreEntity> item) {

                    final CinemaTheatreEntity theatre = item.getModelObject();

                    /* Theatre Location. */
                    item.add(new Label("address", theatre.getAddress()));

                    /* Theatre Selection. */
                    item.add(new Link<String>("select") {

                        private static final long serialVersionUID = 1L;

                        {
                            add(new Label("name", theatre.getName()));
                        }


                        @Override
                        public void onClick() {

                            CinemaSession.get().setTheatre(theatre);
                        }
                    });
                }
            });
        }

        @Override
        protected void onSubmit() {

        }
    }
}