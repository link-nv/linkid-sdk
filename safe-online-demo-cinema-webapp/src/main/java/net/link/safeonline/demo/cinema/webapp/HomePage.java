package net.link.safeonline.demo.cinema.webapp;

import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.FilmEntity;
import net.link.safeonline.demo.cinema.service.FilmService;
import net.link.safeonline.demo.cinema.service.bean.FilmServiceBean;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

public class HomePage extends Layout {

    private static final long serialVersionUID = 1L;

    @EJB
    transient FilmService     filmService      = new FilmServiceBean();


    public HomePage() {

        add(new Label<String>("headerTitle", "Welcome Page"));
        // add(new AdminNavigationBorder("navigation"));
        // add(new PageLink("listLink", ListPage.class));
        // add(new PageLink("editLink", FindCinema.class));

        add(new FilmsForm("films"));
        // add(new TheatersForm("theaters"));
    }


    class FilmsForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public FilmsForm(String id) {

            super(id);

            List<FilmEntity> data = HomePage.this.filmService.getAllFilms();
            add(new ListView<FilmEntity>("list", data) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<FilmEntity> item) {

                    FilmEntity film = item.getModelObject();
                    item.add(new Label<String>("name", film.getName()));
                    item.add(new Label<String>("description", film
                            .getDescription()));
                }

            });
        }

        @Override
        protected void onSubmit() {

        }
    }

    class TheatersForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public TheatersForm(String id) {

            super(id);
        }

        @Override
        protected void onSubmit() {

        }
    }
}
