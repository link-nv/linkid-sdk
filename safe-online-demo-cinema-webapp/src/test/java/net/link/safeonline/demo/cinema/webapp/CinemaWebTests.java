package net.link.safeonline.demo.cinema.webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;

import junit.framework.AssertionFailedError;
import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.CinemaShowTimeEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.service.InitializationService;
import net.link.safeonline.demo.cinema.service.bean.FilmServiceBean;
import net.link.safeonline.demo.cinema.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.cinema.service.bean.RoomServiceBean;
import net.link.safeonline.demo.cinema.service.bean.SeatServiceBean;
import net.link.safeonline.demo.cinema.service.bean.TheatreServiceBean;
import net.link.safeonline.demo.cinema.service.bean.TicketServiceBean;
import net.link.safeonline.demo.cinema.service.bean.UserServiceBean;
import net.link.safeonline.demo.cinema.webapp.LayoutPage.SelectedFilm;
import net.link.safeonline.demo.cinema.webapp.LayoutPage.SelectedPrice;
import net.link.safeonline.demo.cinema.webapp.LayoutPage.SelectedRoom;
import net.link.safeonline.demo.cinema.webapp.LayoutPage.SelectedTheatre;
import net.link.safeonline.demo.cinema.webapp.LayoutPage.SelectedTime;
import net.link.safeonline.demo.cinema.webapp.TicketPage.TicketForm;
import net.link.safeonline.demo.cinema.webapp.servlet.LogoutServlet;
import net.link.safeonline.demo.wicket.javaee.DummyJndi;
import net.link.safeonline.demo.wicket.test.AbstractWicketTests;
import net.link.safeonline.demo.wicket.tools.OlasAuthLink;
import net.link.safeonline.demo.wicket.tools.OlasLogoutLink;
import net.link.safeonline.demo.wicket.tools.WicketUtil;
import net.link.safeonline.sdk.auth.filter.LoginManager;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebApplication;
import org.junit.Test;


public class CinemaWebTests extends AbstractWicketTests {

    @Override
    public void setup() {

        super.setup();

        // Perform the Bank Initialization code that normally runs after webapp deployment.
        DummyJndi.lookup(InitializationService.class).buildEntities();
    }

    /**
     * Log the user in using (dummy) OLAS services.<br>
     * <br>
     * We end up on the {@link TicketPage}.
     */
    @Test
    public void testLogin() {

        // LoginPage: Verify.
        this.wicket.processRequestCycle();
        this.wicket.assertRenderedPage(LoginPage.class);
        this.wicket.assertComponent("loginLink", OlasAuthLink.class);

        // LoginPage: Click to login with digipass.
        this.wicket.clickLink("loginLink");

        // TicketPage: Verify && we've logged in successfully.
        assertTrue("Not logged in.", //
                CinemaSession.isUserSet());
        this.wicket.assertRenderedPage(TicketPage.class);
    }

    /**
     * Log the user in using (dummy) OLAS services.<br>
     * <br>
     * Log the user out.<br>
     * <br>
     * We end up on the {@link LoginPage}.
     */
    @Test
    public void testLogout() {

        // Login using OLAS.
        testLogin();

        // TicketPage: Verify.
        this.wicket.assertComponent("user:logout", OlasLogoutLink.class);

        // TicketPage: Log out.
        this.wicket.clickLink("user:logout");

        // LoginPage: Verify && OLAS user logged out successfully.
        assertFalse("OLAS credentials shouldn't be present.", //
                LoginManager.isAuthenticated(this.wicket.getServletRequest()));
        assertFalse("Shouldn't be logged in.", //
                CinemaSession.isUserSet());
        this.wicket.assertRenderedPage(LoginPage.class);
    }

    /**
     * Log in using OLAS.<br>
     * <br>
     * Create a new film ticket for the first film in the first theatre, in the first room it plays in for that theatre,
     * at the first available time on Monday (see {@link InitializationService}).<br>
     * <br>
     * We end up on the {@link TicketPage}.
     */
    @Test
    public void testNewTicket() {

        // Test data.
        int filmIndex = 0, theatreIndex = 0, roomIndex = 0, timeIndex = 0;
        int filmTheatreIndex = InitializationService.filmTheatres[filmIndex][theatreIndex];
        int filmTimeOfMonday = InitializationService.filmTimes[filmIndex][timeIndex].getMonStart();
        int filmTheatreRoomIndex = InitializationService.filmRooms[filmIndex][theatreIndex][roomIndex];

        GregorianCalendar filmTimeCalendar = new GregorianCalendar();
        filmTimeCalendar.setTimeInMillis(0);
        filmTimeCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        filmTimeCalendar.set(Calendar.HOUR_OF_DAY, filmTimeOfMonday / 3600);
        filmTimeCalendar.set(Calendar.MINUTE, filmTimeOfMonday % 3600 / 60);
        filmTimeCalendar.set(Calendar.SECOND, filmTimeOfMonday % 60);

        String testFilmName = InitializationService.filmNames[filmIndex];
        String testTheatreName = InitializationService.theatreNames[filmTheatreIndex];
        String testRoomName = InitializationService.theatreRooms[theatreIndex][filmTheatreRoomIndex];
        Long testPrice = InitializationService.filmPrices[filmIndex];
        Date testTime = filmTimeCalendar.getTime();
        int testSeatX = 1, testSeatY = 1;

        // Login using OLAS.
        testLogin();

        // TicketPage: Verify.
        this.wicket.assertPageLink("tickets:new", FilmTheatreSelectionPage.class);

        // TicketPage: Click to create new account.
        this.wicket.clickLink("tickets:new");

        // FilmTheatreSelectionPage: Verify.
        this.wicket.assertRenderedPage(FilmTheatreSelectionPage.class);
        this.wicket.assertComponent("films", Form.class);
        this.wicket.assertComponent("films:list", ListView.class);

        // FilmTheatreSelectionPage: Find our film.
        @SuppressWarnings("unchecked")
        ListView<CinemaFilmEntity> films = (ListView<CinemaFilmEntity>) this.wicket
                .getComponentFromLastRenderedPage("films:list");
        Iterator<? extends ListItem<CinemaFilmEntity>> filmsIt = films.iterator();

        String filmLink = null;
        while (filmsIt.hasNext()) {
            ListItem<CinemaFilmEntity> film = filmsIt.next();
            String sampleFilmName = film.getModelObject().getName();

            if (testFilmName.equals(sampleFilmName)) {
                filmLink = film.get("select").getPageRelativePath();
                break;
            }
        }
        assertNotNull(String.format("film not found: test: %s - sample: %s", testFilmName, films.getList()), //
                filmLink);
        this.wicket.assertComponent(filmLink, Link.class);

        // FilmTheatreSelectionPage: Select our film.
        this.wicket.clickLink(filmLink);

        // FilmTheatreSelectionPage: Verify.
        this.wicket.assertRenderedPage(FilmTheatreSelectionPage.class);
        this.wicket.assertComponent("theatres", Form.class);
        this.wicket.assertComponent("theatres:list", ListView.class);

        // FilmTheatreSelectionPage: Find our theatre.
        @SuppressWarnings("unchecked")
        ListView<CinemaTheatreEntity> theatres = (ListView<CinemaTheatreEntity>) this.wicket
                .getComponentFromLastRenderedPage("theatres:list");
        Iterator<? extends ListItem<CinemaTheatreEntity>> theatresIt = theatres.iterator();

        String theatreLink = null;
        while (theatresIt.hasNext()) {
            ListItem<CinemaTheatreEntity> theatre = theatresIt.next();
            String sampleTheatreName = theatre.getModelObject().getName();

            if (testTheatreName.equals(sampleTheatreName)) {
                theatreLink = theatre.get("select").getPageRelativePath();
                break;
            }
        }
        assertNotNull(String.format("theatre not found: test: %s - sample: %s", testTheatreName, theatres.getList()), //
                theatreLink);
        this.wicket.assertComponent(theatreLink, Link.class);

        // FilmTheatreSelectionPage: Select our theatre.
        this.wicket.clickLink(theatreLink);

        // TimeRoomSelectionPage: Verify.
        this.wicket.assertRenderedPage(TimeRoomSelectionPage.class);
        this.wicket.assertComponent("times", Form.class);
        this.wicket.assertComponent("times:list", ListView.class);

        // TimeRoomSelectionPage: Find our play time.
        @SuppressWarnings("unchecked")
        ListView<Date> times = (ListView<Date>) this.wicket.getComponentFromLastRenderedPage("times:list");
        Iterator<? extends ListItem<Date>> timesIt = times.iterator();

        String timeLink = null;
        while (timesIt.hasNext()) {
            ListItem<Date> time = timesIt.next();
            Date sampleTime = time.getModelObject();

            if (testTime.equals(sampleTime)) {
                timeLink = time.get("select").getPageRelativePath();
                break;
            }
        }
        assertNotNull(String.format("time not found: test: %s - sample: %s", testTime, times.getList()), //
                timeLink);
        this.wicket.assertComponent(timeLink, Link.class);

        // TimeRoomSelectionPage: Select our play time.
        this.wicket.clickLink(timeLink);

        // TimeRoomSelectionPage: Verify.
        this.wicket.assertRenderedPage(TimeRoomSelectionPage.class);
        this.wicket.assertComponent("rooms", Form.class);
        this.wicket.assertComponent("rooms:list", ListView.class);

        // TimeRoomSelectionPage: Find our room.
        @SuppressWarnings("unchecked")
        ListView<CinemaRoomEntity> rooms = (ListView<CinemaRoomEntity>) this.wicket
                .getComponentFromLastRenderedPage("rooms:list");
        Iterator<? extends ListItem<CinemaRoomEntity>> roomsIt = rooms.iterator();

        String roomLink = null;
        while (roomsIt.hasNext()) {
            ListItem<CinemaRoomEntity> room = roomsIt.next();
            String sampleRoomName = room.getModelObject().getName();

            if (testRoomName.equals(sampleRoomName)) {
                roomLink = room.get("select").getPageRelativePath();
                break;
            }
        }
        assertNotNull(String.format("room not found: test: %s - sample: %s", testRoomName, rooms.getList()), //
                roomLink);
        this.wicket.assertComponent(roomLink, Link.class);

        // TimeRoomSelectionPage: Select our room.
        this.wicket.clickLink(roomLink);

        // SeatSelectionPage: Verify.
        this.wicket.assertRenderedPage(SeatSelectionPage.class);
        this.wicket.assertComponent("seats", Form.class);
        this.wicket.assertComponent("seats:rows", ListView.class);

        // SeatSelectionPage: Find our seat.
        @SuppressWarnings("unchecked")
        ListView<List<CinemaSeatEntity>> rows = (ListView<List<CinemaSeatEntity>>) this.wicket
                .getComponentFromLastRenderedPage("seats:rows");
        Iterator<? extends ListItem<List<CinemaSeatEntity>>> rowsIt = rows.iterator();

        String seatLink = null;
        while (rowsIt.hasNext() && seatLink == null) {
            ListItem<List<CinemaSeatEntity>> row = rowsIt.next();

            @SuppressWarnings("unchecked")
            ListView<CinemaSeatEntity> columns = (ListView<CinemaSeatEntity>) row.get("columns");
            Iterator<? extends ListItem<CinemaSeatEntity>> columnsIt = columns.iterator();

            while (columnsIt.hasNext()) {
                ListItem<CinemaSeatEntity> column = columnsIt.next();

                CinemaSeatEntity sampleSeat = column.getModelObject();
                if (testSeatX == sampleSeat.getX() && testSeatY == sampleSeat.getY()) {
                    seatLink = column.get("seat").getPageRelativePath();
                    break;
                }
            }
        }
        assertNotNull(String.format("seat not found: test: %d,%d - sample: %s", testSeatX, testSeatY, rows.getList()), //
                seatLink);
        this.wicket.assertComponent(seatLink, Link.class);

        // SeatSelectionPage: Select our seat.
        this.wicket.clickLink(seatLink);

        // SeatSelectionPage: Verify && our ticket is complete and ready to be payed.
        this.wicket.assertComponent("ticket:film", SelectedFilm.class);
        this.wicket.assertComponent("ticket:theatre", SelectedTheatre.class);
        this.wicket.assertComponent("ticket:time", SelectedTime.class);
        this.wicket.assertComponent("ticket:room", SelectedRoom.class);
        this.wicket.assertComponent("ticket:payment", SelectedPrice.class);

        // - Collect sample data.
        String sampleTimeString = this.wicket.getComponentFromLastRenderedPage("ticket:time:time")
                .getDefaultModelObjectAsString();
        String samplePriceString = this.wicket.getComponentFromLastRenderedPage("ticket:payment:price")
                .getDefaultModelObjectAsString();
        try {
            String sampleFilmName = this.wicket.getComponentFromLastRenderedPage("ticket:film:name")
                    .getDefaultModelObjectAsString();
            String sampleTheatreName = this.wicket.getComponentFromLastRenderedPage("ticket:theatre:name")
                    .getDefaultModelObjectAsString();
            String sampleRoomName = this.wicket.getComponentFromLastRenderedPage("ticket:room:name")
                    .getDefaultModelObjectAsString();
            Number samplePrice = WicketUtil.getCurrencyFormat(CinemaSession.CURRENCY).parse(samplePriceString);
            Date sampleTime = WicketUtil.getDateFormat(Session.get().getLocale()).parse(sampleTimeString);

            // - Verify our sample data against the original test data.
            assertEquals(String.format("film mismatch: test: %s - sample: %s", testFilmName, sampleFilmName), //
                    testFilmName, sampleFilmName);
            assertEquals(String.format("theatre mismatch: test: %s - sample: %s", testTheatreName, sampleTheatreName), //
                    testTheatreName, sampleTheatreName);
            assertEquals(String.format("room mismatch: test: %s - sample: %s", testRoomName, sampleRoomName), //
                    testRoomName, sampleRoomName);
            assertEquals(String.format("price mismatch: test: %s - sample: %s", testPrice, samplePrice), //
                    testPrice, samplePrice);
            assertEquals(String.format("time mismatch: test: %s - sample: %s", testTime, sampleTime), //
                    testTime, sampleTime);
        }

        catch (ParseException e) {
            throw new AssertionFailedError(String.format("illegal time format: sample: %s", sampleTimeString));
        }

        // SeatSelectionPage: Pay for our ticket.
        this.wicket.assertComponent("ticket:payment:pay", Link.class);
        this.wicket.clickLink("ticket:payment:pay");

        // Demo-Payment: Verify && We were redirected to demo-payment; parse out target string and continue to it.
        String sampleRedirect = this.wicket.getServletResponse().getRedirectLocation();
        assertNotNull("Expected a redirect to demo-payment.", //
                sampleRedirect);
        assertTrue("Expected a target parameter on URL: " + sampleRedirect, //
                sampleRedirect.matches(".*[?&]target=.*"));
        String sampleTarget = sampleRedirect.replaceFirst(".*target=", "").replaceFirst("&(?!=amp;).*", "");
        assertNotSame("Target parameter is empty on URL: " + sampleRedirect, //
                "", sampleTarget);
        // FIXME: Can't figure out how to make wicket take that 'sampleTarget' URL.
        // Manually redirecting to the page in it for now (TicketPage).
        this.wicket.startPage(TicketPage.class);
        this.wicket.processRequestCycle();

        // TicketPage: Verify && purchased ticket is created and available.
        this.wicket.assertRenderedPage(TicketPage.class);
        this.wicket.assertComponent("tickets", TicketForm.class);

        this.wicket.dumpPage();
        @SuppressWarnings("unchecked")
        ListView<CinemaTicketEntity> tickets = (ListView<CinemaTicketEntity>) this.wicket
                .getComponentFromLastRenderedPage("tickets:list");
        assertNotNull("No tickets in ticket history.", //
                tickets);
        assertTrue(String.format("Expected (exactly) one ticket: sample: %s", tickets.getList()), //
                tickets.getList().size() == 1);
        ListItem<CinemaTicketEntity> ticket = tickets.iterator().next();

        // - Collect sample data.
        sampleTimeString = ticket.get("time").getDefaultModelObjectAsString();
        samplePriceString = ticket.get("price").getDefaultModelObjectAsString();
        try {
            String sampleFilmName = ticket.get("film").getDefaultModelObjectAsString();
            String sampleTheatreName = ticket.get("theatre").getDefaultModelObjectAsString();
            String sampleRoomName = ticket.get("room").getDefaultModelObjectAsString();
            Number samplePrice = WicketUtil.getCurrencyFormat(CinemaSession.CURRENCY).parse(samplePriceString);
            Date sampleTime = WicketUtil.getDateFormat(Session.get().getLocale()).parse(sampleTimeString);

            // - Verify our sample data against the original test data.
            assertEquals(String.format("film mismatch: test: %s - sample: %s", testFilmName, sampleFilmName), //
                    testFilmName, sampleFilmName);
            assertEquals(String.format("theatre mismatch: test: %s - sample: %s", testTheatreName, sampleTheatreName), //
                    testTheatreName, sampleTheatreName);
            assertEquals(String.format("room mismatch: test: %s - sample: %s", testRoomName, sampleRoomName), //
                    testRoomName, sampleRoomName);
            assertEquals(String.format("price mismatch: test: %s - sample: %s", testPrice, samplePrice), //
                    testPrice, samplePrice);
            assertEquals(String.format("time mismatch: test: %s - sample: %s", testTime, sampleTime), //
                    testTime, sampleTime);
        }

        catch (ParseException e) {
            throw new AssertionFailedError(String.format("illegal time format: sample: %s", sampleTimeString));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOLASUser() {

        return "tester";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends HttpServlet> getLogoutServlet() {

        return LogoutServlet.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected WebApplication getApplication() {

        return new CinemaApplication();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getServiceBeans() {

        return new Class[] { FilmServiceBean.class, InitializationServiceBean.class, RoomServiceBean.class,
                SeatServiceBean.class, TheatreServiceBean.class, TicketServiceBean.class, UserServiceBean.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class[] { CinemaFilmEntity.class, CinemaRoomEntity.class, CinemaSeatEntity.class,
                CinemaSeatOccupationEntity.class, CinemaShowTimeEntity.class, CinemaTheatreEntity.class,
                CinemaTicketEntity.class, CinemaUserEntity.class };
    }
}
