package net.link.safeonline.demo.cinema.webapp;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;

import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.service.RoomService;
import net.link.safeonline.demo.cinema.service.SeatService;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;


/**
 * <h2>{@link SeatSelectionPage}<br>
 * <sub>Wicket backend for seat selection page.</sub></h2>
 *
 * <p>
 * [description / usage].
 * </p>
 *
 * <p>
 * <i>Jun 24, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
public class SeatSelectionPage extends LayoutPage {

    @EJB
    transient RoomService roomService;

    @EJB
    transient SeatService seatService;


    /**
     * If not, assign components to the HTML wicket elements so the user can select a seat in the theatre.
     */
    public SeatSelectionPage() {

        // If time or room are not yet set, go back.
        if (!CinemaSession.isTimeAndRoomSet())
            throw new RestartResponseException( TimeRoomSelectionPage.class);

        add(new Label("headerTitle", "Seat Selection"));

        add(new SeatsForm("seats"));
    }


    /**
     * <h2>{@link SeatsForm}<br>
     * <sub>A form that lets the user pick seats in the selected room.</sub></h2>
     *
     * <p>
     * <i>Jun 24, 2008</i>
     * </p>
     *
     * @author mbillemo
     */
    class SeatsForm extends Form<String> {

        private static final long serialVersionUID = 1L;


        public SeatsForm(String id) {

            super(id);

            // Room Name.
            add(new Label("name", CinemaSession.get().getRoom().getName()));

            // Create a grid of seat occupations.
            List<List<CinemaSeatEntity>> rows = new ArrayList<List<CinemaSeatEntity>>();
            List<CinemaSeatEntity> seats = SeatSelectionPage.this.seatService.getSeatsFor(CinemaSession.get().getRoom());
            for (CinemaSeatEntity seat : seats) {
                // Add empty rows.
                while (rows.size() < seat.getY()) {
                    rows.add(new ArrayList<CinemaSeatEntity>());
                }

                // Add empty seats.
                List<CinemaSeatEntity> row = rows.get(seat.getY() - 1);
                while (row.size() < seat.getX()) {
                    row.add(null);
                }

                // Add this seat.
                row.set(seat.getX() - 1, seat);
            }

            add(new ListView<List<CinemaSeatEntity>>("rows", rows) {

                private static final long serialVersionUID = 1L;


                @Override
                protected void populateItem(ListItem<List<CinemaSeatEntity>> rowItem) {

                    rowItem.add(new ListView<CinemaSeatEntity>("columns", rowItem.getModelObject()) {

                        private static final long serialVersionUID = 1L;


                        @Override
                        protected void populateItem(final ListItem<CinemaSeatEntity> seatItem) {

                            final CinemaSeatEntity seat = seatItem.getModelObject();
                            seatItem.add(new Link<String>("seat") {

                                private static final long serialVersionUID = 1L;


                                @Override
                                public void onClick() {

                                    CinemaSession.get().toggleSeat(seat);
                                    setResponsePage(SeatSelectionPage.class);
                                }

                                @Override
                                protected void onComponentTag(ComponentTag tag) {

                                    super.onComponentTag(tag);

                                    if (SeatSelectionPage.this.seatService.isOccupied(seat, CinemaSession.get()
                                            .getTime())) {
                                        tag.put("class", "disabled");
                                    }
                                    if (CinemaSession.isOccupied(seat)) {
                                        tag.put("class", "selected");
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }

        @Override
        protected void onSubmit() {

            // TODO: Redirect to payment.
        }
    }
}
