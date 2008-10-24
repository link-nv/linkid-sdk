package net.link.safeonline.demo.cinema.webapp;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaShowTimeEntity;
import net.link.safeonline.demo.cinema.service.FilmService;
import net.link.safeonline.demo.cinema.service.RoomService;
import net.link.safeonline.demo.wicket.tools.WicketUtil;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * <h2>{@link TimeRoomSelectionPage}<br>
 * <sub>Wicket backend for time and room selection page.</sub></h2>
 * 
 * <p>
 * On this page the user selects at what time and in which room he wants to view his film.
 * </p>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
public class TimeRoomSelectionPage extends LayoutPage {

    private static final long serialVersionUID = 1L;

    @EJB
    transient FilmService     filmService;

    @EJB
    transient RoomService     roomService;


    /**
     * If time and room are selected; continue to the seat selection page.
     * 
     * If not, assign components to the HTML wicket elements so the user can select a show time and the room.
     */
    public TimeRoomSelectionPage() {

        // If theatre and film are not yet set; go back.
        if (!CinemaSession.isFilmAndTheaterSet())
            throw new RestartResponseException(FilmTheatreSelectionPage.class);
        // If room and time selected, send user to the seat selection page.
        if (CinemaSession.isTimeAndRoomSet())
            throw new RestartResponseException(SeatSelectionPage.class);

        add(new Label("headerTitle", "Time And Room Selection"));

        add(new TimesForm("times"));
        add(new RoomsForm("rooms"));
    }


    /**
     * <h2>{@link TimesForm}<br>
     * <sub>Time Selection Form.</sub></h2>
     * 
     * <p>
     * This form shows the times at which the selected film plays.
     * 
     * TODO:
     * 
     * When no room is selected, it lists all times, otherwise it limits the time selection to those available in the selected rooms.
     * 
     * The user can then select the viewing time of his choosing.
     * </p>
     * 
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class TimesForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public TimesForm(String id) {

            super(id);
            setVisible(!CinemaSession.isTimeSet());

            // TODO: Either get all times or just those for the selected room.
            List<Date> data = new LinkedList<Date>();
            for (CinemaShowTimeEntity showTime : CinemaSession.get().getFilm().getTimes()) {
                addDate(data, Calendar.MONDAY, showTime.getMonStart());
                addDate(data, Calendar.TUESDAY, showTime.getTueStart());
                addDate(data, Calendar.WEDNESDAY, showTime.getWedStart());
                addDate(data, Calendar.THURSDAY, showTime.getThuStart());
                addDate(data, Calendar.FRIDAY, showTime.getFriStart());
                addDate(data, Calendar.SATURDAY, showTime.getSatStart());
                addDate(data, Calendar.SUNDAY, showTime.getSunStart());
            }

            add(new ListView<Date>("list", data) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<Date> item) {

                    final Date time = item.getModelObject();

                    item.add(new Link<String>("select") {

                        private static final long serialVersionUID = 1L;

                        {
                            add(new Label("time", WicketUtil.format(getLocale(), time)));
                        }


                        @Override
                        public void onClick() {

                            CinemaSession.get().setTime(time);
                            setResponsePage(TimeRoomSelectionPage.class);
                        }
                    });
                }
            });
        }

        /**
         * Calculate the {@link Date} for the given time of the day on the given day of the current week.
         */
        private void addDate(List<Date> data, int dayOfWeek, Integer timeOfDay) {

            if (timeOfDay == null)
                return;

            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(0);
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, timeOfDay / 3600);
            calendar.set(Calendar.MINUTE, timeOfDay % 3600 / 60);
            calendar.set(Calendar.SECOND, timeOfDay % 60);

            data.add(calendar.getTime());
        }

        @Override
        protected void onSubmit() {

        }
    }

    /**
     * <h2>{@link RoomsForm}<br>
     * <sub>Room Selection Form.</sub></h2>
     * 
     * <p>
     * This form shows the rooms in which the selected film plays.
     * 
     * TODO:
     * 
     * When no time is selected, it lists all rooms, otherwise it limits the room selection to those that play the film at the selected
     * time.
     * 
     * The user can then select the room of his choosing.
     * <p>
     * 
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class RoomsForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public RoomsForm(String id) {

            super(id);
            setVisible(!CinemaSession.isRoomSet());

            // Either get all rooms TODO: or just those that play the film at
            // the selected time.
            List<CinemaRoomEntity> data = TimeRoomSelectionPage.this.roomService.getRoomsFor(CinemaSession.get().getTheatre(),
                    CinemaSession.get().getFilm());

            add(new ListView<CinemaRoomEntity>("list", data) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<CinemaRoomEntity> item) {

                    final CinemaRoomEntity room = item.getModelObject();

                    item.add(new Link<String>("select") {

                        private static final long serialVersionUID = 1L;

                        {
                            add(new Label("name", room.getName()));
                        }


                        @Override
                        public void onClick() {

                            CinemaSession.get().setRoom(room);
                            setResponsePage(TimeRoomSelectionPage.class);
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
